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
import com.dili.trace.dto.TradeDetailInputWrapperDto;
import com.dili.trace.dto.TradeDto;
import com.dili.trace.dto.TradeRequestWrapperDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.ProductRpcService;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.google.common.collect.Lists;
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

        if (TradeOrderTypeEnum.NONE != tradeDto.getTradeOrderType()) {
            CustomerExtendDto seller = customerRpcService.findCustomerById(tradeDto.getSeller().getSellerId(), tradeDto.getMarketId()).orElseThrow(() -> {
                return new TraceBizException("卖家不存在");
            });
            tradeDto.getSeller().setSellerName(seller.getName());
        }

        if (TradeOrderTypeEnum.SEPREATE != tradeDto.getTradeOrderType()) {
            CustomerExtendDto buyer = customerRpcService.findCustomerById(tradeDto.getBuyer().getBuyerId(), tradeDto.getMarketId()).orElseThrow(() -> {
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
                tradeDto.getBuyer().setBuyerName(upStream.getName() + "-" + upStream.getId());
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
        if (TradeOrderTypeEnum.NONE != tradeDto.getTradeOrderType()) {
            // 判断是否全部是卖家的库存信息
            boolean notSellerOwnedBatchBlock = StreamEx.of(this.productStockService.findByIdList(batchStockIdList))
                    .map(ProductStock::getUserId).distinct().anyMatch(uid -> !uid.equals(sellerId));
            if (notSellerOwnedBatchBlock) {
                throw new TraceBizException("参数不匹配:有库存不属于当前卖家");
            }
        }


    }

    /**
     * 创建TradeOrder
     *
     * @param tradeOrderStatusEnum
     * @return
     */
    public TradeOrder createTradeOrder(TradeDto tradeDto, TradeOrderStatusEnum tradeOrderStatusEnum) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setBuyerId(tradeDto.getBuyer().getBuyerId());
        tradeOrder.setBuyerName(tradeDto.getBuyer().getBuyerName());
        tradeOrder.setBuyerMarketId(tradeDto.getMarketId());
        tradeOrder.setBuyerType(tradeDto.getBuyer().getBuyerType().getCode());

        tradeOrder.setSellerId(tradeDto.getSeller().getSellerId());
        tradeOrder.setSellerName(tradeDto.getSeller().getSellerName());
        tradeOrder.setSellerMarketId(tradeDto.getMarketId());
//        tradeOrder.setOrderStatus(tradeOrderStatusEnum.getCode());
        tradeOrder.setOrderType(tradeDto.getTradeOrderType().getCode());
        this.insertSelective(tradeOrder);
        return tradeOrder;
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
        TradeOrderStatusEnum tradeOrderStatusEnum = TradeOrderStatusEnum.fromCode(handleDto.getHandleStatus()).orElseThrow(() -> {
            return new TraceBizException("状态值错误");
        });
        TradeOrder tradeOrder = this.get(tradeRequest.getTradeOrderId());

        TradeOrderTypeEnum tradeOrderTypeEnum = TradeOrderTypeEnum.fromCode(tradeOrder.getOrderType()).orElseThrow(() -> {
            return new TraceBizException("类型错误");
        });

        TradeRequest updatableTradeRequest = new TradeRequest();
        updatableTradeRequest.setId(tradeRequest.getId());
        updatableTradeRequest.setOrderStatus(tradeOrderStatusEnum.getCode());
        this.tradeRequestService.updateSelective(updatableTradeRequest);

        this.dealTradeRequest(tradeRequest, tradeOrderStatusEnum, tradeOrderTypeEnum);

    }

    /**
     * SB
     *
     * @param registerBill
     * @return
     */
    public Optional<TradeOrder> createTradeFromRegisterBill(RegisterBill registerBill) {
        TradeDetail tdq=new TradeDetail();
        tdq.setBillId(registerBill.getBillId());
        tdq.setTradeType(TradeTypeEnum.NONE.getCode());
        boolean hasTD=this.tradeDetailService.listByExample(tdq).size()>0;
        if(hasTD){
            return Optional.empty();
        }


        TradeDto tradeDto = new TradeDto();
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.NONE);
        tradeDto.setMarketId(registerBill.getMarketId());
        tradeDto.getBuyer().setBuyerName(registerBill.getName());
        tradeDto.getBuyer().setBuyerId(registerBill.getUserId());
        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);


        ProductStock productStock = this.createOrFindProductStock(registerBill, registerBill.getUserId(), registerBill.getName()).orElseThrow(() -> {
            return new TraceBizException("创建/查询卖家库存失败");
        });
        ProductStock updatablePS = new ProductStock();
        updatablePS.setId(productStock.getId());
        updatablePS.setStockWeight(productStock.getStockWeight().add(registerBill.getWeight()));
        updatablePS.setTradeDetailNum(productStock.getTradeDetailNum() + 1);
        this.productStockService.updateSelective(updatablePS);

        ProductStockInput psInput = new ProductStockInput();
        psInput.setTradeWeight(registerBill.getWeight());
        psInput.setProductStockId(productStock.getProductStockId());


        List<TradeDetailInputDto> tradeDetailInputList = new ArrayList<>();
        psInput.setTradeDetailInputList(tradeDetailInputList);

        TradeDetailInputDto tradeDetailInputDto = new TradeDetailInputDto();
        tradeDetailInputDto.setBillId(registerBill.getBillId());
        tradeDetailInputDto.setTradeWeight(registerBill.getWeight());
        tradeDetailInputList.add(tradeDetailInputDto);

        this.checkInput(tradeDto, Arrays.asList(psInput));

        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);


        List<TradeRequest> tradeRequestList = StreamEx.of(psInput).map(productStockInput -> {
            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailForInput(productStockInput.getTradeDetailInputList(), tradeRequest);
            return tradeRequest;
        }).toList();


        this.dealTradeOrder(tradeOrder.getTradeOrderId(), TradeOrderStatusEnum.FINISHED);
//        this.productRpcService.create(registerBill, Optional.empty());
        return Optional.of(tradeOrder);

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
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailForInput(productStockInput.getTradeDetailInputList(), tradeRequest);
            return tradeRequest;
        }).toList();
        this.dealTradeOrder(tradeOrder.getTradeOrderId(), TradeOrderStatusEnum.FINISHED);
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
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailForInput(productStockInput.getTradeDetailInputList(), tradeRequest);
            return tradeRequest;
        }).toList();
        this.dealTradeOrder(tradeOrder.getTradeOrderId(), TradeOrderStatusEnum.FINISHED);
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
        ProductStock productStock = productStockService.get(productStockId);

        tradeDto.getSeller().setSellerId(productStock.getUserId());
        tradeDto.getSeller().setSellerName(productStock.getUserName());
        TradeOrder tradeOrder = this.createTradeOrder(tradeDto);

        List<TradeRequest> tradeRequestList = StreamEx.of(productStockInputList).map(productStockInput -> {
            TradeRequest tradeRequest = this.createTradeRequest(tradeDto, productStockInput, tradeOrder, productStockInput.getTradeWeight());
            List<TradeRequestDetail> tradeRequestDetailList = this.createTradeRequestDetailForInput(productStockInput.getTradeDetailInputList(), tradeRequest);
            return tradeRequest;
        }).toList();
        return tradeOrder;
    }

    /**
     * deal trade order
     *
     * @param tradeOrderId
     * @param tradeOrderStatusEnum
     */
    private void dealTradeOrder(Long tradeOrderId, TradeOrderStatusEnum tradeOrderStatusEnum) {

        if (TradeOrderStatusEnum.NONE == tradeOrderStatusEnum) {
            return;
        }
        TradeOrder tradeOrderItem = this.get(tradeOrderId);
        TradeOrderTypeEnum tradeOrderTypeEnum = TradeOrderTypeEnum.fromCode(tradeOrderItem.getOrderType()).orElseThrow(() -> {
            return new TraceBizException("交易类型错误");
        });


        TradeRequest trQ = new TradeRequest();
        trQ.setTradeOrderId(tradeOrderId);
        for (TradeRequest tradeRequest : this.tradeRequestService.listByExample(trQ)) {
            if (tradeOrderStatusEnum.equalsToCode(tradeRequest.getOrderStatus())) {
                return;
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
     * 处理
     *
     * @param tradeRequest
     * @param tradeOrderStatusEnum
     * @param tradeOrderTypeEnum
     */
    private void dealTradeRequest(TradeRequest tradeRequest, TradeOrderStatusEnum tradeOrderStatusEnum, TradeOrderTypeEnum tradeOrderTypeEnum) {
        {
            List<TradeRequestDetail> tradeRequestDetailList = this.findOrCreateTradeRequestDetail(tradeRequest);
            for (TradeRequestDetail trd : tradeRequestDetailList) {

                if (TradeOrderStatusEnum.FINISHED == tradeOrderStatusEnum) {

                    RegisterBill registerBill = this.billService.get(trd.getBillId());
                    TradeDetail sellerTD = this.tradeDetailService.get(trd.getTradeDetailId());

                    if (TradeOrderTypeEnum.NONE != tradeOrderTypeEnum) {
                        registerBill = this.billService.get(sellerTD.getBillId());
                    }

                    if (sellerTD != null) {
                        LOGGER.info("seller tradedetail id={},stockweight={}", sellerTD.getId(), sellerTD.getStockWeight());
                    }
                    TradeDetail buyerTD = this.createTradeDetail(registerBill, tradeRequest);

                    TradeDetail updatableBuyerTD = new TradeDetail();
                    updatableBuyerTD.setId(buyerTD.getId());
                    updatableBuyerTD.setStockWeight(trd.getTradeWeight());
                    updatableBuyerTD.setTotalWeight(trd.getTradeWeight());
                    updatableBuyerTD.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                    updatableBuyerTD.setParentId(null);

                    //分销时候buyerProductStock为空
                    Optional<ProductStock> buyerProductStock = this.createOrFindProductStock(registerBill, tradeRequest.getBuyerId(), tradeRequest.getBuyerName());
                    buyerProductStock.ifPresent(bp -> {
                        updatableBuyerTD.setProductStockId(bp.getProductStockId());
                        LOGGER.debug("buyerProductStock id={},stockweight={},tradeweight={}", bp.getId(), bp.getStockWeight(), trd.getTradeWeight());
                    });
                    if (TradeOrderTypeEnum.NONE == tradeOrderTypeEnum) {
                        updatableBuyerTD.setBatchNo(this.tradeDetailService.buildParentBatchNo(registerBill));
                        updatableBuyerTD.setParentBatchNo(this.tradeDetailService.buildParentBatchNo(registerBill));
                        updatableBuyerTD.setTradeType(TradeTypeEnum.NONE.getCode());
                        this.tradeDetailService.updateSelective(updatableBuyerTD);
                        LOGGER.info("buyer tradedetail id={},stockweight={},tradeweight={}", updatableBuyerTD.getId(), updatableBuyerTD.getStockWeight(), trd.getTradeWeight());
                        this.productRpcService.createRegCreate(updatableBuyerTD.getId(), tradeRequest.getBuyerMarketId(), Optional.empty());
//                        this.productRpcService.handleTradeStocks(
//                                Lists.newArrayList(this.tradeDetailService.get(updatableBuyerTD.getId()))
//                                , Lists.newArrayList()
//                                , Optional.empty()
//                                , tradeRequest.getSellerMarketId());
                        continue;
                    }

                    buyerProductStock.ifPresent(bp -> {

                        ProductStock updatableBuyerPS = new ProductStock();
                        updatableBuyerPS.setId(bp.getId());
                        updatableBuyerPS.setStockWeight(bp.getStockWeight().add(trd.getTradeWeight()));
                        updatableBuyerPS.setTradeDetailNum(bp.getTradeDetailNum() + 1);
                        this.productStockService.updateSelective(updatableBuyerPS);
                    });

                    LOGGER.debug("seller id={},name={}", sellerTD.getBuyerId(), sellerTD.getBuyerName());
                    updatableBuyerTD.setBatchNo(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    updatableBuyerTD.setParentBatchNo(this.tradeDetailService.buildParentBatchNo(sellerTD));
                    updatableBuyerTD.setCheckinRecordId(sellerTD.getCheckinRecordId());
                    updatableBuyerTD.setCheckoutRecordId(sellerTD.getCheckoutRecordId());
                    updatableBuyerTD.setCheckinStatus(sellerTD.getCheckinStatus());
                    updatableBuyerTD.setCheckoutStatus(sellerTD.getCheckoutStatus());
                    updatableBuyerTD.setTradeType(TradeTypeEnum.SEPARATE_SALES.getCode());

                    ProductStock sellerProductStock = this.createOrFindProductStock(registerBill, sellerTD.getBuyerId(), sellerTD.getBuyerName()).orElseThrow(() -> {
                        return new TraceBizException("创建/查询卖家库存失败");
                    });
                    LOGGER.debug("sellerProductStock id={},sellerid={},stockweight={},tradeweight={}", sellerProductStock.getId(), sellerProductStock.getUserId(), sellerProductStock.getStockWeight(), trd.getTradeWeight());
                    ProductStock updatableSellerPS = new ProductStock();
                    updatableSellerPS.setId(sellerProductStock.getId());
                    updatableSellerPS.setStockWeight(sellerProductStock.getStockWeight().subtract(trd.getTradeWeight()));

                    TradeDetail updatableSellerTD = new TradeDetail();
                    updatableSellerTD.setId(sellerTD.getId());
                    updatableSellerTD.setStockWeight(sellerTD.getStockWeight().subtract(trd.getTradeWeight()));
                    if (updatableSellerTD.getStockWeight().compareTo(BigDecimal.ZERO) == 0) {
                        updatableSellerTD.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
                        updatableSellerPS.setTradeDetailNum(sellerProductStock.getTradeDetailNum() - 1);
                    }
                    this.productStockService.updateSelective(updatableSellerPS);
//                    if (trd.getId() == null) {
//                        this.tradeRequestDetailService.insertSelective(trd);
//                    }

                    this.tradeDetailService.updateSelective(updatableSellerTD);
                    updatableBuyerTD.setParentId(sellerTD.getId());
                    LOGGER.info("buyer tradedetail id={},stockweight={}", updatableBuyerTD.getId(), updatableBuyerTD.getStockWeight());
                    this.tradeDetailService.updateSelective(updatableBuyerTD);


                    this.productRpcService.createRegCreate(updatableBuyerTD.getId(), tradeRequest.getBuyerMarketId(), Optional.empty());
                    this.productRpcService.deductRegDetail(sellerTD.getTradeDetailId(), tradeRequest.getSellerMarketId(), trd.getTradeWeight(), Optional.empty());
//                    this.productRpcService.handleTradeStocks(
//                            Lists.newArrayList(this.tradeDetailService.get(updatableBuyerTD.getId()))
//                            , Lists.newArrayList(this.tradeDetailService.get(updatableSellerTD.getId()))
//                            , Optional.empty()
//                            , tradeRequest.getSellerMarketId());

                }


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

    /**
     * @param tradeDetailInputList
     */
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
            if (trinput.getTradeDetailId() != null) {
                TradeDetail tradeDetail = this.tradeDetailService.get(trinput.getTradeDetailId());
                if (tradeDetail == null) {
                    throw new TraceBizException("批次数据不存在");
                }
                if (tradeDetail.getStockWeight().compareTo(trinput.getTradeWeight()) < 0) {
                    throw new TraceBizException("购买[" + tradeDetail.getProductName() + "]重量不能超过批次库存重量");
                }
                tradeRequestDetail.setTradeDetailId(tradeDetail.getTradeDetailId());
                tradeRequestDetail.setBillId(tradeDetail.getBillId());
            } else if (trinput.getBillId() != null) {
                tradeRequestDetail.setBillId(trinput.getBillId());
            } else {
                throw new TraceBizException("批次参数错误");
            }

            tradeRequestDetail.setTradeWeight(trinput.getTradeWeight());
            this.tradeRequestDetailService.insertSelective(tradeRequestDetail);
            tradeRequestDetailList.add(tradeRequestDetail);
        });

        return tradeRequestDetailList;
    }

    /**
     * @param tradeRequest
     * @param tradeRequest
     * @return
     */
    public List<TradeRequestDetail> findOrCreateTradeRequestDetail(TradeRequest tradeRequest) {

        ProductStock productStockItem = this.productStockService.selectByIdForUpdate(tradeRequest.getProductStockId())
                .orElseThrow(() -> {
                    return new TraceBizException("操作库存失败");
                });
        BigDecimal totalTradeWeight = tradeRequest.getTradeWeight();
        if (BigDecimal.ZERO.compareTo(totalTradeWeight) >= 0) {
            throw new TraceBizException("购买重量要大于0");
        }

        if (productStockItem.getStockWeight().compareTo(totalTradeWeight) < 0) {
            throw new TraceBizException("购买重量不能超过总库存重量");
        }
        List<TradeRequestDetail> tradeRequestDetailList = this.tradeRequestDetailService.findByTradeRequestIdList(Arrays.asList(tradeRequest.getId()));


        if (!tradeRequestDetailList.isEmpty()) {
            return tradeRequestDetailList;
        }

        TradeDetailQueryDto tradeDetailQuery = new TradeDetailQueryDto();
        tradeDetailQuery.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetailQuery.setProductStockId(productStockItem.getId());
        tradeDetailQuery.setSort("created");
        tradeDetailQuery.setOrder("asc");
        tradeDetailQuery.setMinStockWeight(BigDecimal.ZERO);

        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
        for (TradeDetail td : tradeDetailList) {
            if (BigDecimal.ZERO.compareTo(totalTradeWeight) >= 0) {
                break;
            }
            TradeRequestDetail tradeRequestDetail = new TradeRequestDetail();
            tradeRequestDetail.setTradeDetailId(td.getTradeDetailId());
            tradeRequestDetail.setTradeRequestId(tradeRequest.getTradeRequestId());
            if (totalTradeWeight.compareTo(td.getStockWeight()) >= 0) {
                tradeRequestDetail.setTradeWeight(td.getStockWeight());
                totalTradeWeight = totalTradeWeight.subtract(td.getStockWeight());
            } else {
                tradeRequestDetail.setTradeWeight(totalTradeWeight);
                totalTradeWeight = BigDecimal.ZERO;
            }
            this.tradeRequestDetailService.insertSelective(tradeRequestDetail);
            tradeRequestDetailList.add(tradeRequestDetail);
        }
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
        // query.setPreserveType(tradeDetailItem.getPreserveType());
        psQuery.setProductId(registerBill.getProductId());
        psQuery.setWeightUnit(registerBill.getWeightUnit());
        psQuery.setSpecName(registerBill.getSpecName());
        psQuery.setBrandId(registerBill.getBrandId());


        ProductStock batchStockItem = StreamEx.of(this.productStockService.listByExample(psQuery)).findFirst().orElseGet(() -> {
            //创建初始BatchStock
            ProductStock batchStock = new ProductStock();
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
     * 查询下一个code
     *
     * @return
     */
    private String getNextCode() {
        return this.uidRestfulRpcService.bizNumber(BizNumberType.TRADE_REQUEST_CODE);
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
        if (TradeOrderTypeEnum.NONE != tradeDto.getTradeOrderType()) {
            if (!seller.getSellerId().equals(productStock.getUserId())) {
                throw new TraceBizException("没有权限销售当前商品");
            }
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

    /**
     * 处理tradeorder
     *
     * @param tradeOrderItem
     * @param tradeStatusEnum
     */
//
//    public void handleTradeOrder(TradeOrder tradeOrderItem, TradeOrderStatusEnum tradeStatusEnum) {
//        tradeOrderItem.setOrderStatus(tradeStatusEnum.getCode());
//        this.updateSelective(tradeOrderItem);
//    }

}