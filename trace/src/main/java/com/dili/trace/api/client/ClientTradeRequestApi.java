package com.dili.trace.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.Attachment;
import com.dili.customer.sdk.domain.VehicleInfo;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.dto.CustomerSimpleExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.IDTO;
import com.dili.trace.api.input.*;
import com.dili.trace.api.output.CheckInApiDetailOutput;
import com.dili.trace.api.output.TradeRequestOutputDto;
import com.dili.trace.api.output.UserOutput;
import com.dili.trace.domain.*;
import com.dili.trace.dto.TradeDto;
import com.dili.trace.dto.CustomerExtendOutPutDto;
import com.dili.trace.dto.VehicleInfoDto;
import com.dili.trace.enums.*;
import com.dili.trace.rpc.dto.CustomerQueryDto;
import com.dili.trace.rpc.service.AttachmentRpcService;
import com.dili.trace.rpc.service.CarTypeRpcService;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.VehicleRpcService;
import com.dili.trace.service.*;

import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.swing.text.html.Option;

/**
 * (经营户，买家)用户交易请求接口
 */
@SuppressWarnings("deprecation")
@Api(value = "/api/client/clientTradeRequestApi")
@RestController
@AppAccess(role = Role.Client, url = "", subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
@RequestMapping(value = "/api/client/clientTradeRequestApi")
public class ClientTradeRequestApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientTradeRequestApi.class);
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    TradeOrderService tradeOrderService;

    @Autowired
    ProductStockService productStockService;

    @Autowired
    CustomerRpcService customerRpcService;

    @Autowired
    CarTypeRpcService carTypeRpcService;
    @Autowired
    UserStoreService userStoreService;
    @Autowired
    VehicleRpcService vehicleRpcService;
    @Autowired
    AttachmentRpcService attachmentRpcService;


    /**
     * 查询交易请求列表
     *
     * @param condition
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @RequestMapping(value = "/listPage.api", method = {RequestMethod.POST})
    public BaseOutput<BasePage<TradeRequest>> listPage(@RequestBody TradeRequestListInput condition) {

        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            Long userId = sessionData.getUserId();

            if (condition.getBuyerId() == null && condition.getSellerId() == null) {
                return BaseOutput.failure("参数错误");
            }
            if (!userId.equals(condition.getBuyerId())
                    && !userId.equals(condition.getSellerId())) {
                return BaseOutput.failure("参数错误");
            }
            if (condition.getBuyerId() != null) {
                condition.setBuyerMarketId(this.sessionContext.getSessionData().getMarketId());
                String orderTypeStrs = StreamEx.of(TradeOrderTypeEnum.values()).map(TradeOrderTypeEnum::getCode).joining(",");
                condition.setMetadata(IDTO.AND_CONDITION_EXPR, "  trade_order_id not  in( select id from trade_order  where  order_type not in(" + orderTypeStrs + ") and `buyer_id` = " + condition.getBuyerId() + " and `buyer_market_id` = " + condition.getBuyerMarketId() + ")");
            }
            if (condition.getSellerId() != null) {
                condition.setSellerMarketId(this.sessionContext.getSessionData().getMarketId());
            }
            condition.setSort("created");
            condition.setOrder("desc");

            BasePage<TradeRequest> page = this.tradeRequestService.listPageByExample(condition);
            List<TradeRequest> data = page.getDatas();
            return BaseOutput.success().setData(page);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 查询交易详情(包括交易批次)
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/viewTradeDetail.api", method = {RequestMethod.POST})
    public BaseOutput<CheckInApiDetailOutput> viewTradeDetail(@RequestBody TradeRequestInputDto inputDto) {

        if (inputDto == null || inputDto.getTradeRequestId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            Long userId = sessionData.getUserId();

            TradeRequest tradeRequestItem = this.tradeRequestService.get(inputDto.getTradeRequestId());
            if (tradeRequestItem == null) {
                return BaseOutput.failure("数据不存在");
            }
            if (!userId.equals(tradeRequestItem.getBuyerId()) && !userId.equals(tradeRequestItem.getSellerId())) {
                return BaseOutput.failure("没有权限查看数据");
            }
            TradeRequestOutputDto out = new TradeRequestOutputDto();
            TradeDetail tradeDetailQuery = new TradeDetail();
            tradeDetailQuery.setTradeRequestId(tradeRequestItem.getId());
            List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
            out.setTradeRequest(tradeRequestItem);
            out.setTradeDetailList(tradeDetailList);
            return BaseOutput.success().setData(out);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 创建购买请求
     *
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "创建购买请求")
    @RequestMapping(value = "/createBuyProductRequest.api", method = RequestMethod.POST)
    public BaseOutput<?> createBuyProductRequest(@RequestBody TradeRequestListInput inputDto) {
        if (inputDto == null || inputDto.getTradeMarketId() == null || inputDto.getBatchStockList() == null) {
            return BaseOutput.failure("参数错误");
        }
        List<ProductStockInput> batchStockInputList = StreamEx.of(inputDto.getBatchStockList()).nonNull().toList();
        if (batchStockInputList.isEmpty()) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            logger.info("buyerId id in session:{}", sessionData.getUserId());

            TradeDto tradeDto = new TradeDto();
            tradeDto.setMarketId(sessionData.getMarketId());
            tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);

            tradeDto.getBuyer().setBuyerId(sessionData.getUserId());
            tradeDto.getBuyer().setBuyerName(sessionData.getUserName());
            tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
            TradeOrder tradeOrder = this.tradeOrderService.createBuyTrade(tradeDto, batchStockInputList);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 创建销售请求
     *
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "创建销售请求")
    @RequestMapping(value = "/createSellProductRequest.api", method = RequestMethod.POST)
    public BaseOutput<?> createSellProductRequest(@RequestBody TradeRequestListInput inputDto) {
        if (inputDto == null || inputDto.getBuyerId() == null || inputDto.getBatchStockList() == null
                || inputDto.getBatchStockList().isEmpty()) {
            return BaseOutput.failure("参数错误");
        }

        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            logger.info("seller id in session:{}", sessionData.getUserId());

            TradeDto tradeDto = new TradeDto();
            tradeDto.setMarketId(sessionData.getMarketId());
            tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);
            tradeDto.getSeller().setSellerId(sessionData.getUserId());
            tradeDto.getSeller().setSellerName(sessionData.getUserName());

            tradeDto.getBuyer().setBuyerId(inputDto.getBuyerId());

            TradeOrder tradeOrder = this.tradeOrderService.createSellTrade(tradeDto,
                    inputDto.getBatchStockList());
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 创建销售请求
     *
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "创建分销请求")
    @RequestMapping(value = "/createSeperateProductRequest.api", method = RequestMethod.POST)
    public BaseOutput<?> createSeperateProductRequest(@RequestBody TradeRequestListInput inputDto) {
        if (inputDto == null || inputDto.getBuyerId() == null || inputDto.getBatchStockList() == null
                || inputDto.getBatchStockList().isEmpty()) {
            return BaseOutput.failure("参数错误");
        }

        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            logger.info("seller id in session:{}", sessionData.getUserId());

            TradeDto tradeDto = new TradeDto();
            tradeDto.setMarketId(sessionData.getMarketId());
            tradeDto.setTradeOrderType(TradeOrderTypeEnum.SEPREATE);
            tradeDto.getSeller().setSellerId(sessionData.getUserId());
            tradeDto.getSeller().setSellerName(sessionData.getUserName());
            tradeDto.getBuyer().setBuyerId(inputDto.getBuyerId());

            TradeOrder tradeOrder = this.tradeOrderService.createSeperateTrade(tradeDto,
                    inputDto.getBatchStockList());
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 通过订单ID查询交易请求详情
     *
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "通过订单ID查询交易请求详情")
    @RequestMapping(value = "/listPageTradeRequestByTraderOrderId.api", method = RequestMethod.POST)
    public BaseOutput<?> listPageTradeRequestByTraderOrderId(@RequestBody TradeRequestInputDto inputDto) {
        if (inputDto == null || inputDto.getTraderOrderId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            Long sellerId = sessionData.getUserId();

            TradeRequest request = new TradeRequest();
            request.setTradeOrderId(inputDto.getTraderOrderId());
            return BaseOutput.success().setData(this.tradeRequestService.listPageByExample(request));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 发起退货
     *
     * @param inputDto
     * @return 交易请求主键
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/createReturning.api", method = {RequestMethod.POST})
    public BaseOutput<Long> createReturning(@RequestBody TradeRequestInputDto inputDto) {
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            Long userId = sessionData.getUserId();

            Long id = this.tradeRequestService.createReturning(inputDto.getTradeRequestId(), userId);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 确认退货
     *
     * @param inputDto
     * @return 交易请求主键
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/handleReturning.api", method = {RequestMethod.POST})
    public BaseOutput<Long> handleReturning(@RequestBody TradeRequestInputDto inputDto) {
        try {
            TradeReturnStatusEnum returnStatus = TradeReturnStatusEnum.fromCode(inputDto.getReturnStatus())
                    .orElse(null);
            if (returnStatus == null || inputDto.getTradeRequestId() == null) {
                return BaseOutput.failure("参数错误");
            }
            SessionData sessionData = this.sessionContext.getSessionData();

            Long userId = sessionData.getUserId();
            Long id = this.tradeRequestService.handleReturning(inputDto.getTradeRequestId(), userId, returnStatus,
                    inputDto.getReason());
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 处理购买请求
     *
     * @param inputDto
     * @return
     */
    @RequestMapping(value = "/handleBuyerRquest.api", method = {RequestMethod.POST})
    public BaseOutput handleBuyRequest(@RequestBody TradeRequestHandleDto inputDto) {
        try {
            this.tradeOrderService.handleBuyerRequest(inputDto);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 查询购买历史信息
     *
     * @param buyerId 买家ID
     * @return 买家列表
     */
    @RequestMapping(value = "/listBuyHistory.api", method = {RequestMethod.GET})
    public BaseOutput<List<UserOutput>> listBuyHistory(@RequestParam Long buyerId) {
        try {
            List<UserOutput> list = this.tradeRequestService.queryTradeSellerHistoryList(buyerId);
            return BaseOutput.success().setData(list);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 查询卖家信息
     *
     * @param query
     * @return 卖家列表
     */
    @RequestMapping(value = "/listSeller.api", method = {RequestMethod.POST})
    public String listSeller(@RequestBody CustomerQueryDto query) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            query.setPage(1);
            //取100个，再多远程太慢了
            query.setRows(50);
            query.setMarketId(marketId);
            PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.customerRpcService.listSeller(query);

            // UAP 内置对象缺少市场名称、园区卡号，只能重新构建返回对象
            List<CustomerExtendOutPutDto> list = getListPageOutput(marketId, pageOutput, ClientTypeEnum.SELLER).getData();

            return JSON.toJSONString(BaseOutput.successData(list), SerializerFeature.DisableCircularReferenceDetect);
        } catch (TraceBizException e) {
            return JSON.toJSONString(BaseOutput.failure(e.getMessage()), SerializerFeature.DisableCircularReferenceDetect);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return JSON.toJSONString(BaseOutput.failure("操作失败：服务端出错"), SerializerFeature.DisableCircularReferenceDetect);
        }
    }

    /**
     * 构建卖家、买家、司机查询返回对象
     *
     * @param marketId
     * @param pageOutput
     * @return
     */
    private PageOutput<List<CustomerExtendOutPutDto>> getListPageOutput(Long marketId, PageOutput<List<CustomerSimpleExtendDto>> pageOutput, ClientTypeEnum clientTypeEnum) {
        PageOutput<List<CustomerExtendOutPutDto>> page = new PageOutput<>();
        if (null != pageOutput) {
            Map<Long, String> carTypeMap = StreamEx.ofNullable(this.carTypeRpcService.listCarType()).flatCollection(Function.identity())
                    .mapToEntry(CarTypeDTO::getId, CarTypeDTO::getName).toMap();

            List<CustomerSimpleExtendDto> customerList = pageOutput.getData();
            List<Long> customerIdList = StreamEx.ofNullable(customerList).flatCollection(Function.identity()).nonNull().map(CustomerSimpleExtendDto::getId).toList();
            Map<Long, List<VehicleInfo>> vehicleInfoMap = this.vehicleRpcService.findVehicleInfoByMarketIdAndCustomerIdList(marketId, customerIdList);
            Map<Long, Optional<Attachment>> attachmentMap = this.attachmentRpcService.findAttachmentByAttachmentTypeAndCustomerIdList(marketId, customerIdList, CustomerEnum.AttachmentType.营业执照);

            List<CustomerExtendOutPutDto> customerOutputList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(customerList)) {
                customerList.forEach(c -> {
                    CustomerExtendOutPutDto customerOutput = new CustomerExtendOutPutDto();
                    customerOutput.setMarketId(marketId);
                    customerOutput.setMarketName(this.sessionContext.getSessionData().getMarketName());
                    customerOutput.setId(c.getId());
                    customerOutput.setName(c.getName());
                    customerOutput.setOrganizationType(c.getOrganizationType());
                    customerOutput.setBusinessLicenseAttachment(attachmentMap.getOrDefault(c.getId(),Optional.empty()).orElse(null));
                    customerOutput.setPhone(c.getContactsPhone());
                    customerOutput.setClientType(clientTypeEnum.getCode());

                    this.customerRpcService.queryCardInfoByCustomerCode(c.getCode(), null, marketId).ifPresent(cardInfo -> {
                        customerOutput.setCardNo(cardInfo.getCardNo());
                    });

                    List<VehicleInfoDto> vehicleInfoDtoList = StreamEx.of(vehicleInfoMap.getOrDefault(c.getId(), Lists.newArrayList())).map(v -> {
                        VehicleInfoDto vehicleInfoDto = new VehicleInfoDto();
                        vehicleInfoDto.setVehiclePlate(v.getRegistrationNumber());
                        vehicleInfoDto.setVehicleType(v.getTypeNumber());
                        vehicleInfoDto.setVehicleTypeName(carTypeMap.getOrDefault(v.getTypeNumber(), ""));
                        return vehicleInfoDto;
                    }).toList();
                    customerOutput.setVehicleInfoList(vehicleInfoDtoList);

                    customerOutputList.add(customerOutput);
                });
            }
            BeanUtils.copyProperties(pageOutput, page);
            page.setData(customerOutputList);
        }
        return page;
    }

    /**
     * 查询可销售商品列表
     *
     * @param userId 业户ID
     * @return 库存列表
     */
    @RequestMapping(value = "/listSaleableProduct.api", method = {RequestMethod.GET})
    public BaseOutput<List<ProductStock>> listSaleableProduct(@RequestParam Long userId) {
        try {
            ProductStock productStock = new ProductStock();
            productStock.setUserId(userId);
            productStock.setMetadata(IDTO.AND_CONDITION_EXPR, "stock_weight > 0");
            List<ProductStock> list = this.productStockService.listByExample(productStock);
            return BaseOutput.success().setData(list);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }


}