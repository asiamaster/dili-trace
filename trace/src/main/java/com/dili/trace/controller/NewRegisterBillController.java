package com.dili.trace.controller;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.domain.*;
import com.dili.trace.dto.*;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.*;
import com.dili.trace.events.RegisterBillMessageEvent;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.dili.trace.util.BeanMapUtil;
import com.dili.trace.util.ImageCertUtil;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Api("/newRegisterBill")
@Controller
@RequestMapping("/newRegisterBill")
public class NewRegisterBillController {
    private static final Logger logger = LoggerFactory.getLogger(NewRegisterBillController.class);

    @Autowired
    BillService billService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    SgRegisterBillService sgRegisterBillService;
    @Autowired
    TradeTypeService tradeTypeService;
    @Autowired
    CustomerRpcService customerService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    UserPlateService userPlateService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    ProcessService processService;

    @Autowired
    FieldConfigDetailService fieldConfigDetailService;
    @Autowired
    EnumService enumService;
    @Autowired
    RegisterHeadService registerHeadService;
    @Autowired
    RegisterTallyAreaNoService registerTallyAreaNoService;
    @Autowired
    GlobalVarService globalVarService;


    /**
     * 跳转到RegisterBill页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到RegisterBill页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap, HttpServletRequest req) {
        BillReportQueryDto query = new BillReportQueryDto();
        Date now = new Date();
        query.setBillCreatedEnd(DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
        query.setBillCreatedStart(DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
        modelMap.put("query", query);
        UserTicket user = this.uapRpcService.getCurrentUserTicket().orElse(DTOUtils.newDTO(UserTicket.class));
        modelMap.put("user", user);
        modelMap.put("isDeleted", YesOrNoEnum.NO.getCode());
        //加载配置项，以便控制页面查询条件、列显示与否
        FieldConfigModuleTypeEnum moduleType = FieldConfigModuleTypeEnum.REGISTER;
        Map<String, FieldConfigDetailRetDto> filedNameRetMap = StreamEx.of(this.fieldConfigDetailService.findByMarketIdAndModuleType(user.getFirmId(), moduleType))
                .toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());
        modelMap.put("filedNameRetMap", filedNameRetMap);

        return "new-registerBill/index";
    }

    /**
     * 分页查询RegisterBill
     *
     * @param registerBill
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询RegisterBill", notes = "分页查询RegisterBill，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "RegisterBill", paramType = "form", value = "RegisterBill的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody RegisterBillDto registerBill) throws Exception {
        registerBill.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        List<Integer> billTypes = new ArrayList<>();
        billTypes.add(BillTypeEnum.REGISTER_BILL.getCode());
//        billTypes.add(BillTypeEnum.CHECK_ORDER.getCode());
//        billTypes.add(BillTypeEnum.CHECK_DISPOSE.getCode());
        registerBill.setBillTypes(billTypes);
        return sgRegisterBillService.listBasePageByExample(registerBill);
        // return registerBillService.listPage(registerBill);
    }

    /**
     * 分页查询RegisterBill
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询RegisterBill", notes = "分页查询RegisterBill，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "RegisterBill", paramType = "form", value = "RegisterBill的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/findHighLightBill.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    Object findHighLightBill(RegisterBillDto dto) throws Exception {
        RegisterBill registerBill = sgRegisterBillService.findHighLightBill(dto,this.uapRpcService.getCurrentOperatorOrEx());
        return BaseOutput.success().setData(registerBill);
    }

    /**
     * 登记单录入页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html")
    public String add(ModelMap modelMap) {
        modelMap.put("tradeTypes", tradeTypeService.findAll());
        modelMap.put("citys", this.queryCitys());

        Firm currentFirm = this.uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        FieldConfigModuleTypeEnum moduleType = FieldConfigModuleTypeEnum.REGISTER;

        Map<String, FieldConfigDetailRetDto> filedNameRetMap = StreamEx.of(this.fieldConfigDetailService.findByMarketIdAndModuleType(currentFirm.getId(), moduleType))
                .toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());
        modelMap.put("filedNameRetMap", JSON.toJSONString(filedNameRetMap));

        List<ImageCertTypeEnum> imageCertTypeEnumList = this.enumService.listImageCertType(currentFirm.getId(), moduleType);
        modelMap.put("imageCertTypeEnumMap", JSON.toJSONString(StreamEx.of(imageCertTypeEnumList).map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("certType", e.getCode());
            m.put("certTypeName", e.getName());
            return m;
        }).toList()));
        modelMap.put("measureTypeEnumList", JSON.toJSONString(StreamEx.of(MeasureTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));
        modelMap.put("truckTypeEnumEnumList", JSON.toJSONString(StreamEx.of(TruckTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));

        RegisterBillOutputDto item = new RegisterBillOutputDto();
        item.setMeasureType(MeasureTypeEnum.COUNT_WEIGHT.getCode());
        item.setRegistType(RegistTypeEnum.NONE.getCode());
        item.setTruckType(TruckTypeEnum.FULL.getCode());
        item.setWeightUnit(WeightUnitEnum.JIN.getCode());
        modelMap.put("item", JSON.toJSONString(item));

        return "new-registerBill/add";
    }


    /**
     * 新增
     *
     * @return
     */
    @RequestMapping(value = "/doAdd.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doAdd(@RequestBody CreateRegisterBillInputDto input) {
        Firm firm = this.uapRpcService.getCurrentFirm().orElse(null);
        if (firm == null) {
            return BaseOutput.failure("未登录");
        }

        Long userId =input.getUserId();
        if (userId == null) {
            return BaseOutput.failure("参数错误");
        }

        try {

            registerBillService.createRegisterBillList(firm.getId(), Lists.newArrayList(input)
                    , userId
                    , this.uapRpcService.getCurrentOperator()
                    , CreatorRoleEnum.MANAGER);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
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

        return "new-registerBill/create";
    }

    /**
     * 交易单复制
     *
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/copy.html", method = RequestMethod.GET)
    public String copy(Long id, ModelMap modelMap) {
        RegisterBillOutputDto registerBill = RegisterBillOutputDto.build(billService.get(id), Lists.newLinkedList());
        String firstTallyAreaNo = Stream.of(StringUtils.trimToEmpty(registerBill.getTallyAreaNo()).split(","))
                .filter(StringUtils::isNotBlank).findFirst().orElse("");
//        registerBill.setSourceName(firstTallyAreaNo);
        // registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id,ImageCertBillTypeEnum.BILL_TYPE));
        UserInfoDto userInfoDto = this.findUserInfoDto(registerBill, firstTallyAreaNo);
        modelMap.put("userInfo", this.maskUserInfoDto(userInfoDto));
        modelMap.put("tradeTypes", tradeTypeService.findAll());
        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        modelMap.put("citys", this.queryCitys());

        if (registerBill.getRegisterSource().equals(RegisterSourceEnum.TALLY_AREA.getCode())) {
            List<UserPlate> userPlateList = this.userPlateService.findUserPlateByUserId(registerBill.getUserId());
            modelMap.put("userPlateList", userPlateList);
        } else {
            modelMap.put("userPlateList", new ArrayList<>(0));
        }

        return "new-registerBill/copy";
    }

    /**
     * 交易单修改
     *
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/edit.html", method = RequestMethod.GET)
    public String edit(Long id, ModelMap modelMap) {

        Firm currentFirm = this.uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        FieldConfigModuleTypeEnum moduleType = FieldConfigModuleTypeEnum.REGISTER;

        Map<String, FieldConfigDetailRetDto> filedNameRetMap = StreamEx.of(this.fieldConfigDetailService.findByMarketIdAndModuleType(currentFirm.getId(), moduleType))
                .toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());
        modelMap.put("filedNameRetMap", JSON.toJSONString(filedNameRetMap));

        modelMap.put("measureTypeEnumList", JSON.toJSONString(StreamEx.of(MeasureTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));
        modelMap.put("truckTypeEnumEnumList", JSON.toJSONString(StreamEx.of(TruckTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));

        List<ImageCertTypeEnum> imageCertTypeEnumList = this.enumService.listImageCertType(currentFirm.getId(), moduleType);
        modelMap.put("imageCertTypeEnumMap", JSON.toJSONString(StreamEx.of(imageCertTypeEnumList).map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("certType", e.getCode());
            m.put("certTypeName", e.getName());
            return m;
        }).toList()));


        RegisterBillOutputDto registerBill = billService.getAvaiableBill(id).map(bill -> {
            return RegisterBillOutputDto.build(bill, Lists.newLinkedList());
        }).orElseGet(() -> {
            RegisterBillOutputDto item = new RegisterBillOutputDto();
            item.setMeasureType(MeasureTypeEnum.COUNT_WEIGHT.getCode());
            item.setRegistType(RegistTypeEnum.NONE.getCode());
            item.setTruckType(TruckTypeEnum.FULL.getCode());
            item.setWeightUnit(WeightUnitEnum.JIN.getCode());

            return item;
        });
        RegisterHead registerHead = this.registerHeadService.findByCode(registerBill.getRegisterHeadCode()).orElse(new RegisterHead());
        registerBill.setRegisterHead(registerHead);
        modelMap.put("item", JSON.toJSONString(registerBill));
        if (registerBill.getBillId() == null) {
            return "new-registerBill/edit";
        }
        registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id, BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null)));
        String firstTallyAreaNo = Stream.of(StringUtils.trimToEmpty(registerBill.getTallyAreaNo()).split(","))
                .filter(StringUtils::isNotBlank).findFirst().orElse("");
        registerBill.setSourceName(firstTallyAreaNo);

        List<RegisterTallyAreaNo> arrivalTallynos = this.registerTallyAreaNoService.findTallyAreaNoByBillIdAndType(registerBill.getBillId(), BillTypeEnum.REGISTER_BILL);
        registerBill.setArrivalTallynos(StreamEx.of(arrivalTallynos).map(RegisterTallyAreaNo::getTallyareaNo).toList());


        UserInfoDto userInfoDto = this.findUserInfoDto(registerBill, firstTallyAreaNo);
        modelMap.put("userInfo", this.maskUserInfoDto(userInfoDto));
        modelMap.put("tradeTypes", tradeTypeService.findAll());
        RegisterBillOutputDto registerBillOutputDto = RegisterBillOutputDto.build(this.maskRegisterBillOutputDto(registerBill), Lists.newLinkedList());

        String upstreamName = StreamEx.ofNullable(registerBillOutputDto.getUpStreamId()).map(this.upStreamService::get).nonNull().map(UpStream::getName).findFirst().orElse(null);
        registerBillOutputDto.setUpStreamName(upstreamName);

        Map<Object,Object>   item= Maps.newHashMap(new BeanMap(registerBillOutputDto));

        Map<ImageCertTypeEnum, List<ImageCert>>groupedImageList=registerBillOutputDto.getGroupedImageCertList();
        List<String>uniqueCertTypeNameList=StreamEx.of(imageCertTypeEnumList).map(e->{
            List<String>imageUidList=StreamEx.of(groupedImageList.get(e)).nonNull().map(ImageCert::getUid).nonNull().map(uid->this.globalVarService.getDfsImageViewPathPrefix()+"/"+uid).toList();
            String uniqueCertTypeName="certType"+e.getCode();
            item.put(uniqueCertTypeName,imageUidList);
            return uniqueCertTypeName;
        }).toList();
        modelMap.put("uniqueCertTypeNameList", uniqueCertTypeNameList);
        modelMap.put("item", JSON.toJSONString(item));


        modelMap.put("citys", this.queryCitys());

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        if (registerBill.getRegisterSource().equals(RegisterSourceEnum.TALLY_AREA.getCode())) {
            List<UserPlate> userPlateList = this.userPlateService.findUserPlateByUserId(registerBill.getUserId());
            modelMap.put("userPlateList", userPlateList);
        } else {
            modelMap.put("userPlateList", new ArrayList<>(0));
        }


        return "new-registerBill/edit";
    }

    /**
     * 登记单录修改页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/uploadDetectReport.html", method = RequestMethod.GET)
    public String uploadDetectReport(ModelMap modelMap, @RequestParam(name = "id", required = true) Long id) {
        RegisterBillOutputDto registerBill = billService.getAvaiableBill(id).map(bill -> {
            return RegisterBillOutputDto.build(bill, Lists.newLinkedList());
        }).orElse(null);
        if (registerBill == null) {
            return "";
        }
        registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id, BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null)));
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
            QualityTraceTradeBill condition = new QualityTraceTradeBill();
            condition.setRegisterBillCode(registerBill.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }
        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "new-registerBill/upload-detectReport";
    }

    /**
     * 上传产地证明
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/uploadOrigincertifiy.html", method = RequestMethod.GET)
    public String uploadOrigincertifiy(ModelMap modelMap, @RequestParam(name = "id", required = true) Long id) {
        RegisterBillOutputDto registerBill = billService.getAvaiableBill(id).map(bill -> {
            return RegisterBillOutputDto.build(bill, Lists.newLinkedList());
        }).orElse(null);
        if (registerBill == null) {
            return "";
        }
        registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id, BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null)));
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
            QualityTraceTradeBill condition = new QualityTraceTradeBill();
            condition.setRegisterBillCode(registerBill.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }

        // 查询检测请求
        if (registerBill.getDetectRequestId() != null) {
            DetectRequest detectRequest = detectRequestService.get(registerBill.getDetectRequestId());
            registerBill.setDetectRequest(detectRequest);
        }

        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "new-registerBill/upload-origincertifiy";
    }

    /**
     * 上传处理结果
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/uploadHandleResult.html", method = RequestMethod.GET)
    public String uploadHandleResult(ModelMap modelMap, @RequestParam(name = "id", required = true) Long id) {
        RegisterBillOutputDto registerBill = billService.getAvaiableBill(id).map(bill -> {
            return RegisterBillOutputDto.build(bill, Lists.newLinkedList());
        }).orElse(null);
        if (registerBill == null) {
            return "";
        }
        registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id, BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null)));

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
            QualityTraceTradeBill condition = new QualityTraceTradeBill();
            condition.setRegisterBillCode(registerBill.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }

        // 查询检测请求
        if (registerBill.getDetectRequestId() != null) {
            DetectRequest detectRequest = detectRequestService.get(registerBill.getDetectRequestId());
            registerBill.setDetectRequest(detectRequest);
        }

        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "new-registerBill/upload-handleresult";
    }

    /**
     * 审核页面
     *
     * @param modelMap
     * @param id
     * @return
     */
    @RequestMapping(value = "/audit.html", method = RequestMethod.GET)
    public String audit(ModelMap modelMap, @RequestParam(name = "id", required = true) Long id) {
        modelMap.put("registerBill", billService.get(id));
        return "new-registerBill/audit";
    }

    /**
     * 审核
     *
     * @param id
     * @param verifyStatus
     * @return
     */
    @RequestMapping(value = "/doAudit.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doAudit(@RequestParam(name = "id", required = true) Long id, @RequestParam(name = "verifyStatus", required = true) Integer verifyStatus) {
        try {
            BillVerifyStatusEnum billVerifyStatusEnum = BillVerifyStatusEnum.fromCode(verifyStatus).orElse(null);
            if (billVerifyStatusEnum == null) {
                return BaseOutput.failure("审核状态错误");
            }
            sgRegisterBillService.auditRegisterBill(id, billVerifyStatusEnum,this.uapRpcService.getCurrentOperatorOrEx());
            if (BillVerifyStatusEnum.PASSED == billVerifyStatusEnum) {
                Long marketId = this.uapRpcService.getCurrentFirm().get().getId();
                processService.afterBillPassed(id, marketId, this.uapRpcService.getCurrentOperator());
            }
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 批量主动送检
     *
     * @param modelMap
     * @param idList
     * @return
     */
    @RequestMapping(value = "/doBatchAutoCheck.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchAutoCheck(ModelMap modelMap, @RequestBody List<Long> idList) {
//		modelMap.put("registerBill", registerBillService.get(id));
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.sgRegisterBillService.doBatchAutoCheck(idList,this.uapRpcService.getCurrentOperatorOrEx());
    }

    /**
     * 批量撤销
     *
     * @param modelMap
     * @param idList
     * @return
     */
    @RequestMapping(value = "/doBatchUndo.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchUndo(ModelMap modelMap, @RequestBody List<Long> idList) {
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.sgRegisterBillService.doBatchUndo(idList,this.uapRpcService.getCurrentOperatorOrEx());
    }

    /**
     * 批量采样检测
     *
     * @param modelMap
     * @param idList
     * @return
     */
    @RequestMapping(value = "/doBatchSamplingCheck.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doBatchSamplingCheck(ModelMap modelMap, @RequestBody List<Long> idList) {
//		modelMap.put("registerBill", registerBillService.get(id));
        idList = CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return BaseOutput.failure("参数错误");
        }
        return this.sgRegisterBillService.doBatchSamplingCheck(idList,this.uapRpcService.getCurrentOperatorOrEx());
    }

    /**
     * 批量审核
     *
     * @param modelMap
     * @param batchAuditDto
     * @return
     */
    @RequestMapping(value = "/doBatchAudit.action", method = RequestMethod.POST)
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
        batchAuditDto.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        batchAuditDto.setRegisterBillIdList(idList);
        return this.sgRegisterBillService.doBatchAudit(batchAuditDto,this.uapRpcService.getCurrentOperatorOrEx());
    }

    /**
     * 撤销
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doUndo.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doUndo(@RequestParam(name = "id", required = true) Long id) {
        try {
            sgRegisterBillService.undoRegisterBill(id,this.uapRpcService.getCurrentOperatorOrEx());
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
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id
            , @RequestParam(required = false, name = "displayWeight") Boolean displayWeight) {

        Firm currentFirm = this.uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        FieldConfigModuleTypeEnum moduleType = FieldConfigModuleTypeEnum.REGISTER;

        Map<String, FieldConfigDetailRetDto> filedNameRetMap = StreamEx.of(this.fieldConfigDetailService.findByMarketIdAndModuleType(currentFirm.getId(), moduleType))
                .toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());
        modelMap.put("filedNameRetMap", filedNameRetMap);

        modelMap.put("measureTypeEnumList", JSON.toJSONString(StreamEx.of(MeasureTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));
        modelMap.put("truckTypeEnumEnumList", JSON.toJSONString(StreamEx.of(TruckTypeEnum.values()).map(BeanMapUtil::beanToMap).toList()));

        List<ImageCertTypeEnum> imageCertTypeEnumList = this.enumService.listImageCertType(currentFirm.getId(), moduleType);
        modelMap.put("imageCertTypeEnumList", imageCertTypeEnumList);

        RegisterBill item = billService.get(id);
        if (item == null) {

            modelMap.put("registerBill", new RegisterBillOutputDto());

            return "new-registerBill/view";
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
            QualityTraceTradeBill condition = new QualityTraceTradeBill();
            condition.setRegisterBillCode(item.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }

        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        modelMap.put("displayWeight", displayWeight);

        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        DetectRequest detectRequest = detectRequestService.get(registerBill.getDetectRequestId());
        modelMap.put("detectRequest", detectRequest);
        if (null != registerBill.getPieceNum()) {
            modelMap.put("pieceNum", registerBill.getPieceNum().setScale(0, BigDecimal.ROUND_DOWN));
        } else {
            modelMap.put("pieceNum", null);
        }
        if (null != registerBill.getPieceWeight()) {
            modelMap.put("pieceWeight", registerBill.getPieceWeight().setScale(0, BigDecimal.ROUND_DOWN));
        } else {
            modelMap.put("pieceWeight", null);
        }
        if (null != registerBill.getTruckTareWeight()) {
            modelMap.put("truckTareWeight", registerBill.getTruckTareWeight().setScale(0, BigDecimal.ROUND_DOWN));
        } else {
            modelMap.put("truckTareWeight", null);
        }
        if (null != registerBill.getUpStreamId()) {
            UpStream upStream = upStreamService.get(registerBill.getUpStreamId());
            String upStreamName = Optional.ofNullable(upStream).orElse(new UpStream()).getName();
            modelMap.put("upStreamName", upStreamName);
        }
        RegisterBillOutputDto bill = buildRegisterBill(id);
        List<RegisterTallyAreaNo> arrivalTallynos = this.registerTallyAreaNoService.findTallyAreaNoByBillIdAndType(bill.getBillId(), BillTypeEnum.REGISTER_BILL);
        bill.setArrivalTallynos(StreamEx.of(arrivalTallynos).map(RegisterTallyAreaNo::getTallyareaNo).toList());
        modelMap.put("registerBill", bill);

        return "new-registerBill/view";
    }

    /**
     * 构建报备信息
     *
     * @param billId
     * @return
     */
    private RegisterBillOutputDto buildRegisterBill(Long billId) {
        RegisterBill item = billService.get(billId);
        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        List<ImageCert> imageCerts = this.sgRegisterBillService.findImageCertListByBillId(item.getBillId());
        registerBill.setImageCertList(imageCerts);
        return registerBill;
    }

    /**
     * 自动送检
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doAutoCheck.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doAutoCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            sgRegisterBillService.autoCheckRegisterBill(id,this.uapRpcService.getCurrentOperatorOrEx());
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
    @RequestMapping(value = "/doSamplingCheck.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doSamplingCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            sgRegisterBillService.samplingCheckRegisterBill(id,this.uapRpcService.getCurrentOperatorOrEx());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 复检
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doReviewCheck.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doReviewCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            sgRegisterBillService.reviewCheckRegisterBill(id,this.uapRpcService.getCurrentOperatorOrEx());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 保存处理结果
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doUploadHandleResult.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doUploadHandleResult(@RequestBody RegisterBill input) {
        try {
            List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();
            input.setImageCertList(imageCertList);
            Long id = this.sgRegisterBillService.doUploadHandleResult(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 上传产地报告
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doUploadOrigincertifiy.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doUploadOrigincertifiy(@RequestBody RegisterBill input) {
        try {
            List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();
            input.setImageCertList(imageCertList);
            Long id = this.sgRegisterBillService.doUploadOrigincertifiy(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 上传检测报告
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doUploadDetectReport.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doUploadDetectReport(@RequestBody RegisterBill input) {
        try {
            List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();
            input.setImageCertList(imageCertList);
            Long id = this.sgRegisterBillService.doUploadDetectReport(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 构造 ImageCert
     *
     * @param billId
     * @param s
     * @param certType
     * @return
     */
    private ImageCert buildImageCert(Long billId, String s, Integer certType) {
        ImageCert imageCert = new ImageCert();
        imageCert.setBillId(billId);
        imageCert.setCertType(certType);
        imageCert.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        imageCert.setUid(s);
        return imageCert;
    }

    /**
     * 删除检测报告及产地证明
     *
     * @param removeDto
     * @return
     */
    @RequestMapping(value = "/doRemoveReportAndCertifiy.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doRemoveReportAndCertifiy(@RequestBody ReportAndCertifiyRemoveDto removeDto) {
        try {
//			Long id = this.registerBillService.doUploadDetectReport(input);
            return this.sgRegisterBillService.doRemoveReportAndCertifiy(removeDto.getId(), removeDto.getDeleteType());
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 保存处理结果
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doAuditWithoutDetect.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doAuditWithoutDetect(RegisterBill input) {
        try {
            Long id = this.sgRegisterBillService.doAuditWithoutDetect(input);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 保存处理结果
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doEdit.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> doEdit(@RequestBody RegisterBill input) {
        try {
            Long id = this.registerBillService.doEdit(input,input.getImageCertList(),this.uapRpcService.getCurrentOperator());
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 新增RegisterBill
     *
     * @param input
     * @return
     */
    @ApiOperation("新增RegisterBill")
    @RequestMapping(value = "/insert.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput insert(@RequestBody CreateListBillParam input) {
        if (input == null) {
            return BaseOutput.failure("参数错误");
        }

        List<RegisterBill> billList = StreamEx.ofNullable(input.getRegisterBills()).flatCollection(Function.identity())
                .nonNull()
                .map(rbInputDto -> {
                    CustomerExtendDto customer = new CustomerExtendDto();
                    RegisterBill rb = rbInputDto.build(customer, this.uapRpcService.getCurrentFirm().orElse(null).getId());


                    rb.setCardNo(input.getCardNo());
                    rb.setName(input.getName());
                    rb.setIdCardNo(input.getIdCardNo());
                    rb.setAddr(input.getAddr());
                    rb.setPhone(input.getPhone());
                    rb.setPlate(input.getPlate());
                    rb.setUserId(input.getUserId());
                    rb.setImageCertList(input.getGlobalImageCertList());

                    // 保存产地证明
                    List<ImageCert> imageList = StreamEx.ofNullable(input.getGlobalImageCertList()).flatCollection(Function.identity()).nonNull().toList();
//                    if (StringUtils.isNotBlank(rbInputDto.getOriginCertifiyUrl())) {
//                        List<ImageCert> imageCerts = rb.getImageCerts();
//                        ImageCert imageCert = new ImageCert();
//                        imageCert.setUid(rbInputDto.getOriginCertifiyUrl());
//                        imageCert.setCertType(ImageCertTypeEnum.ORIGIN_CERTIFIY.getCode());
//                        imageCerts.add(imageCert);
                    rb.setImageCertList(imageList);
//                    }
                    rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
                    rb.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
                    rb.setRegisterSource(RegisterSourceEnum.getRegisterSourceEnum(input.getRegisterSource()).orElse(RegisterSourceEnum.OTHERS).getCode());
                    if (RegisterSourceEnum.TRADE_AREA.getCode().equals(input.getRegisterSource())) {
                        rb.setSourceName(input.getSourceName());
                    } else {
                        rb.setSourceName(input.getTallyAreaNo());
                    }
                    rb.setSourceId(input.getSourceId());
                    rb.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
                    rb.setPreserveType(PreserveTypeEnum.NONE.getCode());
                    rb.setVerifyType(VerifyTypeEnum.NONE.getCode());
                    rb.setTruckType(TruckTypeEnum.FULL.getCode());
//                    rb.setIsCheckin(YesOrNoEnum.NO.getCode());
                    rb.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
                    rb.setIsDeleted(YesOrNoEnum.NO.getCode());
                    rb.setMeasureType(MeasureTypeEnum.COUNT_WEIGHT.getCode());
                    rb.setRegistType(RegistTypeEnum.NONE.getCode());
                    rb.setCreatorRole(CreatorRoleEnum.MANAGER.getCode());

                    CustomerExtendDto customerExtendDto = this.customerService.findApprovedCustomerByIdOrEx(rb.getUserId(), rb.getMarketId());

                    TraceCustomer cq = new TraceCustomer();
                    cq.setCode(customerExtendDto.getCode());
                    this.customerService.findCustomer(cq, rb.getMarketId()).ifPresent(card -> {
                        rb.setThirdPartyCode(card.getCardNo());
                    });

                    return rb;
                }).toList();
        try {
            OperatorUser operatorUser = this.uapRpcService.getCurrentOperator().get();
            this.sgRegisterBillService.createRegisterBillList(billList, operatorUser);
            return BaseOutput.success("新增成功").setData(billList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("服务器出错,请重试");
        }
    }


    /**
     * 跳转到statics页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到修改图片页面")
    @RequestMapping(value = "/update_image.html", method = RequestMethod.GET)
    public String update_image(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id,
                               @RequestParam(required = false, name = "displayWeight") Boolean displayWeight) {

        RegisterBill item = billService.get(id);
        if (item == null) {
            modelMap.put("registerBill", item);
            return "new-registerBill/update_image";
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
            QualityTraceTradeBill condition = new QualityTraceTradeBill();
            condition.setRegisterBillCode(item.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        modelMap.put("displayWeight", displayWeight);
        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        List<ImageCert> imageCerts = this.sgRegisterBillService.findImageCertListByBillId(item.getBillId());
        registerBill.setImageCertList(imageCerts);
        modelMap.put("registerBill", registerBill);
        return "new-registerBill/update_image";
    }

    /**
     * 更新图片
     *
     * @param registerBill
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateImage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput updateImage(@RequestBody RegisterBill registerBill) {
        try {
            this.registerBillService.doUpdateImage(registerBill);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
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
     * 查找用户信息
     *
     * @param registerBill
     * @param firstTallyAreaNo
     * @return
     */
    private UserInfoDto findUserInfoDto(RegisterBill registerBill, String firstTallyAreaNo) {
        UserInfoDto userInfoDto = new UserInfoDto();


        TraceCustomer condition = new TraceCustomer();
        condition.setCode(registerBill.getTradeAccount());
        condition.setCardNo(StringUtils.trimToNull(registerBill.getCardNo()));
        TraceCustomer traceCustomer = this.customerService.findCustomer(condition, this.uapRpcService.getCurrentFirm().get().getId()).orElse(null);
        if (traceCustomer != null) {
            userInfoDto.setUserId(String.valueOf(traceCustomer.getId()));
            userInfoDto.setName(traceCustomer.getName());
            userInfoDto.setIdCardNo(traceCustomer.getIdNo());
            userInfoDto.setPhone(traceCustomer.getPhone());
            userInfoDto.setAddr(traceCustomer.getAddress());
            userInfoDto.setPrintingCard(traceCustomer.getCardNo());
        }

        return userInfoDto;
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
        return dto.mask(!this.uapRpcService.hasAccess( "registerBill/create.html#user"));
    }

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
        if (this.uapRpcService.hasAccess( "registerBill/create.html#user")) {
            return dto;
        } else {
            dto.setIdCardNo(MaskUserInfo.maskIdNo(dto.getIdCardNo()));
            dto.setAddr(MaskUserInfo.maskAddr(dto.getAddr()));
            return dto;
        }

    }

    /**
     * 查询当前controller可用事件
     *
     * @param billIdList
     * @return
     */
    @RequestMapping("/queryEvents.action")
    @ResponseBody
    public List<String> queryEvents(@RequestBody List<Long> billIdList) {
        List<RegisterBillMessageEvent> list = Lists.newArrayList(RegisterBillMessageEvent.add, RegisterBillMessageEvent.export);
        if (billIdList == null || billIdList.size() == 0) {
            return StreamEx.of(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        }
        list.add(RegisterBillMessageEvent.DETAIL);
        if (billIdList.size() == 1) {
            return StreamEx.of(this.sgRegisterBillService.queryEvents(billIdList.get(0))).append(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        } else {
            return StreamEx.of(this.queryBatchEvents(billIdList)).append(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        }

    }

    /**
     * SB
     *
     * @param idList
     * @return
     */
    private List<RegisterBillMessageEvent> queryBatchEvents(List<Long> idList) {

        boolean allEcBill = StreamEx.of(this.billService.findByIdList(idList)).map(RegisterBill::getBillType).allMatch(bt -> BillTypeEnum.E_COMMERCE_BILL.equalsToCode(bt));
        if (allEcBill) {
            return Lists.newArrayList();
        }
        Map<RegisterBillMessageEvent, Boolean> eventCount = StreamEx.ofNullable(idList).flatCollection(Function.identity()).nonNull().flatMap(bid -> {
            return StreamEx.of(this.sgRegisterBillService.queryEvents(bid));
        }).distinct().mapToEntry(v -> v, v -> v).collapseKeys().mapValues(v -> v.size() > 0).toMap();

        List<RegisterBillMessageEvent> batchEventList = Lists.newLinkedList();
        if (eventCount.getOrDefault(RegisterBillMessageEvent.audit, false)) {
            batchEventList.add(RegisterBillMessageEvent.batch_audit);
        }

        if (eventCount.getOrDefault(RegisterBillMessageEvent.audit, false)) {
            batchEventList.add(RegisterBillMessageEvent.batch_audit);
        }

        if (eventCount.getOrDefault(RegisterBillMessageEvent.auto, false)) {
            batchEventList.add(RegisterBillMessageEvent.batch_auto);
        }

        if (eventCount.getOrDefault(RegisterBillMessageEvent.sampling, false)) {
            batchEventList.add(RegisterBillMessageEvent.batch_sampling);
        }

        if (eventCount.getOrDefault(RegisterBillMessageEvent.undo, false)) {
            batchEventList.add(RegisterBillMessageEvent.batch_undo);
        }
        return batchEventList;
    }

}