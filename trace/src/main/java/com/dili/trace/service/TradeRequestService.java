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
import java.util.function.Function;

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
     * 查询下一个code
     *
     * @return
     */
    private String getNextCode() {
        return this.uidRestfulRpcService.bizNumber(BizNumberType.TRADE_REQUEST_CODE);
    }




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
            tradeDetail.setSoftWeight(td.getStockWeight());
            tradeDetail.setStockWeight(BigDecimal.ZERO);
            this.productRpcService.lock(td.getThirdPartyStockId(),buyerPS.getMarketId(),td.getStockWeight());
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

            StreamEx.of(tradeDetailList).forEach(buyerTd -> {
                BigDecimal softWeight=buyerTd.getSoftWeight();

                ProductStock buyerStockItem = this.batchStockService.selectByIdForUpdate(buyerTd.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                ProductStock batchStock = new ProductStock();
                batchStock.setId(buyerStockItem.getId());
                batchStock.setTradeDetailNum(buyerStockItem.getTradeDetailNum() + 1);
                batchStock.setStockWeight(buyerStockItem.getStockWeight().add(softWeight));
                this.batchStockService.updateSelective(batchStock);

                TradeDetail tradeDetail = new TradeDetail();
                tradeDetail.setId(buyerTd.getId());
                tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                tradeDetail.setStockWeight(softWeight);
                tradeDetail.setSoftWeight(BigDecimal.ZERO);
                this.tradeDetailService.updateSelective(tradeDetail);

                this.productRpcService.release(buyerTd.getThirdPartyStockId(),buyerStockItem.getMarketId(),softWeight);
            });
        } else {

            StreamEx.of(tradeDetailList).mapToEntry(buyerTd -> buyerTd, buyerTd -> {
                Long parentId = buyerTd.getParentId();
                return this.tradeDetailService.get(parentId);
            }).forKeyValue((buyerTd, sellertd) -> {

                BigDecimal softWeight=buyerTd.getSoftWeight();

                ProductStock sellerBatchStockItem = this.batchStockService.selectByIdForUpdate(sellertd.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                ProductStock buyerStockItem = this.batchStockService.selectByIdForUpdate(buyerTd.getProductStockId())
                        .orElseThrow(() -> {
                            return new TraceBizException("操作库存失败");
                        });

                TradeDetail buyerTradeDetail = new TradeDetail();
                buyerTradeDetail.setId(buyerTd.getId());
                buyerTradeDetail.setIsBatched(YesOrNoEnum.NO.getCode());
                buyerTradeDetail.setSoftWeight(BigDecimal.ZERO);
                buyerTradeDetail.setStockWeight(BigDecimal.ZERO);
                this.tradeDetailService.updateSelective(buyerTradeDetail);


                ProductStock sellerBatchStock = new ProductStock();
                sellerBatchStock.setId(sellerBatchStockItem.getId());
                sellerBatchStock.setStockWeight(sellerBatchStockItem.getStockWeight().add(softWeight));

                TradeDetail sellerTradeDetail = new TradeDetail();
                sellerTradeDetail.setId(sellertd.getId());
                sellerTradeDetail.setStockWeight(sellertd.getStockWeight().add(softWeight));

                if (SaleStatusEnum.FOR_SALE.equalsToCode(sellertd.getSaleStatus())) {
                    // do nothing
                } else {
                    sellerBatchStock.setTradeDetailNum(sellerBatchStockItem.getTradeDetailNum() + 1);
                    sellerTradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
                    sellerTradeDetail.setIsBatched(YesOrNoEnum.YES.getCode());
                }
                this.batchStockService.updateSelective(sellerBatchStock);
                this.tradeDetailService.updateSelective(sellerTradeDetail);

                this.productRpcService.release(buyerTradeDetail.getThirdPartyStockId(),buyerStockItem.getMarketId(),buyerTradeDetail.getSoftWeight());
                this.productRpcService.deductRegDetail(buyerTradeDetail.getTradeDetailId(),tradeRequestItem.getBuyerMarketId(),buyerTd.getStockWeight(),Optional.empty());

                this.productRpcService.increaseRegDetail(sellerTradeDetail.getTradeDetailId(),tradeRequestItem.getSellerMarketId(),buyerTd.getStockWeight(),Optional.empty());

            });

            this.userQrHistoryService.rollBackByTradeRequest(tradeRequestItem);
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
                StreamEx.ofNullable(cust.getAttachmentGroupInfoList()).flatCollection(Function.identity()).nonNull()
                        .filterBy(AttachmentGroupInfo::getCode, CustomerEnum.AttachmentType.营业执照.getCode())
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
     * @param ids
     * @return
     */
    public List<ReportOrderDetailDto> selectOrderDetailReport(List<String> ids) {
        return getActualDao().selectOrderDetailReport(ids);
    }
}