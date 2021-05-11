package com.dili.trace.rpc.service;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.StockInUnitEnum;
import com.dili.trace.enums.WeightUnitEnum;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.StockRegisterSourceEnum;
import com.dili.trace.rpc.api.ProductRpc;
import com.dili.trace.rpc.dto.*;
import com.dili.trace.service.BillService;
import com.dili.trace.service.ProductStockService;
import com.dili.trace.service.TradeDetailService;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 库存服务
 */
@Service
public class ProductRpcService {

    private static final Logger logger = LoggerFactory.getLogger(ProductRpcService.class);

    @Autowired(required = false)
    ProductRpc productRpc;
    @Autowired
    FirmRpcService firmRpcService;
    @Autowired
    BillService billService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    ProductStockService productStockService;

    /**
     * 创建库存
     *
     * @param bill
     * @param opt
     */
//    @Transactional(rollbackFor = Exception.class)
//    public RegCreateResultDto create(RegisterBill bill, Optional<OperatorUser> opt) {
//
//        try {
//            // 库存基本信息
//            RegCreateDto createDto = this.buildCreateDtoFromBill(bill, opt, bill.getMarketId());
//            // 远程调用库存接口
//            BaseOutput<RegCreateResultDto> out = this.productRpc.create(createDto);
//
//            if (out.isSuccess() && out.getData() != null) {
//                // 同步完成后更新和溯源库存的关联关系
//                updateStockIdAfterCreate(out, bill.getId());
//                logger.debug("创建库存成功");
//                return out.getData();
//            } else {
//                logger.error("创建库存失败:{}", out.getMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return null;
////        throw new TraceBizException("创建库存失败");
//    }

    /**
     * 扣减库存
     *
     * @param bill
     * @param opt
     */
//    public List<StockReductResultDto> reduceByStockIds(Long stockId, RegisterBill bill, Optional<OperatorUser> opt) {
//        try {
//            //库存基本信息
//            StockReduceRequestDto obj = new StockReduceRequestDto();
//            obj.setFirmId(bill.getMarketId());
//            this.firmRpcService.getFirmById(bill.getMarketId()).ifPresent(firm -> {
//                obj.setFirmName(firm.getName());
//            });
//
//            StockReduceDto stockReduceDto = new StockReduceDto();
//            // 库存id
//            stockReduceDto.setBizId(bill.getId());
//            stockReduceDto.setReduceNum(bill.getWeight());
//            // 业务单据号
//            stockReduceDto.setStockId(stockId);
//            obj.setStockReduceItems(Lists.newArrayList(stockReduceDto));
//
//            BaseOutput<List<StockReductResultDto>> out = this.productRpc.reduceByStockIds(obj);
//            if (out.isSuccess() && out.getData() != null) {
//                logger.debug("扣减库存成功");
//                return out.getData();
//            } else {
//                logger.error("扣减库存失败:{}", out.getMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return Lists.newArrayList();
//        // throw new TraceBizException("扣减库存失败");
//    }

    /**
     * 处理交易单库存
     *
     * @param buyerDetailList
     * @param sellerDetailList
     * @param optUser
     * @param marketId
     */
//    public void handleTradeStocks(List<TradeDetail> buyerDetailList, List<TradeDetail> sellerDetailList,
//                                  Optional<OperatorUser> optUser, Long marketId) {
//
//        // 构建创建库存基础信息
//        List<RegCreateDto> createDtos = buildCreateDtoFromTrade(buyerDetailList, optUser, marketId);
//
//        // 远程调用库存接口
//        StreamEx.of(createDtos).forEach(createDto -> {
//            BaseOutput<RegCreateResultDto> out = this.productRpc.create(createDto);
//            if (out.isSuccess() && out.getData() != null) {
//                List<RegDetailDto> regDetailDtos = out.getData().getRegDetailDtos();
//                if (!CollectionUtils.isEmpty(regDetailDtos)) {
//                    // 同步完成后更新和溯源库存的关联关系
//                    TradeDetail condition = new TradeDetail();
//                    condition.setId(createDto.getTradeDetailId());
//                    condition.setThirdPartyStockId(regDetailDtos.get(0).getStockId());
//                    tradeDetailService.updateSelective(condition);
//                    logger.debug("创建库存成功");
//                } else {
//                    logger.error("创建库存成功，但是未返回StockId");
//                }
//            } else {
//                logger.error("创建库存失败：{}", out.getMessage());
//                //throw new TraceBizException("创建库存失败");
//            }
//        });
//
//        if (sellerDetailList != null && !sellerDetailList.isEmpty()) {
//            // 构建扣减库存基础信息
//            StockReduceRequestDto obj = buildReduceDtoFromTrade(sellerDetailList, optUser, marketId);
//            BaseOutput<List<StockReductResultDto>> out = this.productRpc.reduceByStockIds(obj);
//            if (out.isSuccess() && out.getData() != null) {
//                return;
//            }
//            logger.error("创建库存失败：{}", out.getMessage());
//            return;
//        }
//
//        // throw new TraceBizException("扣减库存失败");
//    }

    /**
     * @param detailList
     * @param optUser
     * @param marketId
     * @return
     */
//    private StockReduceRequestDto buildReduceDtoFromTrade(List<TradeDetail> detailList, Optional<OperatorUser> optUser, Long marketId) {
//
//        if (CollectionUtils.isEmpty(detailList)) {
//            throw new TraceBizException("处理库存失败，因为交易批次为空");
//        }
//
//        StockReduceRequestDto obj = new StockReduceRequestDto();
//        obj.setFirmId(marketId);
//        this.firmRpcService.getFirmById(marketId).ifPresent(firm -> {
//            obj.setFirmName(firm.getName());
//        });
//
//        List<StockReduceDto> reduceDtoList = new ArrayList<>();
//        StreamEx.of(detailList).forEach(d -> {
//            StockReduceDto stockReduceDto = new StockReduceDto();
//            // 业务单据号
//            stockReduceDto.setBizId(d.getBillId());
//            stockReduceDto.setReduceNum(d.getStockWeight());
//            // 库存ID
//            stockReduceDto.setStockId(d.getThirdPartyStockId());
//            reduceDtoList.add(stockReduceDto);
//        });
//
//        obj.setStockReduceItems(reduceDtoList);
//
//        return obj;
//    }


    /**
     * 同步完成 UAP 库存后，溯源本地维护 stockId
     *
     * @param out
     * @param billId
     */
//    private void updateStockIdAfterCreate(BaseOutput<RegCreateResultDto> out, Long billId) {
//        List<RegDetailDto> regDetailDtos = out.getData().getRegDetailDtos();
//        Optional<TradeDetail> tradeDetail = tradeDetailService.findBilledTradeDetailByBillId(billId);
//        tradeDetail.ifPresent(t -> {
//            TradeDetail condition = new TradeDetail();
//            condition.setId(tradeDetail.get().getId());
//            condition.setThirdPartyStockId(regDetailDtos.get(0).getStockId());
//            tradeDetailService.updateSelective(condition);
//        });
//    }

    /**
     * 根据交易批次构建库存创建 DTO
     *
     * @param detailList
     * @param optUser
     * @param marketId
     * @return
     */
//    private List<RegCreateDto> buildCreateDtoFromTrade(List<TradeDetail> detailList, Optional<OperatorUser> optUser, Long marketId) {
//
//        if (CollectionUtils.isEmpty(detailList)) {
//            throw new TraceBizException("处理库存失败，因为交易批次为空");
//        }
//
//        List<RegCreateDto> createDtoList = new ArrayList<>();
//        //库存基本信息
//        StreamEx.of(detailList).forEach(d -> {
//            RegisterBill bill = billService.getAvaiableBill(d.getBillId()).orElseThrow(() -> {
//                return new TraceBizException("处理库存失败，因为报备单" + d.getBillId() + "查询失败");
//            });
//            bill.setId(d.getBillId());
//            bill.setUserId(d.getBuyerId());
//            bill.setName(d.getBuyerName());
//            bill.setWeight(d.getStockWeight());
//            bill.setWeightUnit(d.getWeightUnit());
//            RegCreateDto createDto = this.buildCreateDtoFromBill(bill, optUser, marketId);
//            // 设置溯源批次和UAP批次映射
//            createDto.setTradeDetailId(d.getId());
//            createDtoList.add(createDto);
//        });
//
//        return createDtoList;
//    }

    /**
     * 增加白细美库存
     *
     * @param increaseWeight
     * @param optUser
     */
    public void increaseRegDetail(Long tradeDetailId, Long marketId, BigDecimal increaseWeight, Optional<OperatorUser> optUser) {

        if (tradeDetailId == null) {
            return;
        }
        TradeDetail tradeDetail = this.tradeDetailService.get(tradeDetailId);
        if (tradeDetail == null) {
            return;
        }
        logger.debug("increaseRegDetail tradeDetailId={},thirdPartyStockId={}", tradeDetailId, tradeDetail.getThirdPartyStockId());
        if (tradeDetail.getThirdPartyStockId() == null) {
            return;
        }

        StockReduceDto stockReduceDto = new StockReduceDto();
        // 业务单据号
        stockReduceDto.setBizId(tradeDetailId);
        stockReduceDto.setReduceNum(increaseWeight);
        // 库存ID
        stockReduceDto.setStockId(tradeDetail.getThirdPartyStockId());

        StockReduceRequestDto obj = new StockReduceRequestDto();
        obj.setFirmId(marketId);
        this.firmRpcService.getFirmById(marketId).ifPresent(mk -> {
            obj.setFirmName(mk.getName());
        });


        List<StockReduceDto> reduceDtoList = Lists.newArrayList(stockReduceDto);

        obj.setStockReduceItems(reduceDtoList);


        try {
            logger.debug("incByStockIds obj={}", JSON.toJSONString(obj));
            BaseOutput<List<StockReductResultDto>> out = this.productRpc.incByStockIds(obj);
            if (out.isSuccess()) {
                return;
            }
            logger.error("增加批次库存失败:{}", out.getMessage());
        } catch (Exception e) {
            logger.error("增加批次库存失败:{}", e.getMessage());
        }


    }

    /**
     * 扣减批次库存
     *
     * @param deductWeight
     * @param optUser
     */
    public void deductRegDetail(Long tradeDetailId, Long marketId, BigDecimal deductWeight, Optional<OperatorUser> optUser) {
        if (tradeDetailId == null) {
            return;
        }
        TradeDetail tradeDetail = this.tradeDetailService.get(tradeDetailId);
        if (tradeDetail == null) {
            return;
        }
        logger.debug("deductRegDetail tradeDetailId={},thirdPartyStockId={}", tradeDetailId, tradeDetail.getThirdPartyStockId());
        if (tradeDetail.getThirdPartyStockId() == null) {
            return;
        }
        StockReduceDto stockReduceDto = new StockReduceDto();
        // 业务单据号
        stockReduceDto.setBizId(tradeDetailId);
        stockReduceDto.setReduceNum(deductWeight);
        // 库存ID
        stockReduceDto.setStockId(tradeDetail.getThirdPartyStockId());

        StockReduceRequestDto obj = new StockReduceRequestDto();
        obj.setFirmId(marketId);
        this.firmRpcService.getFirmById(marketId).ifPresent(mk -> {
            obj.setFirmName(mk.getName());
        });

        List<StockReduceDto> reduceDtoList = Lists.newArrayList(stockReduceDto);

        obj.setStockReduceItems(reduceDtoList);


        try {
            logger.debug("reduceByStockIds obj={}", JSON.toJSONString(obj));
            BaseOutput<List<StockReductResultDto>> out = this.productRpc.reduceByStockIds(obj);
            if (out.isSuccess()) {
                return;
            }
            logger.error("扣减批次库存失败:{}", out.getMessage());
        } catch (Exception e) {
            logger.error("增加批次库存失败:{}", e.getMessage());
        }


    }

    /**
     * 为买家创建库存
     *
     * @param buyerTradeDetailId
     * @param buyerMarketId
     * @param optUser
     */
    public void createRegCreate(Long buyerTradeDetailId, Long buyerMarketId, Optional<OperatorUser> optUser) {
        this.buildCreateDtoFromBill(buyerTradeDetailId, buyerMarketId, optUser).ifPresent(createDto -> {

            try {
                logger.debug("create={}", JSON.toJSONString(createDto));
                BaseOutput<RegCreateResultDto> out = this.productRpc.create(createDto);
                if (out.isSuccess() && out.getData() != null && out.getData().getRegDetailDtos() != null) {
                    List<RegDetailDto> regDetailDtoList = out.getData().getRegDetailDtos();
                    RegDetailDto detailDto = StreamEx.of(regDetailDtoList).nonNull().filter(dto -> dto.getStockId() != null).findFirst().orElse(null);
                    if (detailDto != null) {
                        TradeDetail condition = new TradeDetail();
                        condition.setId(createDto.getTradeDetailId());
                        condition.setThirdPartyStockId(detailDto.getStockId());
                        tradeDetailService.updateSelective(condition);
                        logger.debug("createRegCreate tradeDetailId={},thirdPartyStockId={}", condition.getId(), condition.getThirdPartyStockId());
                        return;
                    }
                    logger.error("返回的数据不符合接口");
                    return;
                }
                logger.error("创建库存失败:{}", out.getMessage());
            } catch (Exception e) {
                logger.error("创建库存失败:{}", e.getMessage());
            }


        });

    }

    /**
     * 根据报备单构建库存创建 DTO
     *
     * @param optUser
     * @return
     */
    private Optional<RegCreateDto> buildCreateDtoFromBill(Long buyerTradeDetailId, Long buyerMarketId, Optional<OperatorUser> optUser) {
        TradeDetail tradeDetail = this.tradeDetailService.get(buyerTradeDetailId);
        RegisterBill registerBill = this.billService.get(tradeDetail.getBillId());
        ProductStock buyerProductStock = this.productStockService.get(tradeDetail.getProductStockId());
        if (buyerProductStock == null) {
            return Optional.empty();
        }

        List<RegDetailDto> regDetailDtoList = StreamEx.of(this.buildRegDetailDto(tradeDetail, registerBill, buyerProductStock)).toList();
        if (regDetailDtoList.isEmpty()) {
            return Optional.empty();
        }
        TradeDetail tdQ = new TradeDetail();
        tdQ.setProductStockId(buyerProductStock.getProductStockId());


        RegCreateDto createDto = new RegCreateDto();
        createDto.setTradeDetailId(tradeDetail.getId());
        createDto.setFirmId(buyerMarketId);
        this.firmRpcService.getFirmById(buyerMarketId).ifPresent(firm -> {
            createDto.setFirmName(firm.getName());
        });
        // 库存系统要校验InStockNo字段唯一，传报备单主键交易场景有问题，所以生成个唯一单号
        createDto.setInStockNo(uidRestfulRpcService.bizNumber(BizNumberType.STOCK_CODE));
        if (optUser.isPresent()) {
            createDto.setOperatorId(optUser.get().getId());
            createDto.setOperatorName(optUser.get().getName());
        } else {
            createDto.setOperatorId(buyerProductStock.getUserId());
            createDto.setOperatorName(buyerProductStock.getUserName());
        }
        createDto.setPlateNo(registerBill.getPlate());

        createDto.setRegDetailDtos(regDetailDtoList);
        createDto.setSource(StockRegisterSourceEnum.REG.getCode());

        return Optional.of(createDto);
    }

    /**
     * 构造批次
     *
     * @param tradeDetail
     * @param registerBill
     * @param buyerProductStock
     * @return
     */
    private Optional<RegDetailDto> buildRegDetailDto(TradeDetail tradeDetail, RegisterBill registerBill, ProductStock buyerProductStock) {
        if (buyerProductStock == null) {
            return Optional.empty();
        }

        //库存详情信息
        RegDetailDto detailDto = new RegDetailDto();
        detailDto.setBrand(buyerProductStock.getBrandName());
        detailDto.setSpec(buyerProductStock.getSpecName());
        // 买家增加库存
        detailDto.setCustomerId(buyerProductStock.getUserId());
        detailDto.setCustomerName(buyerProductStock.getUserName());
        detailDto.setCateId(buyerProductStock.getProductId());
        detailDto.setCname(buyerProductStock.getProductName());

        if (WeightUnitEnum.JIN.equalsToCode(registerBill.getWeightUnit())) {
            detailDto.setInUnit(StockInUnitEnum.JIN.getCode());
        } else if (WeightUnitEnum.KILO.equalsToCode(registerBill.getWeightUnit())) {
            detailDto.setInUnit(StockInUnitEnum.KILO.getCode());
        } else {
            detailDto.setInUnit(StockInUnitEnum.PIECE.getCode());
        }
        detailDto.setPlace(registerBill.getOriginName());
        detailDto.setPrice(registerBill.getUnitPrice());
        detailDto.setWeight(tradeDetail.getStockWeight());
        detailDto.setProductId(registerBill.getProductId());
        detailDto.setProductName(registerBill.getProductName());
        return Optional.of(detailDto);
    }


    /**
     * 锁定 or 释放库存
     *
     * @param lockReleaseRequestDto
     * @return
     */
    private void lockOrRelease(LockReleaseRequestDto lockReleaseRequestDto) {
        logger.debug("lockOrRelease={}", JSON.toJSONString(lockReleaseRequestDto));
        BaseOutput<?> baseOutput = productRpc.lockOrRelease(lockReleaseRequestDto);
        if (!baseOutput.isSuccess()) {
            throw new TraceBizException("锁定or释放库存接口失败：" + baseOutput.getMessage());
        }
    }

    /**
     * 锁定库存
     *
     * @param thirdPartyStockId
     * @param marketId
     * @param tradeWeight
     */
    public void lock(Long thirdPartyStockId, Long marketId, BigDecimal tradeWeight) {
        if(this!=null){
            return;
        }
        if (thirdPartyStockId == null) {
            return;
        }
        LockReleaseItem item = new LockReleaseItem(tradeWeight.floatValue(), thirdPartyStockId, 1);
        List<LockReleaseItem> items = new ArrayList<>();
        items.add(item);
        LockReleaseRequestDto lockReleaseRequestDto = new LockReleaseRequestDto(marketId, this.firmRpcService.getFirmById(marketId).get().getName(), items);
        this.lockOrRelease(lockReleaseRequestDto);
    }

    /**
     * 释放库存
     *
     * @param thirdPartyStockId
     * @param marketId
     * @param tradeWeight
     */
    public void release(Long thirdPartyStockId, Long marketId, BigDecimal tradeWeight) {
        if(this!=null){
            return;
        }
        if (thirdPartyStockId == null) {
            return;
        }
        LockReleaseItem item = new LockReleaseItem(tradeWeight.floatValue(), thirdPartyStockId, 0);
        List<LockReleaseItem> items = new ArrayList<>();
        items.add(item);
        LockReleaseRequestDto lockReleaseRequestDto = new LockReleaseRequestDto(marketId, this.firmRpcService.getFirmById(marketId).get().getName(), items);
        this.lockOrRelease(lockReleaseRequestDto);
    }

}
