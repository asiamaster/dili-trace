package com.dili.trace.service;

import cn.hutool.core.date.DateUtil;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.api.input.ProductStockInput;
import com.dili.trace.api.input.TradeDetailInputDto;
import com.dili.trace.api.input.TradeDetailQueryDto;
import com.dili.trace.api.input.TradeRequestHandleDto;
import com.dili.trace.domain.*;
import com.dili.trace.dto.TradeDto;
import com.dili.trace.enums.*;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.ProductRpcService;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * tradeorder服务
 */
@Service
@Transactional
public class TradeOrderService extends BaseServiceImpl<TradeOrder, Long> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TradeOrderService.class);
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    TradeRequestDetailService tradeRequestDetailService;
    @Autowired
    ProductStockService productStockService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    BillService billService;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    ProductRpcService productRpcService;

    /**
     * 检查参数是否正确
     *
     * @param tradeDto
     * @param batchStockInputList
     */
    private void checkInput(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {

        CustomerExtendDto seller = this.customerRpcService.findCustomerById(tradeDto.getSeller().getSellerId(), tradeDto.getMarketId()).orElseThrow(() -> {
            return new TraceBizException("卖家不存在");
        });
        tradeDto.getSeller().setSellerName(seller.getName());

        if (TradeOrderTypeEnum.SEPREATE != tradeDto.getTradeOrderType()) {
            CustomerExtendDto buyer = this.customerRpcService.findCustomerById(tradeDto.getBuyer().getBuyerId(), tradeDto.getMarketId()).orElseThrow(() -> {
                return new TraceBizException("买家不存在");
            });
            tradeDto.getBuyer().setBuyerName(buyer.getName());
            tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        } else {
            if (tradeDto.getBuyer().getBuyerId() != null) {
                UpStream upStream = Optional.ofNullable(this.upStreamService.get(tradeDto.getBuyer().getBuyerId())).orElseThrow(() -> {
                    return new TraceBizException("下游不存在");
                });
                tradeDto.getBuyer().setBuyerId(null);
                tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.DOWNSTREAM_BUYER);
//                tradeDto.getBuyer().setBuyerName(upStream.getName() + "-" + upStream.getId());
                tradeDto.getBuyer().setBuyerName(upStream.getName());
            } else {
                if (StringUtils.isBlank(tradeDto.getBuyer().getBuyerName())) {
                    throw new TraceBizException("分销客户信息不完整");
                }
                tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.OTHERS);

            }

        }


        Long sellerId = tradeDto.getSeller().getSellerId();

        Map<Long, List<Long>> batchIdDetailIdMap = StreamEx.of(CollectionUtils.emptyIfNull(batchStockInputList))
                .nonNull().toMap(batchStockInput -> {
                    return batchStockInput.getProductStockId();
                }, batchStockInput -> {
                    return StreamEx.of(CollectionUtils.emptyIfNull(batchStockInput.getTradeDetailInputList())).nonNull()
                            .map(item -> {
                                return item.getTradeDetailId();
                            }).toList();
                });

        List<Long> batchStockIdList = StreamEx.of(batchIdDetailIdMap.keySet()).nonNull().toList();
        if (batchStockIdList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }
        List<Long> tradeDetailIdList = StreamEx.of(batchIdDetailIdMap.values()).flatMap(List::stream).toList();
        if (!tradeDetailIdList.isEmpty()) {
            List<TradeDetail> tradeDetailList = this.tradeDetailService.findTradeDetailByIdList(tradeDetailIdList);
            // 判断是否全部是卖家的商品
            boolean notBelongToSeller = StreamEx.of(tradeDetailList).map(TradeDetail::getBuyerId).distinct()
                    .anyMatch(uid -> !uid.equals(sellerId));
            if (notBelongToSeller) {
                throw new TraceBizException("参数不匹配:有批次不属于当前卖家");
            }

        }
        LOGGER.debug("sellerId={},batchStockIdList={}", sellerId, batchStockIdList);
        // 判断是否全部是卖家的库存信息
        boolean notSellerOwnedBatchBlock = StreamEx.of(this.productStockService.findByIdList(batchStockIdList))
                .map(ProductStock::getUserId).distinct().anyMatch(uid -> !uid.equals(sellerId));
        if (notSellerOwnedBatchBlock) {
            throw new TraceBizException("参数不匹配:有库存不属于当前卖家");
        }

        // 判断是否全部是卖家当前市场
        boolean notSellerOwnedMarket = StreamEx.of(this.productStockService.findByIdList(batchStockIdList))
                .map(ProductStock::getMarketId).distinct().anyMatch(mid -> !mid.equals(tradeDto.getMarketId()));
        if (notSellerOwnedMarket) {
            throw new TraceBizException("参数不匹配:有库存不属于当前卖家市场");
        }


    }


    /**
     * 处理购买请求
     *
     * @param handleDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleBuyerRequest(TradeRequestHandleDto handleDto) {
        Long tradeRequestId = handleDto.getTradeRequestId();
        TradeRequest tradeRequest = this.tradeRequestService.get(tradeRequestId);
        TradeOrderStatusEnum tradeOrderStatusEnum = TradeOrderStatusEnum.fromCodeOrEx(handleDto.getHandleStatus());
        TradeOrder tradeOrder = this.get(tradeRequest.getTradeOrderId());

        TradeOrderTypeEnum tradeOrderTypeEnum = TradeOrderTypeEnum.fromCodeOrEx(tradeOrder.getOrderType());

        TradeRequest updatableTradeRequest = new TradeRequest();
        updatableTradeRequest.setId(tradeRequest.getId());
        updatableTradeRequest.setOrderStatus(tradeOrderStatusEnum.getCode());
        this.tradeRequestService.updateSelective(updatableTradeRequest);

        this.dealTradeRequest(tradeRequest, tradeOrderStatusEnum, tradeOrderTypeEnum);

    }

    /**
     * 直接创建数据
     *
     * @param registerBill
     * @param productStockId
     * @return
     */
    private TradeDetail createTradeDetailForRegisterBill(RegisterBill registerBill, Long productStockId) {
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBillId(registerBill.getBillId());
        tradeDetail.setParentId(null);
        tradeDetail.setWeightUnit(registerBill.getWeightUnit());
        tradeDetail.setStockWeight(registerBill.getWeight());
        tradeDetail.setTotalWeight(registerBill.getWeight());
        tradeDetail.setSoftWeight(BigDecimal.ZERO);
        tradeDetail.setProductName(registerBill.getProductName());
        tradeDetail.setProductStockId(productStockId);
        tradeDetail.setBuyerId(registerBill.getUserId());
        tradeDetail.setBuyerName(registerBill.getName());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.NONE.getCode());
        this.tradeDetailService.insertSelective(tradeDetail);

        return tradeDetail;

    }

    /**
     * SB
     *
     * @param registerBill
     * @return
     */
    public Optional<TradeDetail> createTradeFromRegisterBill(RegisterBill registerBill) {
        TradeDetail tdq = new TradeDetail();
        tdq.setBillId(registerBill.getBillId());
        tdq.setTradeType(TradeTypeEnum.NONE.getCode());
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tdq);
        TradeDetail tradeDetail = StreamEx.of(tradeDetailList).findFirst().orElse(null);
        if (tradeDetail != null) {
            return Optional.ofNullable(tradeDetail);
        }

        ProductStock productStock = this.createOrFindProductStock(registerBill, registerBill.getUserId(), registerBill.getName()).orElseThrow(() -> {
            return new TraceBizException("创建/查询卖家库存失败");
        });

        TradeDetail buyerTD = this.createTradeDetailForRegisterBill(registerBill, productStock.getProductStockId());


//        TradeDto tradeDto = new TradeDto();
//        tradeDto.setTradeOrderType(TradeOrderTypeEnum.NONE);
//        tradeDto.setMarketId(registerBill.getMarketId());
//        tradeDto.getBuyer().setBuyerName(registerBill.getName());
//        tradeDto.getBuyer().setBuyerId(registerBill.getUserId());
//        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);


//        ProductStock updatablePS = new ProductStock();
//        updatablePS.setId(productStock.getId());
//        updatablePS.setStockWeight(productStock.getStockWeight().add(registerBill.getWeight()));
//        updatablePS.setTradeDetailNum(productStock.getTradeDetailNum() + 1);
//        this.productStockService.updateSelective(updatablePS);
//        this.productStockService.updateProductStock(productStock.getProductStockId());

//        ProductStockInput psInput = new ProductStockInput();
//        psInput.setTradeWeight(registerBill.getWeight());
//        psInput.setProductStockId(productStock.getProductStockId());
//
//
//        List<TradeDetailInputDto> tradeDetailInputList = new ArrayList<>();
//        psInput.setTradeDetailInputList(tradeDetailInputList);
//
//        TradeDetailInputDto tradeDetailInputDto = new TradeDetailInputDto();
//        tradeDetailInputDto.setBillId(registerBill.getBillId());
//        tradeDetailInputDto.setTradeWeight(registerBill.getWeight());
//        tradeDetailInputList.add(tradeDetailInputDto);
//
//        this.checkInput(tradeDto, Arrays.asList(psInput));
//
//        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);
//
//
//        List<TradeRequest> tradeRequestList = StreamEx.of(psInput).map(productStockInput -> {
//            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
//            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailForInput(productStockInput.getTradeDetailInputList(), tradeRequest);
//            return tradeRequest;
//        }).toList();
//
//        this.dealTradeOrder(tradeOrder, TradeOrderStatusEnum.FINISHED, tradeRequestList);
//        return Optional.of(tradeOrder);
        this.productRpcService.createRegCreate(buyerTD.getId(), productStock.getMarketId(), Optional.empty());
        return Optional.ofNullable(buyerTD);
    }

    /**
     * create trade
     *
     * @param tradeDto
     * @param batchStockInputList
     */
    public TradeOrder createSeperateTrade(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {
        List<ProductStockInput> productStockInputList = StreamEx.ofNullable(batchStockInputList).flatCollection(Function.identity())
                .nonNull().toList();
        if (productStockInputList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }
        this.checkInput(tradeDto, batchStockInputList);
        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);

        List<TradeRequest> tradeRequestList = StreamEx.of(productStockInputList).map(productStockInput -> {
            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailList(tradeRequest, productStockInput.getTradeDetailInputList());
            return tradeRequest;
        }).toList();
        this.dealTradeOrder(tradeOrder, TradeOrderStatusEnum.FINISHED, tradeRequestList);
        return tradeOrder;
    }

    /**
     * create trade
     *
     * @param tradeDto
     * @param batchStockInputList
     */
    public TradeOrder createSellTrade(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {
        this.checkInput(tradeDto, batchStockInputList);
        List<ProductStockInput> productStockInputList = StreamEx.ofNullable(batchStockInputList).flatCollection(Function.identity())
                .nonNull().toList();
        if (productStockInputList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }
        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);

        List<TradeRequest> tradeRequestList = StreamEx.of(productStockInputList).map(productStockInput -> {
            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailList(tradeRequest, productStockInput.getTradeDetailInputList());
            return tradeRequest;
        }).toList();
        this.dealTradeOrder(tradeOrder, TradeOrderStatusEnum.FINISHED, tradeRequestList);
        return tradeOrder;
    }

    /**
     * create trade
     *
     * @param tradeDto
     * @param batchStockInputList
     */
    public TradeOrder createBuyTrade(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {
        TradeDto.Buyer buyer = tradeDto.getBuyer();
        this.customerRpcService.findApprovedCustomerByIdOrEx(buyer.getBuyerId(), tradeDto.getMarketId(), "用户未审核通过不能进行购买");
        List<ProductStockInput> productStockInputList = StreamEx.ofNullable(batchStockInputList).flatCollection(Function.identity())
                .nonNull().filter(pin -> {
                    return pin.getProductStockId() != null;
                }).toList();
        if (productStockInputList.isEmpty()) {
            LOGGER.error("productStockInputList.isEmpty={}", productStockInputList.isEmpty());
            throw new TraceBizException("参数错误");
        }
        List<Long> productStockIdList = StreamEx.of(productStockInputList).map(ProductStockInput::getProductStockId).distinct().nonNull().toList();

        boolean ownedByOneSeller = StreamEx.of(this.productStockService.findByIdList(productStockIdList)).map(ProductStock::getUserId).distinct().count() == 1;
        if (!ownedByOneSeller) {
            throw new TraceBizException("所要购买的商品不属于同一个卖家");
        }
        Long productStockId = productStockIdList.get(0);
        ProductStock productStock = this.productStockService.get(productStockId);

        tradeDto.getSeller().setSellerId(productStock.getUserId());
        tradeDto.getSeller().setSellerName(productStock.getUserName());
        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);

        List<TradeRequest> tradeRequestList = StreamEx.of(productStockInputList).map(productStockInput -> {
            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailList(tradeRequest, productStockInput.getTradeDetailInputList());
            return tradeRequest;
        }).toList();
        return tradeOrder;
    }

    /**
     * deal trade order
     *
     * @param tradeOrderItem
     * @param tradeOrderStatusEnum
     * @param tradeRequestList
     */
    protected void dealTradeOrder(TradeOrder tradeOrderItem, TradeOrderStatusEnum tradeOrderStatusEnum, List<TradeRequest> tradeRequestList) {

        if (TradeOrderStatusEnum.NONE == tradeOrderStatusEnum) {
            return;
        }
        TradeOrderTypeEnum tradeOrderTypeEnum = TradeOrderTypeEnum.fromCodeOrEx(tradeOrderItem.getOrderType());

        for (TradeRequest tradeRequest : tradeRequestList) {
            if (tradeOrderStatusEnum.equalsToCode(tradeRequest.getOrderStatus())) {
                continue;
            }
            TradeRequest updatableTradeRequest = new TradeRequest();
            updatableTradeRequest.setId(tradeRequest.getId());
            updatableTradeRequest.setOrderStatus(tradeOrderStatusEnum.getCode());
            this.tradeRequestService.updateSelective(updatableTradeRequest);
            this.dealTradeRequest(tradeRequest, tradeOrderStatusEnum
                    , tradeOrderTypeEnum);
        }


    }

    /**
     * 处理交易数据
     *
     * @param tradeRequest
     * @param trd
     * @param tradeOrderTypeEnum
     */
    private void dealTradeRequestDetail(TradeRequest tradeRequest, TradeRequestDetail trd, TradeOrderTypeEnum tradeOrderTypeEnum) {


        TradeDetail sellerTD = this.tradeDetailService.get(trd.getTradeDetailId());
        RegisterBill registerBill = this.billService.get(sellerTD.getBillId());
//        if (TradeOrderTypeEnum.NONE != tradeOrderTypeEnum) {
//            registerBill = this.billService.get(sellerTD.getBillId());
//        }

//        if (sellerTD != null) {
        LOGGER.info("seller tradedetail id={},stockweight={}", sellerTD.getId(), sellerTD.getStockWeight());
//        }
        TradeDetail buyerTD = this.createTradeDetail(registerBill, tradeRequest);

        TradeDetail updatableBuyerTD = new TradeDetail();
        updatableBuyerTD.setId(buyerTD.getId());
        updatableBuyerTD.setStockWeight(trd.getTradeWeight());
        updatableBuyerTD.setTotalWeight(trd.getTradeWeight());
        updatableBuyerTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());

        //分销时候buyerProductStock为空
        Optional<ProductStock> buyerProductStock = this.createOrFindProductStock(registerBill, tradeRequest.getBuyerId(), tradeRequest.getBuyerName());
        buyerProductStock.ifPresent(bp -> {
            updatableBuyerTD.setProductStockId(bp.getProductStockId());
            LOGGER.debug("buyerProductStock id={},stockweight={},tradeweight={}", bp.getId(), bp.getStockWeight(), trd.getTradeWeight());
        });

        LOGGER.debug("seller id={},name={}", sellerTD.getBuyerId(), sellerTD.getBuyerName());
        updatableBuyerTD.setBatchNo(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        updatableBuyerTD.setParentBatchNo(this.tradeDetailService.buildParentBatchNo(sellerTD));
        updatableBuyerTD.setCheckinRecordId(sellerTD.getCheckinRecordId());
        updatableBuyerTD.setCheckoutRecordId(sellerTD.getCheckoutRecordId());
        updatableBuyerTD.setCheckinStatus(sellerTD.getCheckinStatus());
        updatableBuyerTD.setCheckoutStatus(sellerTD.getCheckoutStatus());
        updatableBuyerTD.setTradeType(TradeTypeEnum.SEPARATE_SALES.getCode());
        updatableBuyerTD.setParentId(sellerTD.getId());
        LOGGER.info("buyer tradedetail id={},stockweight={}", updatableBuyerTD.getId(), updatableBuyerTD.getStockWeight());
        this.tradeDetailService.updateSelective(updatableBuyerTD);
        this.productStockService.updateProductStock(updatableBuyerTD.getProductStockId());

        ProductStock sellerProductStock = this.createOrFindProductStock(registerBill, sellerTD.getBuyerId(), sellerTD.getBuyerName()).orElseThrow(() -> {
            return new TraceBizException("创建/查询卖家库存失败");
        });
        LOGGER.debug("sellerProductStock id={},sellerid={},stockweight={},tradeweight={}", sellerProductStock.getId(), sellerProductStock.getUserId(), sellerProductStock.getStockWeight(), trd.getTradeWeight());

        TradeDetail updatableSellerTD = new TradeDetail();
        updatableSellerTD.setId(sellerTD.getId());
        updatableSellerTD.setSoftWeight(sellerTD.getSoftWeight().subtract(trd.getTradeWeight()));
        LOGGER.info("seller.id={},stockWeight={},softWeight={}", sellerTD.getId(), sellerTD.getStockWeight(), updatableSellerTD.getSoftWeight());

        this.tradeDetailService.updateSelective(updatableSellerTD);
        this.productStockService.updateProductStock(sellerTD.getProductStockId());

        this.productRpcService.createRegCreate(updatableBuyerTD.getId(), tradeRequest.getBuyerMarketId(), Optional.empty());
        this.productRpcService.release(sellerTD.getThirdPartyStockId(), sellerProductStock.getMarketId(), trd.getTradeWeight());
        this.productRpcService.deductRegDetail(sellerTD.getTradeDetailId(), tradeRequest.getSellerMarketId(), trd.getTradeWeight(), Optional.empty());

    }


    /**
     * 处理
     *
     * @param tradeRequest
     * @param tradeOrderStatusEnum
     * @param tradeOrderTypeEnum
     */
    private void dealTradeRequest(TradeRequest tradeRequest, TradeOrderStatusEnum tradeOrderStatusEnum, TradeOrderTypeEnum tradeOrderTypeEnum) {
        List<TradeRequestDetail> tradeRequestDetailList = this.findTradeRequestDetailList(tradeRequest);

        for (TradeRequestDetail trd : tradeRequestDetailList) {
            if (TradeOrderStatusEnum.FINISHED == tradeOrderStatusEnum) {
                this.dealTradeRequestDetail(tradeRequest, trd, tradeOrderTypeEnum);
            } else if (TradeOrderStatusEnum.CANCELLED == tradeOrderStatusEnum) {
                TradeDetail sellerTDItem = this.tradeDetailService.get(trd.getTradeDetailId());
                ProductStock productStock = this.productStockService.get(sellerTDItem.getProductStockId());


                //还原批次库存重量
                TradeDetail td = new TradeDetail();
                td.setId(sellerTDItem.getId());
                td.setStockWeight(sellerTDItem.getStockWeight().add(trd.getTradeWeight()));
                td.setSoftWeight(sellerTDItem.getSoftWeight().subtract(trd.getTradeWeight()));
                if (sellerTDItem.getStockWeight().compareTo(BigDecimal.ZERO) == 0) {
                    //对总库存批次数量还原,对批次状态还原
                    td.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                }
                this.tradeDetailService.updateSelective(td);

                this.productStockService.updateProductStock(sellerTDItem.getProductStockId());

                this.productRpcService.release(sellerTDItem.getThirdPartyStockId(), productStock.getMarketId(), trd.getTradeWeight());
            } else {

            }

        }


    }

    /**
     * 创建TradeOrder
     *
     * @return
     */
    public TradeOrder createTradeOrder(TradeDto tradeDto) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setBuyerId(tradeDto.getBuyer().getBuyerId());
        tradeOrder.setBuyerName(tradeDto.getBuyer().getBuyerName());
        tradeOrder.setBuyerMarketId(tradeDto.getMarketId());
        tradeOrder.setBuyerType(tradeDto.getBuyer().getBuyerType().getCode());

        tradeOrder.setSellerId(tradeDto.getSeller().getSellerId());
        tradeOrder.setSellerName(tradeDto.getSeller().getSellerName());
        tradeOrder.setSellerMarketId(tradeDto.getMarketId());
//        tradeOrder.setOrderStatus(TradeOrderStatusEnum.NONE.getCode());
        tradeOrder.setOrderType(tradeDto.getTradeOrderType().getCode());
        this.insertSelective(tradeOrder);
        return tradeOrder;
    }

    /* *//**
     * @param tradeDetailInputList
     *//*
    private List<TradeRequestDetail> createTradeRequestDetailForInput(List<TradeDetailInputDto> tradeDetailInputList, TradeRequest tradeRequest) {
        List<TradeDetailInputDto> detailInputList = StreamEx.ofNullable(tradeDetailInputList).flatCollection(Function.identity())
                .nonNull().toList();

        boolean invalidTradeWeight = StreamEx.of(detailInputList).anyMatch(trinput -> {
            return trinput.getTradeWeight() == null || BigDecimal.ZERO.compareTo(trinput.getTradeWeight()) >= 0;
        });
        if (invalidTradeWeight) {
            throw new TraceBizException("购买批次重量不能为空或小于0");
        }
        ProductStock productStockItem = this.productStockService.selectByIdForUpdate(tradeRequest.getProductStockId())
                .orElseThrow(() -> {
                    return new TraceBizException("操作库存失败");
                });
        BigDecimal totalTradeWeight = tradeRequest.getTradeWeight();

        if (productStockItem.getStockWeight().compareTo(totalTradeWeight) < 0) {
            throw new TraceBizException("购买重量不能超过总库存重量");
        }
        List<TradeRequestDetail> tradeRequestDetailList = new ArrayList<>();

        StreamEx.of(detailInputList).forEach(trinput -> {
            TradeRequestDetail tradeRequestDetail = new TradeRequestDetail();
            tradeRequestDetail.setTradeRequestId(tradeRequest.getTradeRequestId());
            TradeDetail tradeDetail = new TradeDetail();
            tradeDetail.setId(trinput.getTradeDetailId());

            if (trinput.getTradeDetailId() != null) {
                TradeDetail td = this.tradeDetailService.get(trinput.getTradeDetailId());
                if (td == null) {
                    throw new TraceBizException("批次数据不存在");
                }
                if (td.getStockWeight().compareTo(trinput.getTradeWeight()) < 0) {
                    throw new TraceBizException("购买[" + td.getProductName() + "]重量不能超过批次库存重量");
                }
                tradeRequestDetail.setTradeDetailId(td.getTradeDetailId());
                tradeRequestDetail.setBillId(td.getBillId());

                tradeDetail.setSoftWeight(td.getSoftWeight().add(trinput.getTradeWeight()));
                tradeDetail.setStockWeight(td.getStockWeight().subtract(trinput.getTradeWeight()));
            } else {
                throw new TraceBizException("批次参数错误");
            }
            tradeRequestDetail.setTradeWeight(trinput.getTradeWeight());
            if (tradeDetail.getStockWeight().compareTo(BigDecimal.ZERO) == 0) {
                tradeDetail.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
            } else {
                tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
            }
            this.tradeRequestDetailService.insertSelective(tradeRequestDetail);

            this.tradeDetailService.updateSelective(tradeDetail);

            tradeRequestDetailList.add(tradeRequestDetail);
        });
        this.productStockService.updateProductStock(productStockItem.getProductStockId());

        return tradeRequestDetailList;
    }*/

    /**
     * @param tradeRequest
     * @param tradeRequest
     * @return
     */
    public List<TradeRequestDetail> createTradeRequestDetailList(TradeRequest tradeRequest, List<TradeDetailInputDto> tradeDetailInputList) {
        if (tradeRequest == null || tradeRequest.getId() == null) {
            throw new TraceBizException("交易请求参数不能为空");
        }
        List<TradeRequestDetail> tradeRequestDetailList = this.tradeRequestDetailService.findByTradeRequestIdList(Arrays.asList(tradeRequest.getId()));
        if (!tradeRequestDetailList.isEmpty()) {
            return tradeRequestDetailList;
        }

        BigDecimal remainTradingWeight = tradeRequest.getTradeWeight();
        if (remainTradingWeight == null) {
            throw new TraceBizException("交易重量参数错误");
        }
        if (BigDecimal.ZERO.compareTo(remainTradingWeight) >= 0) {
            throw new TraceBizException("购买重量要大于0");
        }
        tradeDetailInputList = StreamEx.ofNullable(tradeDetailInputList).flatCollection(Function.identity()).nonNull()

                .toList();
        boolean hasInvalidTradeDetailId = StreamEx.of(tradeDetailInputList).anyMatch(input -> input.getTradeDetailId() == null);
        if (hasInvalidTradeDetailId) {
            throw new TraceBizException("交易批次ID参数错误");
        }
        boolean hasInvalidTradeWeight = StreamEx.of(tradeDetailInputList).anyMatch(input -> input.getTradeWeight() == null
                || BigDecimal.ZERO.compareTo(input.getTradeWeight()) >= 0);
        if (hasInvalidTradeWeight) {
            throw new TraceBizException("交易重量参数错误");
        }

        List<Long> tradeDetailIdList = StreamEx.of(tradeDetailInputList).map(TradeDetailInputDto::getTradeDetailId).distinct().toList();
        if (tradeDetailIdList.size() != tradeDetailInputList.size()) {
            throw new TraceBizException("交易批次ID重复");
        }
        BigDecimal totalTradeWeight = StreamEx.of(tradeDetailInputList).map(TradeDetailInputDto::getTradeWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, TradeDetailInputDto> tradeDetailIdInputDtoMap = StreamEx.of(tradeDetailInputList).toMap(TradeDetailInputDto::getTradeDetailId, Function.identity());

        if (!tradeDetailInputList.isEmpty()) {
            if (BigDecimal.ZERO.compareTo(totalTradeWeight) >= 0) {
                throw new TraceBizException("交易重量参数错误");
            }
            if (tradeRequest.getTradeWeight().compareTo(totalTradeWeight) != 0) {
                throw new TraceBizException("交易重量参数错误");
            }
        }

        ProductStock productStockItem = this.productStockService.selectByIdForUpdate(tradeRequest.getProductStockId())
                .orElseThrow(() -> {
                    return new TraceBizException("操作库存失败");
                });
        if (productStockItem.getStockWeight().compareTo(remainTradingWeight) < 0) {
            throw new TraceBizException("购买重量不能超过总库存重量");
        }
        TradeDetailQueryDto tradeDetailQuery = new TradeDetailQueryDto();
        tradeDetailQuery.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetailQuery.setProductStockId(tradeRequest.getProductStockId());
        tradeDetailQuery.setSort("created,id");
        tradeDetailQuery.setOrder("asc,asc");
        tradeDetailQuery.setMinStockWeight(BigDecimal.ZERO);
        tradeDetailQuery.setIdList(tradeDetailIdList);
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);

        if (tradeDetailList.size() < tradeDetailIdList.size()) {
            throw new TraceBizException("不能出售非正常库存商品");
        }
        for (TradeDetail tradeDetail : tradeDetailList) {
            if (BigDecimal.ZERO.compareTo(remainTradingWeight) >= 0) {
                break;
            }
            BigDecimal subWeight = BigDecimal.ZERO;

            if (tradeDetailIdInputDtoMap.containsKey(tradeDetail.getId())) {
                subWeight = tradeDetailIdInputDtoMap.get(tradeDetail.getId()).getTradeWeight();
                if (subWeight.compareTo(tradeDetail.getStockWeight()) > 0) {
                    throw new TraceBizException("批次重量不足,不能扣减成功");
                }
            } else {
                if (remainTradingWeight.compareTo(tradeDetail.getStockWeight()) >= 0) {
                    subWeight = tradeDetail.getStockWeight();
                } else {
                    subWeight = remainTradingWeight;
                }
            }

            TradeRequestDetail tradeRequestDetail = new TradeRequestDetail();
            tradeRequestDetail.setTradeDetailId(tradeDetail.getTradeDetailId());
            tradeRequestDetail.setTradeRequestId(tradeRequest.getTradeRequestId());
            tradeRequestDetail.setTradeWeight(subWeight);

            remainTradingWeight = remainTradingWeight.subtract(tradeRequestDetail.getTradeWeight());

            TradeDetail td = new TradeDetail();
            td.setId(tradeDetail.getId());
            td.setStockWeight(tradeDetail.getStockWeight().subtract(tradeRequestDetail.getTradeWeight()));
            td.setSoftWeight(tradeDetail.getSoftWeight().add(tradeRequestDetail.getTradeWeight()));
            if (td.getStockWeight().compareTo(BigDecimal.ZERO) == 0) {
                td.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
            } else {
                td.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
            }

            this.tradeRequestDetailService.insertSelective(tradeRequestDetail);

            this.tradeDetailService.updateSelective(td);
            tradeRequestDetailList.add(tradeRequestDetail);

            this.productRpcService.lock(tradeDetail.getThirdPartyStockId(), productStockItem.getMarketId(), tradeRequestDetail.getTradeWeight());

        }
        this.productStockService.updateProductStock(productStockItem.getId());
        return tradeRequestDetailList;
    }

//    /**
//     * @param tradeRequest
//     * @param tradeRequest
//     * @return
//     */
//    public List<TradeRequestDetail> createTradeRequestDetail(TradeRequest tradeRequest) {
//
//        ProductStock productStockItem = this.productStockService.selectByIdForUpdate(tradeRequest.getProductStockId())
//                .orElseThrow(() -> {
//                    return new TraceBizException("操作库存失败");
//                });
//        BigDecimal remainTradingWeight = tradeRequest.getTradeWeight();
//        if (BigDecimal.ZERO.compareTo(remainTradingWeight) >= 0) {
//            throw new TraceBizException("购买重量要大于0");
//        }
//        if (productStockItem.getStockWeight().compareTo(remainTradingWeight) < 0) {
//            throw new TraceBizException("购买重量不能超过总库存重量");
//        }
//        List<TradeRequestDetail> tradeRequestDetailList = this.tradeRequestDetailService.findByTradeRequestIdList(Arrays.asList(tradeRequest.getId()));
//
//
//        if (!tradeRequestDetailList.isEmpty()) {
//            return tradeRequestDetailList;
//        }
//
//        TradeDetailQueryDto tradeDetailQuery = new TradeDetailQueryDto();
//        tradeDetailQuery.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
//        tradeDetailQuery.setProductStockId(productStockItem.getId());
//        tradeDetailQuery.setSort("created,id");
//        tradeDetailQuery.setOrder("asc,asc");
//        tradeDetailQuery.setMinStockWeight(BigDecimal.ZERO);
//
//        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
//        for (TradeDetail tradeDetail : tradeDetailList) {
//            if (BigDecimal.ZERO.compareTo(remainTradingWeight) >= 0) {
//                break;
//            }
//
//            TradeRequestDetail tradeRequestDetail = new TradeRequestDetail();
//            tradeRequestDetail.setTradeDetailId(tradeDetail.getTradeDetailId());
//            tradeRequestDetail.setTradeRequestId(tradeRequest.getTradeRequestId());
//
//            if (remainTradingWeight.compareTo(tradeDetail.getStockWeight()) >= 0) {
//                tradeRequestDetail.setTradeWeight(tradeDetail.getStockWeight());
//            } else {
//                tradeRequestDetail.setTradeWeight(remainTradingWeight);
//            }
//
//            remainTradingWeight = remainTradingWeight.subtract(tradeRequestDetail.getTradeWeight());
//
//            TradeDetail td = new TradeDetail();
//            td.setId(tradeDetail.getId());
//            td.setStockWeight(tradeDetail.getStockWeight().subtract(tradeRequestDetail.getTradeWeight()));
//            if (td.getStockWeight().compareTo(BigDecimal.ZERO) == 0) {
//                td.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
//            } else {
//                td.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
//            }
//            td.setSoftWeight(tradeDetail.getSoftWeight().add(tradeRequestDetail.getTradeWeight()));
//            this.tradeRequestDetailService.insertSelective(tradeRequestDetail);
//
//            this.tradeDetailService.updateSelective(td);
//            tradeRequestDetailList.add(tradeRequestDetail);
//
//            this.productRpcService.lock(tradeDetail.getThirdPartyStockId(), productStockItem.getMarketId(), tradeRequestDetail.getTradeWeight());
//
//        }
//
////        this.productStockService.updateSelective(productStock);
//        this.productStockService.updateProductStock(productStockItem.getId());
//        return tradeRequestDetailList;
//    }

    /**
     * @param tradeRequest
     * @param tradeRequest
     * @return
     */
    public List<TradeRequestDetail> findTradeRequestDetailList(TradeRequest tradeRequest) {

        List<TradeRequestDetail> tradeRequestDetailList = this.tradeRequestDetailService.findByTradeRequestIdList(Arrays.asList(tradeRequest.getId()));

        return tradeRequestDetailList;

    }

    /**
     * createOrFind
     *
     * @return
     */
    private Optional<ProductStock> createOrFindProductStock(RegisterBill registerBill, Long userId, String userName) {
        if (userId == null) {
            return Optional.empty();
        }
        ProductStock psQuery = new ProductStock();
        psQuery.setUserId(userId);
        psQuery.setMarketId(registerBill.getMarketId());
        // query.setPreserveType(tradeDetailItem.getPreserveType());
        psQuery.setProductId(registerBill.getProductId());
        psQuery.setWeightUnit(registerBill.getWeightUnit());
        psQuery.setSpecName(registerBill.getSpecName());
        psQuery.setBrandId(registerBill.getBrandId());


        ProductStock batchStockItem = StreamEx.of(this.productStockService.listByExample(psQuery)).findFirst().orElseGet(() -> {
            //创建初始BatchStock
            ProductStock batchStock = new ProductStock();
            batchStock.setMarketId(registerBill.getMarketId());
            batchStock.setUserId(userId);
            batchStock.setUserName(userName);
            batchStock.setPreserveType(registerBill.getPreserveType());
            batchStock.setProductId(registerBill.getProductId());
            batchStock.setProductName(registerBill.getProductName());
            batchStock.setStockWeight(BigDecimal.ZERO);
            batchStock.setTotalWeight(BigDecimal.ZERO);
            batchStock.setWeightUnit(registerBill.getWeightUnit());
            batchStock.setSpecName(registerBill.getSpecName());
            batchStock.setBrandId(registerBill.getBrandId());
            batchStock.setBrandName(registerBill.getBrandName());
            batchStock.setTradeDetailNum(0);
            this.productStockService.insertSelective(batchStock);
            return batchStock;

        });
        return Optional.of(batchStockItem);
    }


    /**
     * 创建TradeRequest
     *
     * @param tradeDto
     * @param tradeOrderItem
     * @return
     */
    public TradeRequest createTradeRequest(TradeDto tradeDto, ProductStockInput input, TradeOrder tradeOrderItem, BigDecimal tradeWeight) {
        TradeDto.Seller seller = tradeDto.getSeller();
        TradeDto.Buyer buyer = tradeDto.getBuyer();


        if (input.getTradeWeight() == null || BigDecimal.ZERO.compareTo(input.getTradeWeight()) >= 0) {
            throw new TraceBizException("购买重量不能小于0");
        }


        if (input.getTradeWeight() == null || input.getTradeWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TraceBizException("购买总重量不能为空或小于0");
        }

        if (input.getProductStockId() == null) {
            throw new TraceBizException("购买商品ID不能为空");
        }
        ProductStock productStock = this.productStockService.get(input.getProductStockId());
        if (productStock == null) {
            throw new TraceBizException("购买商品不存在");
        }
        if (!seller.getSellerId().equals(productStock.getUserId())) {
            throw new TraceBizException("没有权限销售当前商品");
        }


        Date now = new Date();

        TradeRequest tradeRequest = new TradeRequest();

        tradeRequest.setAmount(BigDecimal.ZERO);
        tradeRequest.setBatchNo(null);
        tradeRequest.setCode(this.uidRestfulRpcService.nextTradeRequestCode());

        tradeRequest.setSellerId(seller.getSellerId());
        tradeRequest.setSellerMarketId(tradeDto.getMarketId());
        tradeRequest.setSellerName(seller.getSellerName());
        tradeRequest.setSellerNo(null);
        tradeRequest.setCreated(now);
        tradeRequest.setModified(now);
        tradeRequest.setNumber(0);
        tradeRequest.setPackageNumber(0);
        tradeRequest.setOrderStatus(TradeOrderStatusEnum.NONE.getCode());
        tradeRequest.setWeightUnit(productStock.getWeightUnit());
        tradeRequest.setOrderDate(now);

        tradeRequest.setTradeWeight(tradeWeight);
        tradeRequest.setProductStockId(productStock.getProductStockId());
//        tradeRequest.setOriginName(batchStock.getOriginName());
        tradeRequest.setProductName(productStock.getProductName());
        tradeRequest.setTradeOrderId(tradeOrderItem.getTradeOrderId());

        tradeRequest.setBuyerId(buyer.getBuyerId());
        tradeRequest.setBuyerMarketId(tradeDto.getMarketId());
        tradeRequest.setBuyerName(buyer.getBuyerName());
        tradeRequest.setBuyerNo(null);

        tradeRequest.setReturnStatus(TradeReturnStatusEnum.NONE.getCode());

        this.tradeRequestService.insertSelective(tradeRequest);

        return tradeRequest;
    }

    /**
     * 创建交易详情
     *
     * @param registerBillItem
     * @param tradeRequest
     * @return
     */
    public TradeDetail createTradeDetail(RegisterBill registerBillItem, TradeRequest tradeRequest) {

        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setSoftWeight(BigDecimal.ZERO);
        tradeDetail.setStockWeight(BigDecimal.ZERO);
        tradeDetail.setTotalWeight(BigDecimal.ZERO);
        tradeDetail.setBuyerId(tradeRequest.getBuyerId());
        tradeDetail.setBuyerName(tradeRequest.getBuyerName());

        tradeDetail.setSellerId(tradeRequest.getSellerId());
        tradeDetail.setSellerName(tradeRequest.getSellerName());
        tradeDetail.setCreated(new Date());
        tradeDetail.setModified(new Date());
        tradeDetail.setWeightUnit(registerBillItem.getWeightUnit());
        tradeDetail.setBillId(registerBillItem.getBillId());
        tradeDetail.setProductName(registerBillItem.getProductName());
        tradeDetail.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
//        tradeDetail.setProductStockId(null);
        tradeDetail.setTradeRequestId(tradeRequest.getTradeRequestId());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.NONE.getCode());
        this.tradeDetailService.insertSelective(tradeDetail);
        return tradeDetail;
    }

}