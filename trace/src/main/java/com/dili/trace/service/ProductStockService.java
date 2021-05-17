package com.dili.trace.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.dao.ProductStockMapper;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.google.common.collect.Lists;

import com.google.common.collect.Maps;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import one.util.streamex.StreamEx;
import tk.mybatis.mapper.entity.Example;

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
     * 返还重量(上架,退货,交易取消)
     *
     * @param tradeDetailId
     * @param returnedWeight
     * @return
     */
    @Transactional
    public int returnWeight(Long tradeDetailId, BigDecimal returnedWeight) {
        if (Objects.isNull(tradeDetailId)) {
            throw new TraceBizException("参数错误");
        }
        TradeDetail tradeDetailItem = this.tradeDetailService.get(tradeDetailId);
        if (Objects.isNull(tradeDetailItem)) {
            throw new TraceBizException("批次库存数据不存在");
        }

        Long productStockId = tradeDetailItem.getProductStockId();

        ProductStock productStockItem = this.selectByIdForUpdate(productStockId).orElseThrow(() -> {
            throw new TraceBizException("库存数据不存在");
        });


        DetectResultEnum detectResultEnum = StreamEx.of(this.detectRequestService.findDetectRequestByBillId(tradeDetailItem.getBillId())).map(detectRequest -> {
            return detectRequest.getDetectResult();
        }).map(DetectResultEnum::fromCode).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(DetectResultEnum.NONE);

        ProductStock psUpdatable = new ProductStock();
        psUpdatable.setId(productStockItem.getId());

        if (DetectResultEnum.PASSED == detectResultEnum) {
            psUpdatable.setStockWeight(productStockItem.getStockWeight().add(returnedWeight));
        } else if (DetectResultEnum.FAILED == detectResultEnum) {
            psUpdatable.setDetectFailedWeight(productStockItem.getDetectFailedWeight().add(returnedWeight));
        } else if (DetectResultEnum.NONE == detectResultEnum) {
            psUpdatable.setStockWeight(productStockItem.getStockWeight().add(returnedWeight));
        } else {
            throw new TraceBizException("检测结果错误");
        }
        return this.updateSelective(psUpdatable);
    }

    /**
     * 检测之后,根据billId更新检测失败重量
     *
     * @param billId
     */
    @Transactional
    public int updateDetectFailedWeightByBillIdAfterDetect(Long billId) {
        logger.info("开始更新检测失败库存:billId={}", billId);
        if (this != null) {
            return 0;
        }
        if (Objects.isNull(billId)) {
            throw new TraceBizException("变更检测失败重量:参数错误");
        }

        DetectResultEnum detectResultEnum = StreamEx.of(this.detectRequestService.findDetectRequestByBillId(billId)).map(detectRequest -> {
            return detectRequest.getDetectResult();
        }).map(DetectResultEnum::fromCode).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(DetectResultEnum.NONE);


        return EntryStream.of(this.findGroupedTradeDetailsByBillId(billId)).mapKeys(productStockId -> {
            ProductStock productStockItem = this.selectByIdForUpdate(productStockId).orElseThrow(() -> {
                throw new TraceBizException("库存数据不存在");
            });
            return productStockItem;
        }).mapValues(tdList -> {
            BigDecimal totalFailedWeight = StreamEx.of(tdList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, (t, v) -> {
                return t.add(v);
            });

            if (DetectResultEnum.PASSED == detectResultEnum) {
                return totalFailedWeight;
            } else if (DetectResultEnum.FAILED == detectResultEnum) {
                return totalFailedWeight.negate();
            } else if (DetectResultEnum.NONE == detectResultEnum) {
                return BigDecimal.ZERO;
            } else {
                throw new TraceBizException("检测结果错误");
            }

        }).mapKeyValue((productStockItem, totalFailedWeight) -> {

            ProductStock psUpdatable = new ProductStock();
            psUpdatable.setId(productStockItem.getId());
            psUpdatable.setDetectFailedWeight(productStockItem.getDetectFailedWeight().add(totalFailedWeight));
            psUpdatable.setStockWeight(productStockItem.getStockWeight().subtract(totalFailedWeight));
            return this.updateSelective(psUpdatable);

        }).mapToInt(Integer::intValue).sum();

    }


    /**
     * 根据billid查询tradedetail
     *
     * @param billId
     * @return
     */
    private Map<Long, List<TradeDetail>> findGroupedTradeDetailsByBillId(Long billId) {
        if (Objects.isNull(billId)) {
            return Maps.newHashMap();
        }
        TradeDetail td = new TradeDetail();
        td.setBillId(billId);
        return StreamEx.of(this.tradeDetailService.listByExample(td)).groupingBy(TradeDetail::getProductStockId);
    }

    /**
     * 增减库存重量(正数增加,负数减少)
     *
     * @param productStockId
     * @param alterWeight
     * @return
     */
    private int updateStockWeight(Long productStockId, BigDecimal alterWeight) {

        ProductStock item = this.get(productStockId);
        if (item == null) {
            throw new TraceBizException("库存信息不存在");
        }
        if (alterWeight.compareTo(BigDecimal.ZERO) < 0 && item.getStockWeight().add(alterWeight).compareTo(BigDecimal.ZERO) < 0) {
            throw new TraceBizException("扣减库存不能超过总库存");
        }
        ProductStock up = new ProductStock();
        up.setId(item.getId());
        up.setStockWeight(item.getStockWeight().add(alterWeight));
        return this.updateSelective(up);
    }

    /**
     * 增减检测失败重量(正数增加,负数减少)
     *
     * @param productStockId
     * @param alterWeight
     * @return
     */
    private int updateDetectFailedWeight(Long productStockId, BigDecimal alterWeight) {

        ProductStock item = this.get(productStockId);
        if (item == null) {
            throw new TraceBizException("库存信息不存在");
        }
        if (alterWeight.compareTo(BigDecimal.ZERO) < 0 && item.getDetectFailedWeight().add(alterWeight).compareTo(BigDecimal.ZERO) < 0) {
            throw new TraceBizException("扣减库存不能超过总库存");
        }
        ProductStock up = new ProductStock();
        up.setId(item.getId());
        up.setDetectFailedWeight(item.getDetectFailedWeight().add(alterWeight));
        return this.updateSelective(up);
    }
}
