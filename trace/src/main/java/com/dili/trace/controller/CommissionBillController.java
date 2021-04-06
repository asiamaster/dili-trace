package com.dili.trace.controller;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.*;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.DetectRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.UsualAddress;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.dili.trace.rpc.service.CityRpcService;
import com.dili.trace.service.*;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
//@Api("/commissionBill")
@Controller
@RequestMapping("/commissionBill")
public class CommissionBillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommissionBillController.class);

    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    TradeTypeService tradeTypeService;
    @Autowired
    UserService userService;
    @Autowired
    UserPlateService userPlateService;
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    CityRpcService cityService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    CommissionBillService commissionBillService;
    @Autowired
    BillService billService;

    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    UapRpcService uapRpcService;


    /**
     * 跳转到CommissionBill页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到CommissionBill页面")
    @RequestMapping(value = "/checksheetIndex.html", method = RequestMethod.GET)
    public String checksheetIndex(ModelMap modelMap) {
        Date now = new Date();
        modelMap.put("createdStart", DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
        modelMap.put("createdEnd", DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);
        return "forward:/checkSheet/index.html?billType=" + BillTypeEnum.COMMISSION_BILL.getCode();
    }

    /**
     * 分页查询CommissionBill
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询CommissionBill", notes = "分页查询CommissionBill，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CommissionBill", paramType = "form", value = "CommissionBill的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/findHighLightBill.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    Object findHighLightBill(RegisterBillDto dto) throws Exception {
        try {
            RegisterBill bill = this.commissionBillService.findHighLightCommissionBill(dto, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success().setData(bill);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }

    }

    /**
     * 新增CommissionBill
     *
     * @param input
     * @return
     */
    @ApiOperation("新增CommissionBill")
    @RequestMapping(value = "/insert.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput insert(@RequestBody CreateListBillParam input) {

        if (input == null) {
            return BaseOutput.failure("参数错误");
        }

        Firm firm = this.uapRpcService.getCurrentFirm().orElse(null);
        if (firm == null) {
            return BaseOutput.failure("登录用户市场查询失败");
        }

        List<RegisterBill> billList = StreamEx.ofNullable(input.getRegisterBills()).flatCollection(Function.identity())
                .nonNull()
                .map(rbInputDto -> {
                    CustomerExtendDto customer=new CustomerExtendDto();
                    RegisterBill rb = rbInputDto.build(customer, firm.getId());
                    rb.setIdCardNo(input.getIdCardNo());
                    rb.setName(input.getName());
                    rb.setCorporateName(input.getCorporateName());
                    rb.setPhone(input.getPhone());
                    rb.setPlate(input.getPlate());
                    rb.setAddr(input.getAddr());
                    rb.setUserId(input.getUserId());
                    List<ImageCert> imageList =StreamEx.ofNullable(input.getGlobalImageCertList()).flatCollection(Function.identity()).nonNull().toList();
                    rb.setImageCertList(imageList);
                    rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
                    rb.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
                    rb.setRegisterSource(RegisterSourceEnum.getRegisterSourceEnum(input.getRegisterSource()).orElse(RegisterSourceEnum.OTHERS).getCode());
                    rb.setSourceName(input.getSourceName());
                    rb.setSourceId(input.getSourceId());
                    rb.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
                    rb.setPreserveType(PreserveTypeEnum.NONE.getCode());
                    rb.setVerifyType(VerifyTypeEnum.NONE.getCode());
                    rb.setTruckType(TruckTypeEnum.FULL.getCode());
//                    rb.setIsCheckin(YesOrNoEnum.NO.getCode());
                    rb.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
                    rb.setIsDeleted(YesOrNoEnum.NO.getCode());
                    rb.setMeasureType(MeasureTypeEnum.COUNT_WEIGHT.getCode());
                    // 理货类型为交易区时才保存交易区号和id
                    if (RegisterSourceEnum.TRADE_AREA.getCode().equals(input.getRegisterSource())) {
//                        rb.setTradeTypeId(input.getTradeTypeId());
                        rb.setSourceName(input.getSourceName());
                        rb.setSourceId(input.getSourceId());
//                        rb.setTradeTypeName(input.getTradeTypeName());
                    }
                    return rb;
                }).toList();
        try {
            OperatorUser operatorUser = this.uapRpcService.getCurrentOperator().orElseThrow(() -> {
                throw new TraceBizException("用户未登录");
            });
            this.commissionBillService.createCommissionBillByManager(billList, operatorUser);
            return BaseOutput.success("新增成功").setData(billList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return BaseOutput.failure("服务器出错,请重试");
        }


    }

    /**
     * 登记单录入页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/create.html")
    public String create(ModelMap modelMap) {
        modelMap.put("tradeTypes", tradeTypeService.findAll());
        modelMap.put("citys", this.queryCitys());

        return "commissionBill/create";
    }

    /**
     * 查询产地
     *
     * @return
     */
    private List<UsualAddress> queryCitys() {
        return usualAddressService.findUsualAddressByType(UsualAddressTypeEnum.REGISTER);
    }


    /**
     * 进入审核页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/audit.html")
    public String audit(ModelMap modelMap, Long billId) {
        RegisterBill item = this.billService.get(billId);
        modelMap.put("item", item);
        return "commissionBill/audit";
    }

    /**
     * 审核委托登记单
     *
     * @param billId
     * @return
     */
    @RequestMapping(value = "/doAuditCommissionBillByManager.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doAuditCommissionBillByManager(Long billId) {
        try {
            this.commissionBillService.doAuditCommissionBillByManager(billId, this.uapRpcService.getCurrentOperator().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 登记单录查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/view/{id}/{displayWeight}", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @PathVariable Long id,
                       @PathVariable(required = false) Boolean displayWeight) {
        RegisterBill bill = this.billService.get(id);
        if (bill == null) {
            return "";
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(bill.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        if (displayWeight == null) {
            displayWeight = false;
        }

        modelMap.put("item", bill);
        modelMap.put("displayWeight", displayWeight);
        return "commissionBill/view";
    }


    /**
     * 自动送检
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/autoCheck/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput autoCheck(@PathVariable Long id) {
        try {
            this.registerBillService.autoCheckRegisterBill(id,this.uapRpcService.getCurrentOperator().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 采样检测
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/samplingCheck/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput samplingCheck(@PathVariable Long id) {
        try {
            this.registerBillService.samplingCheckRegisterBill(id,this.uapRpcService.getCurrentOperator().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }


    /**
     * (微信扫码)电商登记单扫码页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/billQRDetail.html", method = RequestMethod.GET)
    public String billQRDetail(ModelMap modelMap, Long id) {
        RegisterBill bill = this.billService.get(id);
        if (bill == null || !BillTypeEnum.COMMISSION_BILL.equalsToCode(bill.getBillType())) {
            modelMap.put("detectRecordList", CollectionUtils.emptyCollection());
            modelMap.put("item", new RegisterBill());
            return "commissionBill/qrDetail";
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(bill.getCode());
        modelMap.put("detectRecordList", detectRecordList);


        modelMap.put("item", bill);
        return "commissionBill/billQRDetail";
    }


    /**
     * 批量复检
     *
     * @param idlist
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "批量复检", notes = "批量复检")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "idlist", paramType = "form", value = "ID集合", required = false, dataType = "string")})
    @RequestMapping(value = "/doBatchReviewCheck.action", method = {RequestMethod.POST})
    public @ResponseBody
    BaseOutput doBatchReviewCheck(@RequestBody List<Long> idlist) throws Exception {
        try {
            List<Long> billIdList = StreamEx.ofNullable(idlist).nonNull().flatCollection(Function.identity()).nonNull().toList();
            if (billIdList.isEmpty()) {
                return BaseOutput.failure("参数不能为空");
            }
            List<String>reviewCheckedCodeList= this.commissionBillService.doBatchReviewCheck(billIdList, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success().setData(reviewCheckedCodeList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }

    }


}
