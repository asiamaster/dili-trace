package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.*;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.dto.*;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.dili.trace.rpc.service.CityRpcService;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Api("/sg/registerBill")
@Controller
@RequestMapping("/sg/registerBill")
public class SgRegisterBillController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SgRegisterBillController.class);
    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    BillService billService;
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
    CustomerRpcService customerService;
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    CityRpcService cityService;
    @Autowired
    UsualAddressService usualAddressService;

    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    ImageCertService imageCertService;


    /**
     * 跳转到RegisterBill页面
     *
     * @param modelMap
     * @return
     */
//    @ApiOperation("跳转到RegisterBill页面")
//    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
//    public String index(ModelMap modelMap) {
//        Date now = new Date();
//        modelMap.put("createdStart", DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
//        modelMap.put("createdEnd", DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
//        modelMap.put("state", RegisterBillStateEnum.WAIT_AUDIT.getCode());
//        UserTicket user = SessionContext.getSessionContext().getUserTicket();
//        modelMap.put("user", user);
//
//        return "sg/registerBill/index";
//    }

    /**
     * 查询RegisterBill
     *
     * @param registerBill
     * @return
     */

    @ApiOperation(value = "查询RegisterBill", notes = "查询RegisterBill，返回列表信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "RegisterBill", paramType = "form", value = "RegisterBill的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    List<RegisterBill> list(RegisterBillDto registerBill) {
        return billService.list(registerBill);
    }


    /**
     * 分页查询RegisterBill
     *
     * @param registerBill
     * @return
     * @throws Exception
     */
//    @ApiOperation(value = "分页查询RegisterBill", notes = "分页查询RegisterBill，返回easyui分页信息")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "RegisterBill", paramType = "form", value = "RegisterBill的form信息", required = false, dataType = "string")})
//    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
//    public @ResponseBody
//    Object listPage(@RequestBody  RegisterBillDto registerBill) throws Exception {
//        String json=registerBillService.listPage(registerBill);
//        return JSON.parse(json);
//    }

    /**
     * 新增RegisterBill
     *
     * @param input
     * @return
     */
//    @ApiOperation("新增RegisterBill")
//    @RequestMapping(value = "/insert2.action", method = RequestMethod.POST)
//    public @ResponseBody
//    BaseOutput insert2(@RequestBody CreateListBillParam input) {
//        if (input == null) {
//            return BaseOutput.failure("参数错误");
//        }
//
//        List<RegisterBill> billList = StreamEx.ofNullable(input.getRegisterBills()).flatCollection(Function.identity())
//                .nonNull()
//                .map(rbInputDto -> {
//                    CustomerExtendDto  user = new CustomerExtendDto();
//                    RegisterBill rb = rbInputDto.build(user,this.uapRpcService.getCurrentFirm().orElse(null).getId());
////                    List<ImageCert> imageList = this.registerBillService.buildImageCertList(input.getDetectReportUrl()
////                            , rbInputDto.getHandleResultUrl(),
////                            rbInputDto.getOriginCertifiyUrl());
//                    List<ImageCert> imageList =StreamEx.ofNullable(input.getGlobalImageCertList()).flatCollection(Function.identity()).nonNull().toList();
//                    rb.setImageCertList(imageList);
//                    rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
//                    rb.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
//                    rb.setRegisterSource(RegisterSourceEnum.getRegisterSourceEnum(input.getRegisterSource()).orElse(RegisterSourceEnum.OTHERS).getCode());
//                    rb.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
//                    rb.setPreserveType(PreserveTypeEnum.NONE.getCode());
//                    rb.setVerifyType(VerifyTypeEnum.NONE.getCode());
//                    rb.setTruckType(TruckTypeEnum.FULL.getCode());
//                    rb.setIsCheckin(YesOrNoEnum.NO.getCode());
//                    rb.setIsDeleted(YesOrNoEnum.NO.getCode());
//                    return rb;
//                }).toList();
//        try {
//            OperatorUser operatorUser = this.uapRpcService.getCurrentOperator().get();
//            this.registerBillService.createRegisterBillList(billList, operatorUser);
//            return BaseOutput.success("新增成功").setData(billList);
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            return BaseOutput.failure("服务器出错,请重试");
//        }
//    }

/*
    *//**
     * 新增RegisterBill
     *
     * @param registerBills
     * @return
     *//*
    @ApiOperation("新增RegisterBill")
    @RequestMapping(value = "/insert.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput insert(@RequestBody List<RegisterBill> registerBills) {
        LOGGER.info("保存登记单数据:" + registerBills.size());
        Map<String, String> tradeTypeMap = CollectionUtils.emptyIfNull(tradeTypeService.findAll()).stream()
                .filter(Objects::nonNull).collect(Collectors.toMap(TradeType::getTypeId, TradeType::getTypeName));
        List<RegisterBill> billList = new ArrayList<>();
        for (RegisterBill registerBill : registerBills) {

            if (registerBill.getRegisterSource().intValue() == RegisterSourceEnum.TALLY_AREA.getCode().intValue()) {
                // 理货区
                User user = userService.findByTallyAreaNo(registerBill.getTallyAreaNo());
                if (user == null) {
                    LOGGER.error("新增登记单失败理货区号[" + registerBill.getTallyAreaNo() + "]对应用户不存在");
                    return BaseOutput.failure("理货区号[" + registerBill.getTallyAreaNo() + "]对应用户不存在");
                }
                registerBill.setName(user.getName());
                registerBill.setIdCardNo(user.getCardNo());
                registerBill.setAddr(user.getAddr());
                registerBill.setUserId(user.getId());
                registerBill.setTallyAreaNo(user.getTallyAreaNos());
            } else {
                String tradeTypeId = StringUtils.trimToEmpty(registerBill.getTradeTypeId());
                registerBill.setTradeTypeName(tradeTypeMap.getOrDefault(tradeTypeId, null));

                if (StringUtils.isNotBlank(registerBill.getTradeAccount())
                        || StringUtils.isNotBlank(registerBill.getTradePrintingCard())) {
                    Customer condition = new Customer();
                    condition.setCustomerId(StringUtils.trimToNull(registerBill.getTradeAccount()));
                    condition.setPrintingCard(StringUtils.trimToNull(registerBill.getTradePrintingCard()));
                    Customer customer = this.customerService.findCustomer(condition).orElse(null);
                    if (customer == null) {
                        LOGGER.error("新增登记单失败交易账号[" + registerBill.getTradeAccount() + "]对应用户不存在");
                        return BaseOutput.failure("交易账号[" + registerBill.getTradeAccount() + "]对应用户不存在");
                    }
                    registerBill.setName(customer.getName());
                    registerBill.setIdCardNo(customer.getIdNo());
                    registerBill.setAddr(customer.getAddress());
                    registerBill.setTradePrintingCard(customer.getPrintingCard());
                    registerBill.setPhone(customer.getPhone());

                }

            }
            registerBill.setState(RegisterBillStateEnum.WAIT_AUDIT.getCode());
            registerBill.setDetectReportUrl(StringUtils.trimToNull(registerBill.getDetectReportUrl()));
            registerBill.setOriginCertifiyUrl(StringUtils.trimToNull(registerBill.getOriginCertifiyUrl()));
            registerBill.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
            billList.add(registerBill);
        }
        try {
            return BaseOutput.success("新增成功").setData(billList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("服务器出错,请重试");
        }

    }*/

    /**
     * 修改RegisterBill
     *
     * @param registerBill
     * @return
     */
    @ApiOperation("修改RegisterBill")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "RegisterBill", paramType = "form", value = "RegisterBill的form信息", required = true, dataType = "string")})
    @RequestMapping(value = "/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput update(RegisterBill registerBill) {
        billService.updateSelective(registerBill);
        return BaseOutput.success("修改成功");
    }

    /**
     * 删除RegisterBill
     *
     * @param id
     * @return
     */
    @ApiOperation("删除RegisterBill")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", paramType = "form", value = "RegisterBill的主键", required = true, dataType = "long")})
    @RequestMapping(value = "/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput delete(Long id) {
        billService.delete(id);
        return BaseOutput.success("删除成功");
    }


    /**
     * 查询城市
     *
     * @return
     */
    private List<UsualAddress> queryCitys() {
        return usualAddressService.findUsualAddressByType(UsualAddressTypeEnum.REGISTER);
    }

    /**
     * 登记单录查看页面
     *
     * @param modelMap
     * @return
     *//*
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @RequestParam(required = true,name = "id") Long id
            ,  @RequestParam(required = false,name = "displayWeight") Boolean displayWeight) {
        RegisterBill item = billService.get(id);
        if (item == null) {
            return "";
        }
        if (displayWeight == null) {
            displayWeight = false;
        }
        if (RegisterSourceEnum.TALLY_AREA.getCode().equals(item.getRegisterSource())) {
            // 分销信息
            if (item.getSalesType() != null
                    && item.getSalesType().intValue() == SalesTypeEnum.SEPARATE_SALES.getCode().intValue()) {
                // 分销
                List<SeparateSalesRecord> records = separateSalesRecordService
                        .findByRegisterBillCode(item.getCode());
                modelMap.put("separateSalesRecords", records);
            }
        } else {
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
            condition.setRegisterBillCode(item.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }

//		DetectRecord conditon=DTOUtils.newDTO(DetectRecord.class);
//		conditon.setRegisterBillCode(registerBill.getCode());
//		conditon.setSort("id");
//		conditon.setOrder("desc");
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        modelMap.put("displayWeight", displayWeight);

        RegisterBillOutputDto registerBill=new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item),registerBill);

        List<ImageCert>imageCerts=this.registerBillService.findImageCertListByBillId(item.getBillId());
        registerBill.setImageCertList(imageCerts);

        modelMap.put("registerBill", registerBill);

        return "sg/registerBill/view";
    }*/

    /**
     * 交易区交易单分销记录
     *
     * @param modelMap
     * @param id       交易单ID
     * @return
     */
    @RequestMapping(value = "/tradeBillSsRecord.html", method = RequestMethod.GET)
    public String tradeBillSRecord(ModelMap modelMap, @RequestParam(name = "id",required = true) Long id) {
        QualityTraceTradeBill qualityTraceTradeBill = qualityTraceTradeBillService.get(id);
        SeparateSalesRecord condition = new SeparateSalesRecord();
        condition.setTradeNo(qualityTraceTradeBill.getOrderId());
        List<SeparateSalesRecord> separateSalesRecords = separateSalesRecordService.listByExample(condition);
        modelMap.put("separateSalesRecords", separateSalesRecords);
        return "sg/registerBill/tradeBillSsRecord";
    }

   /* *//**
     * 登记单录修改页面
     *
     * @param modelMap
     * @return
     *//*
    @RequestMapping(value = "/uploadDetectReport.html", method = RequestMethod.GET)
    public String uploadDetectReport(ModelMap modelMap, @RequestParam(name = "id",required = true) Long id) {
        RegisterBill registerBill = billService.get(id);
        if (registerBill == null) {
            return "";
        }
        if (RegisterSourceEnum.TALLY_AREA.getCode().equals(registerBill.getRegisterSource())) {
            // 分销信息
            if (registerBill.getSalesType() != null
                    && registerBill.getSalesType().intValue() == SalesTypeEnum.SEPARATE_SALES.getCode().intValue()) {
                // 分销
                List<SeparateSalesRecord> records = separateSalesRecordService
                        .findByRegisterBillCode(registerBill.getCode());
                modelMap.put("separateSalesRecords", records);
            }
        } else {
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
            condition.setRegisterBillCode(registerBill.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }
        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "sg/registerBill/upload-detectReport";
    }
*/
   /* *//**
     * 上传产地证明
     *
     * @param modelMap
     * @return
     *//*
    @RequestMapping(value = "/uploadOrigincertifiy.html", method = RequestMethod.GET)
    public String uploadOrigincertifiy(ModelMap modelMap, @RequestParam(name = "id",required = true) Long id) {
        RegisterBill registerBill = billService.get(id);
        if (registerBill == null) {
            return "";
        }
        if (RegisterSourceEnum.TALLY_AREA.getCode().equals(registerBill.getRegisterSource())) {
            // 分销信息
            if (registerBill.getSalesType() != null
                    && registerBill.getSalesType().intValue() == SalesTypeEnum.SEPARATE_SALES.getCode().intValue()) {
                // 分销
                List<SeparateSalesRecord> records = separateSalesRecordService
                        .findByRegisterBillCode(registerBill.getCode());
                modelMap.put("separateSalesRecords", records);
            }
        } else {
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
            condition.setRegisterBillCode(registerBill.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }
        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "sg/registerBill/upload-origincertifiy";
    }*/

    /**
     * 审核页面
     *
     * @param modelMap
     * @param id
     * @return
    @RequestMapping(value = "/audit.html", method = RequestMethod.GET)
    public String audit(ModelMap modelMap, @RequestParam(name = "id",required = true) Long id) {
        modelMap.put("registerBill", billService.get(id));
        return "sg/registerBill/audit";
    }

    *//**
     * 审核
     *
     * @param id
     * @param pass
     * @return
     *//*
    @RequestMapping(value = "/audit/{id}/{pass}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput audit(@PathVariable Long id, @PathVariable Boolean pass) {
        try {
            registerBillService.auditRegisterBill(id, pass);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    *//**
     * 批量主动送检
     *
     * @param modelMap
     * @param idList
     * @return
     *//*
    @RequestMapping(value = "/doBatchAutoCheck", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchAutoCheck(ModelMap modelMap, @RequestBody List<Long> idList) {
//		modelMap.put("registerBill", registerBillService.get(id));
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.registerBillService.doBatchAutoCheck(idList);
    }

    *//**
     * 批量撤销
     *
     * @param modelMap
     * @param idList
     * @return
     *//*
    @RequestMapping(value = "/batchUndo.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput batchUndo(ModelMap modelMap, @RequestBody List<Long> idList) {
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.registerBillService.doBatchUndo(idList);
    }

    *//**
     * 批量采样检测
     *
     * @param modelMap
     * @param idList
     * @return
     *//*
    @RequestMapping(value = "/doBatchSamplingCheck", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchSamplingCheck(ModelMap modelMap, @RequestBody List<Long> idList) {
//		modelMap.put("registerBill", registerBillService.get(id));
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.registerBillService.doBatchSamplingCheck(idList);
    }

    *//**
     * 批量审核
     *
     * @param modelMap
     * @param batchAuditDto
     * @return
     *//*
    @RequestMapping(value = "/doBatchAudit", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchAudit(ModelMap modelMap, @RequestBody BatchAuditDto batchAuditDto) {
//		modelMap.put("registerBill", registerBillService.get(id));
//		if (batchAuditDto.getPass() == null) {
//			return BaseOutput.failure("参数错误");
//		}
        List<Long> idList = CollectionUtils.emptyIfNull(batchAuditDto.getRegisterBillIdList()).stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        batchAuditDto.setPass(true);
        batchAuditDto.setRegisterBillIdList(idList);
        return this.registerBillService.doBatchAudit(batchAuditDto);
    }

    *//**
     * 撤销
     *
     * @param id
     * @return
     *//*
    @RequestMapping(value = "/undo.html", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput undo(@RequestParam(name = "id",required = true) Long id) {
        try {
            registerBillService.undoRegisterBill(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    *//**
     * 自动送检
     *
     * @param id
     * @return
     *//*
    @RequestMapping(value = "/autoCheck/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput autoCheck(@PathVariable Long id) {
        try {
            registerBillService.autoCheckRegisterBill(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    *//**
     * 采样检测
     *
     * @param id
     * @return
     *//*
    @RequestMapping(value = "/samplingCheck/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput samplingCheck(@PathVariable Long id) {
        try {
            registerBillService.samplingCheckRegisterBill(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    *//**
     * 复检
     *
     * @param id
     * @return
     *//*
    @RequestMapping(value = "/reviewCheck/{id}", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput reviewCheck(@PathVariable Long id) {
        try {
            registerBillService.reviewCheckRegisterBill(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }*/

    /**
     * 跳转到statics页面
     *
     * @param modelMap
     * @return
     *//*
    @ApiOperation("跳转到statics页面")
    @RequestMapping(value = "/statics.html", method = RequestMethod.GET)
    public String statics(ModelMap modelMap) {
        Date now = new Date();
        modelMap.put("createdStart", DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
        modelMap.put("createdEnd", DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
        return "sg/registerBill/statics";
    }*/

    /**
     * 查询统计数据
     *
     * @param registerBill
     * @return
     * @throws Exception
     *//*
    @RequestMapping(value = "/listStaticsPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listStaticsPage(RegisterBillDto registerBill) throws Exception {
        return this.registerBillService.listStaticsPage(registerBill);
    }*/

    /**
     * 查询统计数据
     *
     * @param registerBill
     * @return
     * @throws Exception
     *//*
    @RequestMapping(value = "/listStaticsData.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> listStaticsData(RegisterBillDto registerBill) {
        registerBill.setAttrValue(StringUtils.trimToEmpty(registerBill.getAttrValue()));
//        if (StringUtils.isBlank(registerBill.getLatestDetectTimeTimeStart())) {
//            registerBill.setLatestDetectTimeTimeStart(null);
//        }
//        if (StringUtils.isBlank(registerBill.getLatestDetectTimeTimeEnd())) {
//            registerBill.setLatestDetectTimeTimeEnd(null);
//        }

        RegisterBillStaticsDto staticsDto = this.registerBillService.groupByState(registerBill);

        return BaseOutput.success().setData(staticsDto);
    }*/

    /**
     * 交易区订单溯源页面（二维码）
     *
     * @param tradeNo
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/tradeBillDetail.html", method = RequestMethod.GET)
    public String tradeBillDetail(String tradeNo, ModelMap modelMap) {
        RegisterBillOutputDto registerBill = registerBillService.findByTradeNo(tradeNo);
        QualityTraceTradeBill qualityTraceTradeBill = qualityTraceTradeBillService.findByTradeNo(tradeNo);

        if (null != registerBill) {
            registerBill.setDetectRecord(detectRecordService.findByRegisterBillCode(registerBill.getCode()));
            registerBill.setImageCertList(this.registerBillService.findImageCertListByBillId(registerBill.getBillId()));
        }

        modelMap.put("registerBill", registerBill);
        modelMap.put("qualityTraceTradeBill", qualityTraceTradeBill);
        return "sg/registerBill/tradeBillDetail";
    }

    /**
     * 登记单溯源（二维码） 没有分销记录
     *
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/registerBillQRCode.html", method = RequestMethod.GET)
    public String registerBillQRCcode(Long id, ModelMap modelMap) {
        RegisterBill bill = billService.get(id);
        modelMap.put("registerBill", bill);
        return "sg/registerBill/registerBillQRCode";
    }

    /**
     * 登记单分销记录溯源（二维码）
     *
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/separateSalesRecordQRCode.html", method = RequestMethod.GET)
    public String separateSalesRecordQRCcode(Long id, ModelMap modelMap) {
        SeparateSalesRecord separateSalesRecord = separateSalesRecordService.get(id);
        RegisterBill bill = billService.findByCode(separateSalesRecord.getRegisterBillCode());
        modelMap.put("registerBill", bill);
        modelMap.put("separateSalesRecord", separateSalesRecord);
        return "sg/registerBill/registerBillQRCode";
    }

    /**
     * 交易单溯源（二维码） 没有分销记录
     *
     * @param id       交易单ID
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/tradeBillQRCcode.html", method = RequestMethod.GET)
    public String tradeBillQRCcode(Long id, ModelMap modelMap) {
        QualityTraceTradeBill qualityTraceTradeBill = qualityTraceTradeBillService.get(id);
        RegisterBill bill = this.billService.findByCode(qualityTraceTradeBill.getRegisterBillCode());
        modelMap.put("registerBill", bill);
        modelMap.put("qualityTraceTradeBill", qualityTraceTradeBill);
        return "sg/registerBill/tradeBillQRCode";
    }

    /**
     * 交易单分销记录溯源（二维码）
     *
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/tradeSsrQRCcode.html", method = RequestMethod.GET)
    public String tradeSsrQRCcode(Long id, ModelMap modelMap) {
        SeparateSalesRecord separateSalesRecord = separateSalesRecordService.get(id);
        QualityTraceTradeBill qualityTraceTradeBill = qualityTraceTradeBillService
                .findByTradeNo(separateSalesRecord.getTradeNo());
        RegisterBill bill = this.billService.findByCode(qualityTraceTradeBill.getRegisterBillCode());
        modelMap.put("registerBill", bill);
        modelMap.put("qualityTraceTradeBill", qualityTraceTradeBill);
        modelMap.put("separateSalesRecord", separateSalesRecord);
        return "sg/registerBill/tradeBillQRCode";
    }





    /**
     * 保存处理结果
     *
     * @param input
     * @return
     *//*
    @RequestMapping(value = "/saveHandleResult.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> saveHandleResult(RegisterBill input) {
        try {
            Long id = this.registerBillService.saveHandleResult(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }

    }*/
/*


    */
/**
     * 上传产地报告
     *
     * @param input
     * @return
     *//*

    @RequestMapping(value = "/doUploadOrigincertifiy.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doUploadOrigincertifiy(@RequestBody RegisterBill input) {
        try {
            Long id = this.registerBillService.doUploadOrigincertifiy(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    */
/**
     * 上传检测报告
     *
     * @param input
     * @return
     *//*

    @RequestMapping(value = "/doUploadDetectReport.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doUploadDetectReport(@RequestBody RegisterBill input) {
        try {
            Long id = this.registerBillService.doUploadDetectReport(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    */
/**
     * 删除检测报告及产地证明
     *
     * @param id
     * @param deleteType
     * @return
     *//*

    @RequestMapping(value = "/doRemoveReportAndCertifiy.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doRemoveReportAndCertifiy(Long id, String deleteType) {
        try {
//			Long id = this.registerBillService.doUploadDetectReport(input);
            return this.registerBillService.doRemoveReportAndCertifiy(id, deleteType);
        } catch (TraceBizException e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    */
/**
     * 保存处理结果
     *
     * @param input
     * @return
     *//*

    @RequestMapping(value = "/doAuditWithoutDetect.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doAuditWithoutDetect(RegisterBill input) {
        try {
            Long id = this.registerBillService.doAuditWithoutDetect(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }
*/


    /**
     * 保存处理结果
     *
     * @param input
     * @return
     *//*
    @RequestMapping(value = "/doEdit.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doEdit(RegisterBill input) {
        try {

            Long id = this.registerBillService.doEdit(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }*/

    /**
     * 所有状态列表
     *
     * @param
     * @return
     */
    /*@RequestMapping(value = "/listState.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public List<Map<String, String>> listState() {

        return Stream.of(RegisterBillStateEnum.values()).map(e -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", e.getCode().toString());
            map.put("name", e.getName());
            map.put("parentId", "");
            return map;

        }).collect(Collectors.toList());

    }*/

    /**
     * 权限判断并保护敏感信息
     *
     * @param dto
     * @return
     */
    private RegisterBill maskRegisterBillOutputDto(RegisterBill dto) {
        if (dto == null) {
            return dto;
        }
        if (this.uapRpcService.hasAccess("registerBill/create.html#user")) {
            return dto;
        } else {
            dto.setIdCardNo(MaskUserInfo.maskIdNo(dto.getIdCardNo()));
            dto.setAddr(MaskUserInfo.maskAddr(dto.getAddr()));
            return dto;
        }

    }

    /**
     * 保护敏感信息
     *
     * @param dto
     * @return
     */
    private UserInfoDto maskUserInfoDto(UserInfoDto dto) {

        if (dto == null) {
            return dto;
        }
        return dto.mask(true);
//        return dto.mask(!SessionContext.hasAccess("post", "registerBill/create.html#user"));
    }

}