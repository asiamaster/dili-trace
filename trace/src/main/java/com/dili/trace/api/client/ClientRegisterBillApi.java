package com.dili.trace.api.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseDomain;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.api.output.TradeDetailBillOutput;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.RegisterHead;
import com.dili.trace.domain.UpStream;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.CreatorRoleEnum;
import com.dili.trace.enums.RegistTypeEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 经营户(卖家)报备接口
 * Created by wangguofeng
 */
@RestController
@RequestMapping(value = "/api/client/clientRegisterBill")
@Api(value = "/api/client/clientRegisterBill", description = "登记单相关接口")
@AppAccess(role = Role.Client, url = "", subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
public class ClientRegisterBillApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientRegisterBillApi.class);
    @Autowired
    private RegisterBillService registerBillService;

    @Autowired
    private LoginSessionContext sessionContext;

    @Autowired
    ImageCertService imageCertService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    RegisterHeadService registerHeadService;
    /**
     * 保存多个登记单
     *
     * @param createListBillParam
     * @return
     */
    @ApiOperation("保存多个登记单")
    @RequestMapping(value = "/createRegisterBillList.api", method = RequestMethod.POST)
    public BaseOutput<List<Long>> createRegisterBillList(@RequestBody CreateListBillParam createListBillParam) {
        logger.info("保存多个登记单:{}", JSON.toJSONString(createListBillParam));
        if (createListBillParam == null || createListBillParam.getRegisterBills() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            List<CreateRegisterBillInputDto> registerBills = StreamEx.of(createListBillParam.getRegisterBills())
                    .nonNull().map(dto->{
                        dto.setMarketId(sessionData.getMarketId());
                        return dto;
                    }).toList();
            if (registerBills == null) {
                return BaseOutput.failure("没有登记单");
            }
            logger.info("保存多个登记单操作用户:{}，{}", sessionData.getUserId(), sessionData.getUserName());
            List<Long> idList = this.registerBillService.createBillList(registerBills,
                    sessionData.getUserId(), sessionData.getOptUser(), CreatorRoleEnum.CUSTOMER);
            return BaseOutput.success().setData(idList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 修改报备单信息
     *
     * @param dto
     * @return
     */
    @ApiOperation("修改报备单")
    @RequestMapping(value = "/doEditRegisterBill.api", method = RequestMethod.POST)
    public BaseOutput doEditRegisterBill(@RequestBody CreateRegisterBillInputDto dto) {
        logger.info("修改报备单:{}", JSON.toJSONString(dto));
        if (dto == null || dto.getBillId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            CustomerExtendDto customer = this.customerRpcService
                    .findCustomerByIdOrEx(sessionContext.getSessionData().getUserId()
                            , sessionContext.getSessionData().getMarketId());


            RegisterBill registerBill = dto.build(customer,sessionData.getMarketId());
            logger.info("保存登记单:{}", JSON.toJSONString(registerBill));
            this.registerBillService.doEdit(registerBill, dto.getImageCertList(), Optional.empty());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
    }

    /**
     * 删除报备单
     *
     * @param dto
     * @return
     */
    @ApiOperation("删除报备单")
    @RequestMapping(value = "/doDeleteRegisterBill.api", method = RequestMethod.POST)
    public BaseOutput doDeleteRegisterBill(@RequestBody CreateRegisterBillInputDto dto) {
        logger.info("删除报备单:{}", JSON.toJSONString(dto));
        if (dto == null || dto.getBillId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            CustomerExtendDto customer = this.customerRpcService
                    .findCustomerByIdOrEx(sessionContext.getSessionData().getUserId()
                            , sessionContext.getSessionData().getMarketId());
            logger.info("删除报备单:billId:{},userId:{}", dto.getBillId(), customer.getId());
            this.registerBillService.doDelete(dto.getBillId(), customer.getId(), Optional.empty());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
    }

    /**
     * 获取登记单列表
     *
     * @param input
     * @return
     */
    @ApiOperation(value = "获取登记单列表")
    @ApiImplicitParam(paramType = "body", name = "RegisterBill", dataType = "RegisterBill", value = "获取登记单列表")
    @RequestMapping(value = "/listPage.api", method = RequestMethod.POST)
    public BaseOutput<BasePage<TradeDetailBillOutput>> listPage(@RequestBody RegisterBillDto input) {
        logger.info("获取登记单列表:{}", JSON.toJSONString(input));
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            Long userId = sessionData.getUserId();

            logger.info("获取登记单列表 操作用户:{}", userId);
            input.setUserId(userId);
            input.setMarketId(sessionData.getMarketId());
            BasePage basePage = this.tradeDetailService.selectTradeDetailAndBill(input);
            return BaseOutput.success().setData(basePage);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }
    /**
     * 查看进门登记单
     * @param query
     * @RETURN
     */
    @ApiOperation("查看进门登记单")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/viewRegisterBill.api", method = {RequestMethod.POST})
    public BaseOutput<RegisterBill> viewRegisterBill(@RequestBody RegisterBill query) {
        try {
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        query.setMarketId(this.sessionContext.getSessionData().getMarketId());
        query.setUserId(this.sessionContext.getSessionData().getUserId());
        RegisterBillOutputDto registerBill =    StreamEx.of(this.registerBillService.listByExample(query)).findFirst().map(b-> RegisterBillOutputDto.build(b, Lists.newArrayList())).orElseThrow(()->{
            return new TraceBizException("数据不存在");
        });

            List<ImageCert> imageCertList = imageCertService.findImageCertListByBillId(query.getId(), BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null));
            registerBill.setImageCertList(imageCertList);

            UpStream upStream = upStreamService.get(registerBill.getUpStreamId());
            if(upStream!=null){
                registerBill.setUpStreamName(upStream.getName());
            }

            //获取主台账单的总重量与剩余总重量
            if (RegistTypeEnum.PARTIAL.getCode().equals(registerBill.getRegistType())) {
                RegisterHead registerHead = new RegisterHead();
                registerHead.setCode(registerBill.getRegisterHeadCode());
                List<RegisterHead> registerHeadList =  registerHeadService.listByExample(registerHead);
                if(CollectionUtils.isNotEmpty(registerHeadList)){
                    registerHead = registerHeadList.get(0);
                } else {
                    return BaseOutput.failure("未找到主台账单");
                }
                registerBill.setHeadWeight(registerHead.getWeight());
                registerBill.setRemainWeight(registerHead.getRemainWeight());
            }
            return BaseOutput.success().setData(registerBill);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("查询进门主台账单数据出错", e);
            return BaseOutput.failure("查询进门主台账单数据出错");
        }
    }

    /**
     * 通过登记单ID获取登记单详细信息
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "通过登记单ID获取登记单详细信息")
    @RequestMapping(value = "/viewTradeDetailBill.api", method = RequestMethod.POST)
    public BaseOutput<RegisterBillOutputDto> viewTradeDetailBill(@RequestBody RegisterBillApiInputDto inputDto) {
        if (inputDto == null || (inputDto.getBillId() == null && inputDto.getTradeDetailId() == null)) {
            return BaseOutput.failure("参数错误");
        }

        logger.info("获取登记单详细信息->marketId:{},billId:{},tradeDetailId:{}", inputDto.getMarketId(),inputDto.getBillId(), inputDto.getTradeDetailId());
        try {
            RegisterBillOutputDto registerBill = this.registerBillService.viewTradeDetailBill(inputDto);


            List<ImageCert> imageCertList = imageCertService.findImageCertListByBillId(inputDto.getBillId(), BillTypeEnum.REGISTER_BILL);
            registerBill.setImageCertList(imageCertList);

            UpStream upStream = upStreamService.get(registerBill.getUpStreamId());
            if(upStream!=null){
                registerBill.setUpStreamName(upStream.getName());
            }

            //获取主台账单的总重量与剩余总重量
            if (RegistTypeEnum.PARTIAL.getCode().equals(registerBill.getRegistType())) {
                RegisterHead registerHead = new RegisterHead();
                registerHead.setCode(registerBill.getRegisterHeadCode());
                List<RegisterHead> registerHeadList =  registerHeadService.listByExample(registerHead);
                if(CollectionUtils.isNotEmpty(registerHeadList)){
                    registerHead = registerHeadList.get(0);
                } else {
                    return BaseOutput.failure("未找到主台账单");
                }
                registerBill.setHeadWeight(registerHead.getWeight());
                registerBill.setRemainWeight(registerHead.getRemainWeight());
            }

            String data=JSON.toJSONString(registerBill, SerializerFeature.DisableCircularReferenceDetect);
            return BaseOutput.success().setData(JSON.parse(data));

        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }
}
