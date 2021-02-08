package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.Attachment;
import com.dili.customer.sdk.domain.dto.AttachmentGroupInfo;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.IDTO;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.*;
import com.dili.trace.api.input.TradeDetailInputDto;
import com.dili.trace.api.output.UserOutput;
import com.dili.trace.dao.TradeRequestMapper;
import com.dili.trace.domain.*;
import com.dili.trace.dto.*;
import com.dili.trace.dto.thirdparty.report.ReportOrderDetailDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.UpStreamTypeEnum;
import com.dili.trace.glossary.UserTypeEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.ProductRpcService;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 交易请求
 */
@Service
@Transactional
public class TradeRequestService extends BaseServiceImpl<TradeRequest, Long> {
    private static final Logger logger = LoggerFactory.getLogger(TradeRequestService.class);

    @Autowired
    ProductStockService batchStockService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    TradeOrderService tradeOrderService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    MessageService messageService;
    @Autowired
    UserStoreService userStoreService;
    @Autowired
    TradeRequestMapper tradeRequestMapper;
    @Autowired
    UserQrHistoryService userQrHistoryService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    ProcessService processService;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    ProductRpcService productRpcService;

    /**
     * 返回真实mapper
     *
     * @return
     */
    public TradeRequestMapper getActualDao() {
        return (TradeRequestMapper) getDao();
    }

    /**
     * 检查参数是否正确
     *
     * @param tradeDto
     * @param batchStockInputList
     */
    private void checkInput(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {

        CustomerExtendDto seller = customerRpcService.findCustomerById(tradeDto.getSeller().getSellerId(), tradeDto.getMarketId()).orElseThrow(() -> {
            return new TraceBizException("卖家不存在");
        });
        tradeDto.getSeller().setSellerName(seller.getName());

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
                tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.DOWNSTREAM_BUYER);
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
        // 判断是否全部是卖家的库存信息
        boolean notSellerOwnedBatchBlock = StreamEx.of(this.batchStockService.findByIdList(batchStockIdList))
                .map(ProductStock::getUserId).distinct().anyMatch(uid -> !uid.equals(sellerId));
        if (notSellerOwnedBatchBlock) {
            throw new TraceBizException("参数不匹配:有库存不属于当前卖家");
        }

    }

    /**
     * 创建销售请求并处理为完成
     */
    private List<TradeRequest> createSellRequest(TradeDto tradeDto,
                                                 List<ProductStockInput> batchStockInputList, Optional<OperatorUser> optUser) {

        // 检查提交参数
        this.checkInput(tradeDto, batchStockInputList);

        Long buyerId = tradeDto.getBuyer().getBuyerId();
        Long sellerId = tradeDto.getSeller().getSellerId();

        logger.info("sellerId:{},buyerId:{},checkinput:{}", sellerId, buyerId, Json.toJson(batchStockInputList));

        //获取交易单号
        TradeOrder tradeOrderItem = this.tradeOrderService.createTradeOrder(tradeDto, TradeOrderStatusEnum.FINISHED);
        List<TradeRequest> list = EntryStream
                .of(this.createTradeRequestList(tradeDto, tradeOrderItem, batchStockInputList))
                .mapKeyValue((request, tradeDetailInputList) -> {
                    //下单消息
                    String productName = "商品名称:" + request.getProductName() + "重量:" + request.getTradeWeight() + WeightUnitEnum.toName(request.getWeightUnit()) + "订单编号:" + request.getCode();
                    addMessage(sellerId, buyerId, request.getId(), MessageStateEnum.BUSINESS_TYPE_TRADE_SELL.getCode(), MessageTypeEnum.SALERORDER.getCode(), request.getCode(), productName, request.getSellerMarketId());
                    // 卖家下单
                    userQrHistoryService.createUserQrHistoryForOrder(request.getId(), buyerId);
                    return this.hanleRequest(request, tradeDetailInputList, tradeDto, optUser);
                }).toList();
        // this.createUpStreamAndDownStream(sellerId, buyerId);
        StreamEx.of(list).forEach(td -> {
            TradeRequest request = new TradeRequest();
            request.setHandleTime(new Date());
            request.setId(td.getId());
            this.updateSelective(request);
        });

        return list;
    }

    /**
     * 创建购买请求
     *
     * @param buyerId
     * @return
     */
//    @Transactional
    private List<TradeRequest> createBuyRequest(Long buyerId, List<ProductStockInput> batchStockInputList, Long marketId, Optional<OperatorUser> optUser) {
        if (batchStockInputList == null || batchStockInputList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }
        List<Long> batchStockId = StreamEx.of(batchStockInputList).nonNull().map(ProductStockInput::getProductStockId)
                .toList();
        // 库存所对应的用户就是卖家
        List<Long> sellerUserIdList = StreamEx.of(this.batchStockService.findByIdList(batchStockId))
                .map(ProductStock::getUserId).nonNull().distinct().toList();
        if (sellerUserIdList.size() != 1) {
            throw new TraceBizException("参数错误");
        }
        Long sellerUserId = sellerUserIdList.get(0);
        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(marketId);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.BUY);

        tradeDto.getBuyer().setBuyerId(buyerId);
        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        tradeDto.getSeller().setSellerId(sellerUserId);


        logger.debug("buyerId={},sellerUserId={}", buyerId, sellerUserId);
        List<TradeRequest> tradeRequests = EntryStream.of(this.createTradeRequestListForBuy(tradeDto, batchStockInputList))
                .mapKeyValue((request, tradeDetailInputList) -> {
                    return this.hanleRequest(request, tradeDetailInputList, tradeDto, optUser);
                }).toList();

        //下单消息
        tradeRequests.stream().forEach(request ->
                {
                    String productName = "商品名称:" + request.getProductName() + ",  重量:" + request.getTradeWeight() + "(" + WeightUnitEnum.toName(request.getWeightUnit()) + "),  订单编号:" + request.getCode();
                    addMessage(buyerId, sellerUserIdList.get(0), request.getId(), MessageStateEnum.BUSINESS_TYPE_TRADE.getCode(), MessageTypeEnum.BUYERORDER.getCode(), request.getCode(), productName, request.getSellerMarketId());
                }
        );
        return tradeRequests;
    }

    /**
     * 创建请求
     *
     * @param tradeDto
     * @param tradeOrderItem
     * @param input
     * @return
     */
    TradeRequest createTradeRequest(TradeDto tradeDto, TradeOrder tradeOrderItem, ProductStockInput input) {
        Long sellerId = tradeDto.getSeller().getSellerId();
        Long marketId = tradeDto.getMarketId();

        if (input.getTradeWeight() == null || BigDecimal.ZERO.compareTo(input.getTradeWeight()) >= 0) {
            throw new TraceBizException("购买重量不能小于0");
        }
        if (input.getProductStockId() == null) {
            throw new TraceBizException("购买商品ID不能为空");
        }

        if (input.getTradeWeight() == null || input.getTradeWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TraceBizException("购买总重量不能为空或小于0");
        }


        ProductStock batchStock = this.batchStockService.get(input.getProductStockId());
        if (batchStock == null) {
            throw new TraceBizException("购买商品不存在");
        }
        if (sellerId != null && !sellerId.equals(batchStock.getUserId())) {
            throw new TraceBizException("没有权限销售当前商品");
        }

        TradeDto.Buyer buyer = tradeDto.getBuyer();
        TradeDto.Seller seller = tradeDto.getSeller();

        TradeRequest request = new TradeRequest();
        request.setProductStockId(input.getProductStockId());
        request.setTradeWeight(input.getTradeWeight());
        request.setReturnStatus(TradeReturnStatusEnum.NONE.getCode());
        request.setSellerName(seller.getSellerName());
        request.setSellerId(seller.getSellerId());
        request.setSellerMarketId(tradeOrderItem.getSellerMarketId());
        request.setBuyerName(buyer.getBuyerName());
        request.setBuyerMarketId(tradeOrderItem.getBuyerMarketId());
        request.setCreated(new Date());
        request.setModified(new Date());
        request.setBuyerId(buyer.getBuyerId());
        request.setTradeOrderId(tradeOrderItem.getId());
        request.setCode(this.getNextCode());
        request.setProductName(batchStock.getProductName());
        request.setWeightUnit(batchStock.getWeightUnit());
        logger.info("buyer id:{},seller id:{}", request.getBuyerId(), request.getSellerId());
        this.insertSelective(request);
        return request;
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
     * 处理交易
     */
    @Transactional
    TradeRequest hanleRequest(TradeRequest requestItem, List<TradeDetailInputDto> tradeDetailInputList,
                              TradeDto tradeDto, Optional<OperatorUser> optUser) {

        List<MutablePair<TradeDetail, BigDecimal>> tradeDetailIdWeightList = StreamEx
                .of(CollectionUtils.emptyIfNull(tradeDetailInputList)).nonNull().map(tr -> {
                    if (tr.getTradeWeight() == null || BigDecimal.ZERO.compareTo(tr.getTradeWeight()) >= 0) {
                        throw new TraceBizException("购买批次重量不能为空或小于0");
                    }
                    TradeDetail tradeDetail = this.tradeDetailService.get(tr.getTradeDetailId());
                    if (tradeDetail == null) {
                        throw new TraceBizException("数据不存在");
                    }
                    return MutablePair.of(tradeDetail, tr.getTradeWeight());
                }).toList();

        ProductStock batchStockItem = this.batchStockService.selectByIdForUpdate(requestItem.getProductStockId())
                .orElseThrow(() -> {
                    return new TraceBizException("操作库存失败");
                });


        BigDecimal totalTradeWeight = requestItem.getTradeWeight();


        //查询和统计(基于批次)用户总库存(其实可以通过总库存获得)
        TradeDetail tradeDetailQuery = new TradeDetail();
        tradeDetailQuery.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetailQuery.setProductStockId(batchStockItem.getId());
        tradeDetailQuery.setSort("created");
        tradeDetailQuery.setOrder("asc");
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
        if (batchStockItem.getStockWeight().compareTo(totalTradeWeight) < 0) {
            throw new TraceBizException("购买重量不能超过总库存重量");
        }

        List<TradeDetail> buyerTradeDetailList = new ArrayList<>();
        List<TradeDetail> sellerTradeDetailList = new ArrayList<>();
        //基于总库存进行交易
        if (tradeDetailIdWeightList.isEmpty()) {
            AtomicReference<BigDecimal> sumTradeWeightAt = new AtomicReference<BigDecimal>(BigDecimal.ZERO);
            buyerTradeDetailList = StreamEx.of(tradeDetailList).map(td -> {
                BigDecimal stockWeight = td.getStockWeight();
                sumTradeWeightAt.set(sumTradeWeightAt.get().add(stockWeight));
                logger.info("stockWeight={}", stockWeight);
                logger.info("sumTradeWeightAt={}", sumTradeWeightAt.get());
                logger.info("totalTradeWeight={}", totalTradeWeight);
                BigDecimal tradeWeight = BigDecimal.ZERO;
                if (sumTradeWeightAt.get().compareTo(totalTradeWeight) <= 0) {
                    tradeWeight = stockWeight;
                } else {
                    if (sumTradeWeightAt.get().subtract(stockWeight).compareTo(totalTradeWeight) < 0) {
                        tradeWeight = totalTradeWeight.subtract(sumTradeWeightAt.get().subtract(stockWeight));
                    } else {
                        return null;
                    }
                }

                logger.info("tradeWeight={}", tradeWeight);
                Optional<TradeDetail> tradeDetailOpt = this.tradeDetailService.createTradeDetail(requestItem.getId(), td,
                        tradeWeight, tradeDto);

                TradeDetail sellerDetail = new TradeDetail();
                BeanUtils.copyProperties(td, sellerDetail);
                sellerDetail.setStockWeight(tradeWeight);
                sellerTradeDetailList.add(sellerDetail);

                return tradeDetailOpt.orElse(null);
            }).nonNull().toList();
        } else {
            //基于批次交易
            buyerTradeDetailList = StreamEx.of(tradeDetailIdWeightList).map(p -> {
                TradeDetail tradeDetaiItem = p.getKey();
                BigDecimal tradeWeight = p.getValue();
                Optional<TradeDetail> tradeDetailOpt = this.tradeDetailService.createTradeDetail(requestItem.getId(), tradeDetaiItem,
                        tradeWeight, tradeDto);

                TradeDetail sellerDetail = new TradeDetail();
                BeanUtils.copyProperties(tradeDetaiItem, sellerDetail);
                sellerDetail.setStockWeight(tradeWeight);
                sellerTradeDetailList.add(sellerDetail);

                return tradeDetailOpt.orElse(null);

            }).nonNull().toList();
        }

        // 向UAP同步库存
//        if (TradeOrderTypeEnum.BUY.equals(tradeDto.getTradeOrderType())) {
//            // 销售类型才直接处理库存。购买类型有锁库存的操作，应该在卖家确认时向 UAP 同步库存
//            processService.afterTrade(buyerTradeDetailList, sellerTradeDetailList, tradeDto.getMarketId(), optUser);
//        }

        // BatchStock batchStock = new BatchStock();
        // batchStock.setId(batchStockItem.getId());
        // batchStock.setStockWeight(batchStockItem.getStockWeight().subtract(requestItem.getTradeWeight()));
        // this.batchStockService.updateSelective(batchStock);

        return this.get(requestItem.getId());

    }

    /**
     * 创建批量交易请求
     *
     * @param tradeDto
     * @param batchStockInputList
     * @return
     */
    Map<TradeRequest, List<TradeDetailInputDto>> createTradeRequestListForBuy(TradeDto tradeDto, List<ProductStockInput> batchStockInputList) {
        this.checkInput(tradeDto, batchStockInputList);
        Map<TradeRequest, List<TradeDetailInputDto>> map = StreamEx.of(batchStockInputList).nonNull()
                .mapToEntry(input -> {

                    ;
                    // 创建 TradeOrder，并将其作为 key
                    TradeOrder tradeOrderItem = this.tradeOrderService.createTradeOrder(tradeDto, TradeOrderStatusEnum.NONE);
                    // 创建 TradeRequest，并将其作为 key
                    TradeRequest request = this.createTradeRequest(tradeDto, tradeOrderItem, input);
                    return request;
                }, input -> {
                    return StreamEx.of(CollectionUtils.emptyIfNull(input.getTradeDetailInputList())).nonNull().toList();
                }).toMap();
        return map;

    }

    /**
     * 创建批量交易请求
     *
     * @param tradeDto
     * @param tradeOrderItem
     * @param batchStockInputList
     * @param batchStockInputList
     * @return
     */
    Map<TradeRequest, List<TradeDetailInputDto>> createTradeRequestList(TradeDto tradeDto, TradeOrder tradeOrderItem,
                                                                        List<ProductStockInput> batchStockInputList) {
        Map<TradeRequest, List<TradeDetailInputDto>> map = StreamEx.of(batchStockInputList).nonNull()
                .mapToEntry(input -> {
                    TradeRequest request = this.createTradeRequest(tradeDto, tradeOrderItem, input);
                    return request;
                }, input -> {
                    return StreamEx.of(CollectionUtils.emptyIfNull(input.getTradeDetailInputList())).nonNull().toList();
                }).toMap();
        return map;

    }

    // /**
    // * 处理购买请求
    // *
    // * @return
    // */
    // public Long handleBuyRequest(Long tradeRequestId, TradeOrderStatusEnum
    // tradeRequestStatus,
    // List<TradeDetailInputDto> tradeDetailInputList) {

    // // request.setTradeRequestStatus(TradeRequestStatusEnum.NONE.getCode());
    // // request.setTradeRequestType(TradeRequestTypeEnum.BUY.getCode());
    // if (tradeRequestId == null || tradeRequestStatus == null) {
    // throw new TraceBusinessException("参数错误");
    // }
    // TradeRequest tradeRequestItem = this.get(tradeRequestId);
    // if (tradeRequestItem == null) {
    // throw new TraceBusinessException("交易请求不存在");
    // }
    // if (TradeOrderStatusEnum.NONE == tradeRequestStatus) {
    // throw new TraceBusinessException("参数错误");
    // }

    // return this.hanleRequest(tradeRequestItem, tradeDetailInputList).getId();
    // }

    /**
     * 申请退货
     *
     * @param tradeRequestId
     * @param userId
     * @return
     */
    @Transactional
    public Long createReturning(Long tradeRequestId, Long userId) {
        if (tradeRequestId == null || userId == null) {
            throw new TraceBizException("参数错误");
        }
        TradeRequest tradeRequestItem = this.get(tradeRequestId);
        if (tradeRequestItem == null) {
            throw new TraceBizException("数据不存在");
        }
        if (!tradeRequestItem.getBuyerId().equals(userId)) {
            throw new TraceBizException("没有权限访问当前数据");
        }

        if (!TradeReturnStatusEnum.NONE.equalsToCode(tradeRequestItem.getReturnStatus())) {
            throw new TraceBizException("退货状态错误");
        }

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setId(tradeRequestItem.getId());
        tradeRequest.setReturnStatus(TradeReturnStatusEnum.RETURNING.getCode());
        this.updateSelective(tradeRequest);

        TradeDetail tradeDetailQuery = new TradeDetail();
        tradeDetailQuery.setTradeRequestId(tradeRequestId);
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
        StreamEx.of(tradeDetailList).forEach(td -> {
            logger.debug("tradeDetail Id={},totalWeight={},stockWeight={}", td.getId(), td.getTotalWeight(), td.getStockWeight());

            boolean hasChildTD = this.tradeDetailService.findTradeDetailByParentIdList(Arrays.asList(td.getId())).size() > 0;
//            boolean changed = td.getTotalWeight().compareTo(td.getStockWeight()) != 0;
            if (hasChildTD) {
                throw new TraceBizException("不能对已销售的商品申请退货");
            }
            ProductStock buyerPS = this.batchStockService.selectByIdForUpdate(td.getProductStockId())
                    .orElseThrow(() -> {
                        return new TraceBizException("操作库存失败");
                    });

            ProductStock updateBuyerStock = new ProductStock();
            updateBuyerStock.setId(buyerPS.getId());
            updateBuyerStock.setTradeDetailNum(buyerPS.getTradeDetailNum() - 1);
            updateBuyerStock.setStockWeight(buyerPS.getStockWeight().subtract(td.getStockWeight()));
            this.batchStockService.updateSelective(updateBuyerStock);

            TradeDetail tradeDetail = new TradeDetail();
            tradeDetail.setId(td.getId());
            tradeDetail.setSaleStatus(SaleStatusEnum.NOT_FOR_SALE.getCode());
            this.tradeDetailService.updateSelective(tradeDetail);

        });

        return tradeRequest.getId();
    }

    /**
     * 完成退货处理
     *
     * @param tradeRequestId
     * @param userId
     * @return
     */
    @Transactional
    public Long handleReturning(Long tradeRequestId, Long userId, TradeReturnStatusEnum returnStatus, String reason) {
        if (tradeRequestId == null || userId == null) {
            throw new TraceBizException("参数错误");
        }
        TradeRequest tradeRequestItem = this.get(tradeRequestId);
        if (tradeRequestItem == null) {
            throw new TraceBizException("数据不存在");
        }
        if (!tradeRequestItem.getSellerId().equals(userId)) {
            throw new TraceBizException("没有权限访问当前数据");
        }
        if (TradeReturnStatusEnum.REFUSE == returnStatus || TradeReturnStatusEnum.RETURNED == returnStatus) {
            // do nothing
        } else {
            throw new TraceBizException("处理状态错误");
        }
        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setId(tradeRequestItem.getId());
        tradeRequest.setReturnStatus(returnStatus.getCode());
        tradeRequest.setReason(reason);
        this.updateSelective(tradeRequest);

        TradeDetail tradeDetailQuery = new TradeDetail();
        tradeDetailQuery.setTradeRequestId(tradeRequestId);
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);

        if (TradeReturnStatusEnum.REFUSE == returnStatus) {

            StreamEx.of(tradeDetailList).forEach(td -> {

                ProductStock batchStockItem = this.batchStockService.selectByIdForUpdate(td.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                ProductStock batchStock = new ProductStock();
                batchStock.setId(batchStockItem.getId());
                batchStock.setTradeDetailNum(batchStockItem.getTradeDetailNum() + 1);
                batchStock.setStockWeight(batchStockItem.getStockWeight().add(td.getStockWeight()));
                this.batchStockService.updateSelective(batchStock);

                TradeDetail tradeDetail = new TradeDetail();
                tradeDetail.setId(td.getId());
                tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                this.tradeDetailService.updateSelective(tradeDetail);
            });
        } else {

            StreamEx.of(tradeDetailList).mapToEntry(buyerTd -> buyerTd, buyerTd -> {
                Long parentId = buyerTd.getParentId();
                return this.tradeDetailService.get(parentId);
            }).forKeyValue((buyertd, sellertd) -> {

                TradeDetail buyerTradeDetail = new TradeDetail();
                buyerTradeDetail.setId(buyertd.getId());
                buyerTradeDetail.setIsBatched(YesOrNoEnum.NO.getCode());
                buyerTradeDetail.setStockWeight(BigDecimal.ZERO);
                this.tradeDetailService.updateSelective(buyerTradeDetail);

                ProductStock sellerBatchStockItem = this.batchStockService.selectByIdForUpdate(sellertd.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                ProductStock sellerBatchStock = new ProductStock();
                sellerBatchStock.setId(sellerBatchStockItem.getId());
                sellerBatchStock.setStockWeight(sellerBatchStockItem.getStockWeight().add(buyertd.getStockWeight()));

                TradeDetail sellerTradeDetail = new TradeDetail();
                sellerTradeDetail.setId(sellertd.getId());
                sellerTradeDetail.setStockWeight(sellertd.getStockWeight().add(buyertd.getStockWeight()));

                if (SaleStatusEnum.FOR_SALE.equalsToCode(sellertd.getSaleStatus())) {
                    // do nothing
                } else {
                    sellerBatchStock.setTradeDetailNum(sellerBatchStockItem.getTradeDetailNum() + 1);
                    sellerTradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                    sellerTradeDetail.setIsBatched(YesOrNoEnum.YES.getCode());
                }
                this.batchStockService.updateSelective(sellerBatchStock);
                this.tradeDetailService.updateSelective(sellerTradeDetail);


                this.productRpcService.increaseRegDetail(sellerTradeDetail.getTradeDetailId(),tradeRequestItem.getSellerMarketId(),buyertd.getStockWeight(),Optional.empty());
                this.productRpcService.deductRegDetail(buyerTradeDetail.getTradeDetailId(),tradeRequestItem.getBuyerMarketId(),buyertd.getStockWeight(),Optional.empty());
            });


            try {
                userQrHistoryService.rollbackUserQrStatusForOrderReturn(tradeRequestItem.getId(), tradeRequestItem.getBuyerId());
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return tradeRequest.getId();
    }


    /**
     * 查询分页数据
     *
     * @param dto
     * @param userId
     * @return
     */
    public BasePage<TradeRequest> listPageForStatusOrder(TradeRequestInputDto dto, Long userId) {
        dto.setBuyerId(userId);
        dto.setOrderStatus(TradeOrderStatusEnum.FINISHED.getCode());
        if (dto.getPage() == null || dto.getPage() < 0) {
            dto.setPage(1);
        }
        if (dto.getRows() == null || dto.getRows() <= 0) {
            dto.setRows(10);
        }
        PageHelper.startPage(dto.getPage(), dto.getRows());
        List<TradeRequest> list = this.tradeRequestMapper.queryListByOrderStatus(dto);
        Page<TradeRequest> page = (Page) list;
        BasePage<TradeRequest> result = new BasePage<TradeRequest>();
        result.setDatas(list);
        result.setPage(page.getPageNum());
        result.setRows(page.getPageSize());
        result.setTotalItem(page.getTotal());
        result.setTotalPage(page.getPages());
        result.setStartIndex(page.getStartRow());
        return result;
    }

    /**
     * 增加查询条件
     *
     * @param tradeRequest
     * @param userId
     * @return
     */
    public BasePage<TradeRequest> listPageTradeRequestByBuyerIdOrSellerId(TradeRequestInputDto tradeRequest,
                                                                          Long userId) {
        tradeRequest.setMetadata(IDTO.AND_CONDITION_EXPR, "(buyer_id=" + userId + " OR seller_id=" + userId + ")");
        return this.listPageByExample(tradeRequest);

    }

    /**
     * 处理购买请求
     *
     * @param handleDto
     */
//    @Transactional(rollbackFor = Exception.class)
    private void handleBuyerRequest(TradeRequestHandleDto handleDto) {
        Long tradeRequestId = handleDto.getTradeRequestId();
        Integer handleStatus = handleDto.getHandleStatus();
        TradeRequest tradeRequest = this.get(tradeRequestId);
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setId(tradeRequest.getTradeOrderId());
//        tradeOrder.setOrderStatus(handleStatus);
        this.tradeOrderService.updateSelective(tradeOrder);

        TradeDetail tradeDetailQuery = new TradeDetail();
        tradeDetailQuery.setTradeRequestId(tradeRequestId);
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
        StreamEx.of(tradeDetailList).forEach(td -> {
            if (handleStatus.equals(TradeOrderStatusEnum.FINISHED.getCode())) {
                ProductStock productStock = this.batchStockService.selectByIdForUpdate(td.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });
                TradeDetail tradeDetail = new TradeDetail();
                tradeDetail.setId(td.getId());
                tradeDetail.setStockWeight(td.getSoftWeight());
                tradeDetail.setSoftWeight(BigDecimal.ZERO);
                tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                tradeDetail.setIsBatched(YesOrNoEnum.YES.getCode());
                this.tradeDetailService.updateSelective(tradeDetail);

                ProductStock productStockUpdate = new ProductStock();
                productStockUpdate.setId(td.getProductStockId());
                productStockUpdate.setStockWeight(productStock.getStockWeight().add(td.getSoftWeight()));
                productStockUpdate.setTradeDetailNum(productStock.getTradeDetailNum() + 1);
                this.batchStockService.updateSelective(productStockUpdate);


            } else if (handleStatus.equals(TradeOrderStatusEnum.CANCELLED.getCode())) {
                TradeDetail parentTradeDetail = this.tradeDetailService.get(td.getParentId());
                TradeDetail parentTradeDetailForUpdate = new TradeDetail();
                parentTradeDetailForUpdate.setId(parentTradeDetail.getId());
                parentTradeDetailForUpdate.setStockWeight(parentTradeDetail.getStockWeight().add(td.getSoftWeight()));

                ProductStock productStock = this.batchStockService.selectByIdForUpdate(parentTradeDetail.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                BigDecimal totalStockWeight = productStock.getStockWeight().add(td.getSoftWeight());
                ProductStock productStockUpdate = new ProductStock();
                productStockUpdate.setId(productStock.getId());
                productStockUpdate.setStockWeight(totalStockWeight);
                if (parentTradeDetail.getStockWeight().compareTo(BigDecimal.ZERO) <= 0
                        && parentTradeDetailForUpdate.getStockWeight().compareTo(BigDecimal.ZERO) > 0) {

                    productStockUpdate.setTradeDetailNum(productStock.getTradeDetailNum() + 1);
                    parentTradeDetailForUpdate.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                    parentTradeDetailForUpdate.setIsBatched(YesOrNoEnum.YES.getCode());
                }
                this.tradeDetailService.updateSelective(parentTradeDetailForUpdate);
                this.batchStockService.updateSelective(productStockUpdate);
            }
        });

        TradeRequest request = new TradeRequest();
        request.setHandleTime(new Date());
        request.setId(tradeRequestId);
        this.updateSelective(request);

        //完成发送消息
        if (handleStatus.equals(TradeOrderStatusEnum.FINISHED.getCode())) {
            //下单消息--一个单一个消息方便跳转页面
            String productName = "商品名称:" + tradeRequest.getProductName() + ",  重量:" + tradeRequest.getTradeWeight() + "(" + WeightUnitEnum.toName(tradeRequest.getWeightUnit()) + "),  订单编号:" + tradeRequest.getCode();
            addMessage(tradeRequest.getSellerId(), tradeRequest.getBuyerId(), tradeRequest.getId(), MessageStateEnum.BUSINESS_TYPE_TRADE.getCode(), MessageTypeEnum.BUYERORDER.getCode(), null, productName, tradeRequest.getSellerMarketId());
            userQrHistoryService.createUserQrHistoryForOrder(tradeRequest.getId(), tradeRequest.getBuyerId());
        }
    }

    /**
     * 新增消息并发送短信
     *
     * @param sendUserId
     * @param messageType
     * @param productNames
     */
    private void addMessage(Long sendUserId, Long receiUserId, Long businessId, Integer businessType, Integer messageType, String tradeNo, String productNames, Long marketId) {
        // 增加消息
        MessageInputDto messageInputDto = new MessageInputDto();
        messageInputDto.setCreatorId(sendUserId);
        messageInputDto.setMessageType(messageType);
        messageInputDto.setReceiverIdArray(new Long[]{receiUserId});
        messageInputDto.setEventMessageContentParam(new String[]{tradeNo});
        messageInputDto.setSourceBusinessType(businessType);
        messageInputDto.setSourceBusinessId(businessId);
        messageInputDto.setReceiverType(MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode());
        //增加卖家短信
        Map<String, Object> sellmap = new HashMap<>();
        sellmap.put("userName", sendUserId);
        sellmap.put("created", DateUtils.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        sellmap.put("tradeInfo", productNames);
        messageInputDto.setSmsContentParam(sellmap);
        messageService.addMessage(messageInputDto, marketId);
    }

    /**
     * 查询 历史数据
     *
     * @param buyerId
     * @return
     */
    public List<UserOutput> queryTradeSellerHistoryList(Long buyerId) {
        TradeRequest request = new TradeRequest();
        request.setBuyerId(buyerId);
        request.setSort("created");
        request.setOrder("desc");
        List<TradeRequest> tradeRequests = this.listByExample(request);
        List<Long> sellerIds = StreamEx.of(tradeRequests)
                .map(TradeRequest::getSellerId).nonNull().distinct().toList();


        return StreamEx.of(tradeRequests).nonNull().filter(tr -> {
            return tr.getSellerId() != null;
        }).map(tr -> {

            Long sellerId = tr.getSellerId();
            return this.customerRpcService.findCustomerById(sellerId, tr.getSellerMarketId()).map(cust -> {
                UserOutput outPutDto = new UserOutput();
                outPutDto.setUserId(sellerId);
                outPutDto.setUserName(cust.getName());
                outPutDto.setOrganizationType(cust.getOrganizationType());
                StreamEx.of(cust.getAttachmentGroupInfoList()).filterBy(AttachmentGroupInfo::getCode, CustomerEnum.AttachmentType.营业执照.getCode())
                        .flatCollection(AttachmentGroupInfo::getAttachmentList).findFirst().ifPresent(businessLicenseAttachment -> {
                    outPutDto.setBusinessLicenseAttachment(businessLicenseAttachment);

                });

//                UserStore userStore = new UserStore();
//                userStore.setUserId(sellerId);
//                UserStore userStoreExists = StreamEx.of(userStoreService.list(userStore)).nonNull().findFirst().orElse(null);
//                if (userStoreExists != null && StringUtils.isNoneBlank(userStoreExists.getStoreName())) {
//                    outPutDto.setUserName(userStoreExists.getStoreName());
//                }
                return outPutDto;

            }).orElse(null);
        }).nonNull().distinct(UserOutput::getUserId).toList();
    }

    /**
     * 查询近7天有买商品的用户
     *
     * @param user
     * @return
     */
    public List<Long> selectBuyerIdWithouTradeRequest(UserQueryDto user) {
        return getActualDao().selectBuyerIdWithouTradeRequest(user);
    }

    /**
     * 查询近7天有买商品的用户
     *
     * @param ids
     * @return
     */
    public List<ReportOrderDetailDto> selectOrderDetailReport(List<String> ids) {
        return getActualDao().selectOrderDetailReport(ids);
    }
}