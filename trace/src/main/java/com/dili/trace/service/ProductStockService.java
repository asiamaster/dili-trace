package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.api.input.TradeDetailQueryDto;
import com.dili.trace.dao.ProductStockMapper;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.enums.SaleStatusEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.google.common.collect.Lists;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 库存service
 */
@Service
public class ProductStockService extends BaseServiceImpl<ProductStock, Long> {
    private static final Logger logger = LoggerFactory.getLogger(ProductStockService.class);
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    TradeService tradeService;

    /**
     * 查询 锁定数据行
     *
     * @param id
     * @return
     */
    @Transactional
    public Optional<ProductStock> selectByIdForUpdate(Long id) {
        return ((ProductStockMapper) this.getDao()).selectByIdForUpdate(id);
    }

    /**
     * 根据id查找 数据
     *
     * @param idList
     * @return
     */
    public List<ProductStock> findByIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return Lists.newArrayList();
        }
        Example e = new Example(ProductStock.class);
        e.and().andIn("id", idList);
        return this.getDao().selectByExample(e);
    }

    /**
     * 查询或创建
     *
     * @param buyerId
     * @param billItem
     * @return
     */
    @Transactional
    public ProductStock findOrCreateBatchStock(Long buyerId, RegisterBill billItem) {
        Long marketId = billItem.getMarketId();
        ProductStock query = new ProductStock();
        query.setUserId(buyerId);
        query.setMarketId(marketId);
        // query.setPreserveType(tradeDetailItem.getPreserveType());
        query.setProductId(billItem.getProductId());
        query.setWeightUnit(billItem.getWeightUnit());
        query.setSpecName(billItem.getSpecName());
        query.setBrandId(billItem.getBrandId());
        ProductStock batchStockItem = StreamEx.of(this.listByExample(query)).findFirst().orElseGet(() -> {
            // 创建初始BatchStock
            CustomerExtendDto cust = this.customerRpcService.findCustomerByIdOrEx(buyerId, marketId);
            return this.registerBillService.selectByIdForUpdate(billItem.getId()).map(bill -> {
                ProductStock batchStock = new ProductStock();
                batchStock.setUserId(cust.getId());
                batchStock.setUserName(cust.getName());
                batchStock.setMarketId(marketId);
                batchStock.setPreserveType(bill.getPreserveType());
                batchStock.setProductId(bill.getProductId());
                batchStock.setProductName(bill.getProductName());
                batchStock.setStockWeight(BigDecimal.ZERO);
                batchStock.setTotalWeight(BigDecimal.ZERO);
                batchStock.setWeightUnit(bill.getWeightUnit());
                batchStock.setSpecName(bill.getSpecName());
                batchStock.setBrandId(bill.getBrandId());
                batchStock.setBrandName(bill.getBrandName());
                batchStock.setTradeDetailNum(0);
                this.insertSelective(batchStock);
                return batchStock;
            }).orElseThrow(() -> {
                return new TraceBizException("创建库存失败");
            });

        });
        return batchStockItem;
    }


    /**
     * 更新检测结果
     *
     * @param billId
     * @return
     */
    public int updateProductStockAndTradeDetailAfterDetect(Long billId, Optional<OperatorUser> operatorUser) {


        DetectResultEnum detectResultEnum = StreamEx.of(this.detectRequestService.findDetectRequestByBillId(billId)).map(detectRequest -> {
            return detectRequest.getDetectResult();
        }).map(DetectResultEnum::fromCode).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(DetectResultEnum.NONE);

        if (detectResultEnum == null) {
            return 0;
        }
        if (DetectResultEnum.NONE == detectResultEnum) {
            return 0;
        }
        RegisterBill billItem = this.registerBillService.getAndCheckById(billId).orElseThrow(() -> {
            return new TraceBizException("报备单不存在");
        });

        this.tradeService.createBatchStockAfterVerifiedAndCheckin(billItem.getBillId(), operatorUser);
        TradeDetail tdq = new TradeDetail();
        tdq.setBillId(billId);
        Map<Long, List<TradeDetail>> productIdTradeDetailListMap = StreamEx.of(StreamEx.of(this.tradeDetailService.listByExample(tdq))).groupingBy(TradeDetail::getProductStockId);

        return EntryStream.of(productIdTradeDetailListMap).mapKeyValue((productStockId, tradeDetailList) -> {
            int rowCount = StreamEx.of((tradeDetailList)).mapToInt(tradeDetail -> {
                TradeDetail upTD = new TradeDetail();
                upTD.setId(tradeDetail.getId());
                if (DetectResultEnum.FAILED == detectResultEnum) {
                    upTD.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
                } else {
                    upTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                }
                return this.tradeDetailService.updateSelective(upTD);
            }).sum();

            this.updateProductStock(productStockId);

            return rowCount;
        }).mapToInt(Integer::intValue).sum();
    }


    /**
     * 根据相关的tradedetail数据,更新库存productstock相关数据
     *
     * @param productStockId
     * @return
     */
    public int updateProductStock(Long productStockId) {
        if (productStockId == null) {
            return 0;
        }
        ProductStock ps = this.selectByIdForUpdate(productStockId).orElseThrow(() -> {
            return new TraceBizException("库存信息不存在");
        });
        TradeDetailQueryDto tdq = new TradeDetailQueryDto();
        tdq.setProductStockId(ps.getProductStockId());
        tdq.setMinStockWeight(BigDecimal.ZERO);
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tdq);

        List<TradeDetail> notForSaleList = StreamEx.of(tradeDetailList)
                .filter(tradeDetail -> SaleStatusEnum.NOT_FOR_SALE.equalsToCode(tradeDetail.getSaleStatus()))
                .toList();
        List<Long> notForSaleBillIdList = StreamEx.of(notForSaleList).map(TradeDetail::getBillId).nonNull().toList();
        RegisterBillDto q = new RegisterBillDto();
        q.setIdList(notForSaleBillIdList);
        Map<Long, RegisterBill> idBillMap = StreamEx.of(q).filter(v -> v.getIdList().size() > 0).flatCollection(v -> this.registerBillService.listByExample(v)).toMap(r -> r.getBillId(), Function.identity());
        logger.info("not for sale id List={}",StreamEx.of(notForSaleList).map(TradeDetail::getId).toList());


        List<TradeDetail> forSaleList = StreamEx.of(tradeDetailList)
                .filter(tradeDetail -> SaleStatusEnum.FOR_SALE.equalsToCode(tradeDetail.getSaleStatus()))
                .toList();
        logger.info("for sale id List={}",StreamEx.of(forSaleList).map(TradeDetail::getId).toList());


        BigDecimal detectFailedWeight = StreamEx.of(notForSaleList).map(tradeDetail -> {
            RegisterBill rb = idBillMap.get(tradeDetail.getBillId());
            BillVerifyStatusEnum verifyStatusEnum = BillVerifyStatusEnum.fromCodeOrEx(rb.getVerifyStatus());
            DetectResultEnum detectResultEnum = StreamEx.ofNullable(rb).map(RegisterBill::getDetectRequestId).nonNull().map(this.detectRequestService::get)
                    .nonNull().map(DetectRequest::getDetectResult)
                    .map(DetectResultEnum::fromCodeOrEx).findFirst().orElse(DetectResultEnum.NONE);
            if (BillVerifyStatusEnum.RETURNED == verifyStatusEnum) {
                if (DetectResultEnum.FAILED == detectResultEnum) {
                    return tradeDetail.getTotalWeight();
                }
            }
            return BigDecimal.ZERO;

        }).reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal softWeight = StreamEx.of(tradeDetailList).map(TradeDetail::getSoftWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        int tradenum = forSaleList.size();
        BigDecimal stockWeight = StreamEx.of(forSaleList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        ProductStock up = new ProductStock();
        up.setId(ps.getId());
        up.setTradeDetailNum(tradenum);
        up.setStockWeight(stockWeight);
        up.setDetectFailedWeight(detectFailedWeight);
        up.setSoftWeight(softWeight);
        return this.updateSelective(up);
    }
}
