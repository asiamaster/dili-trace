package com.dili.trace.controller;

import com.dili.trace.events.RegisterBillMessageEvent;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.sg.trace.glossary.SalesTypeEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.*;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.dto.*;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.*;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
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
    SgRegisterBillService registerBillService;
    @Autowired
    TradeTypeService tradeTypeService;
    @Autowired
    CustomerRpcService customerService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    UserService userService;
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
        billTypes.add(BillTypeEnum.CHECK_ORDER.getCode());
        billTypes.add(BillTypeEnum.CHECK_DISPOSE.getCode());
        registerBill.setBillTypes(billTypes);
        return registerBillService.listBasePageByExample(registerBill);
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
        RegisterBill registerBill = registerBillService.findHighLightBill(dto);
        return BaseOutput.success().setData(registerBill);
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
        RegisterBillOutputDto registerBill = billService.getAvaiableBill(id).map(bill -> {
            return RegisterBillOutputDto.build(bill, Lists.newLinkedList());
        }).orElse(null);
        if (registerBill == null) {
            return "";
        }
        registerBill.setImageCertList(this.imageCertService.findImageCertListByBillId(id,BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null)));
        String firstTallyAreaNo = Stream.of(StringUtils.trimToEmpty(registerBill.getTallyAreaNo()).split(","))
                .filter(StringUtils::isNotBlank).findFirst().orElse("");
        registerBill.setSourceName(firstTallyAreaNo);

        UserInfoDto userInfoDto = this.findUserInfoDto(registerBill, firstTallyAreaNo);
        modelMap.put("userInfo", this.maskUserInfoDto(userInfoDto));
        modelMap.put("tradeTypes", tradeTypeService.findAll());
        modelMap.put("registerBill", this.maskRegisterBillOutputDto(registerBill));

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
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
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
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
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
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
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
            registerBillService.auditRegisterBill(id, billVerifyStatusEnum);
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
        return this.registerBillService.doBatchAutoCheck(idList);
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
        return this.registerBillService.doBatchUndo(idList);
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
        return this.registerBillService.doBatchSamplingCheck(idList);
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
        return this.registerBillService.doBatchAudit(batchAuditDto);
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
            registerBillService.undoRegisterBill(id);
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
        RegisterBill item = billService.get(id);
        if (item == null) {

            modelMap.put("registerBill", item);

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
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
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
        }else{
            modelMap.put("pieceNum", null);
        }
        if (null != registerBill.getPieceWeight()) {
            modelMap.put("pieceWeight", registerBill.getPieceWeight().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("pieceWeight", null);
        }
        if (null != registerBill.getTruckTareWeight()) {
            modelMap.put("truckTareWeight", registerBill.getTruckTareWeight().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("truckTareWeight", null);
        }
        if (null != registerBill.getUpStreamId()) {
            UpStream upStream = upStreamService.get(registerBill.getUpStreamId());
            String upStreamName = Optional.ofNullable(upStream).orElse(new UpStream()).getName();
            modelMap.put("upStreamName", upStreamName);
        }
        RegisterBillOutputDto bill = buildRegisterBill(id);
        modelMap.put("registerBill", bill);

        return "new-registerBill/view";
    }

    /**
     * 构建报备信息
     * @param billId
     * @return
     */
    private RegisterBillOutputDto buildRegisterBill(Long billId) {
        RegisterBill item = billService.get(billId);
        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
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
            registerBillService.autoCheckRegisterBill(id);
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
            registerBillService.samplingCheckRegisterBill(id);
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
            registerBillService.reviewCheckRegisterBill(id);
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
            Long id = this.registerBillService.doUploadHandleResult(input);
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
            Long id = this.registerBillService.doUploadOrigincertifiy(input);
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
            Long id = this.registerBillService.doUploadDetectReport(input);
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
            return this.registerBillService.doRemoveReportAndCertifiy(removeDto.getId(), removeDto.getDeleteType());
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
            Long id = this.registerBillService.doAuditWithoutDetect(input);
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
    public BaseOutput<?> doEdit(RegisterBill input) {
        try {
            Long id = this.registerBillService.doEdit(input);
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


                    rb.setTradePrintingCard(input.getTradePrintingCard());
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

                    Customer cq = new Customer();
                    cq.setCustomerId(customerExtendDto.getCode());
                    this.customerService.findCustomer(cq, rb.getMarketId()).ifPresent(card -> {
                        rb.setThirdPartyCode(card.getPrintingCard());
                    });

                    return rb;
                }).toList();
        try {
            OperatorUser operatorUser = this.uapRpcService.getCurrentOperator().get();
            this.registerBillService.createRegisterBillList(billList, operatorUser);
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
    @ApiOperation("跳转到statics页面")
    @RequestMapping(value = "/statics.html", method = RequestMethod.GET)
    public String statics(ModelMap modelMap) {
        Date now = new Date();
        modelMap.put("createdStart", DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
        modelMap.put("createdEnd", DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
        return "detectReport/statics";
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
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
            condition.setRegisterBillCode(item.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        modelMap.put("displayWeight", displayWeight);
        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
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
     * 显示条数
     *
     * @param registerBill
     * @return
     */
    private RegisterBillStaticsDto buildBillStatic(RegisterBillDto registerBill) {
        List<RegisterBill> billList = billService.listByExample(registerBill);
        Map<Long, DetectRequest> idAndDetectRquestMap = this.detectRequestService.findDetectRequestByIdList(StreamEx.of(billList).map(RegisterBill::getDetectRequestId).toList());
        //检测值
        StreamEx.of(billList).forEach(rb -> {
            rb.setDetectRequest(idAndDetectRquestMap.get(rb.getDetectRequestId()));
        });
        RegisterBillStaticsDto rbd = new RegisterBillStaticsDto();
        StreamEx.of(billList).nonNull().forEach(b -> {
            DetectRequest detectRequest = b.getDetectRequest();
            //有产地证明
            if (YesOrNoEnum.YES.getCode().equals(b.getHasOriginCertifiy())) {
                rbd.setHasOriginCertifiyNum(rbd.getHasOriginCertifiyNum() + 1);
            }
            //有检测报告
            if (YesOrNoEnum.YES.getCode().equals(b.getHasDetectReport())) {
                rbd.setHasDetectReportNum(rbd.getHasDetectReportNum() + 1);
            }
            if (null != detectRequest) {
                if (null != detectRequest.getDetectResult()) {
                    //检测合格
                    if (BillDetectStateEnum.PASS.getCode().equals(detectRequest.getDetectResult()) || BillDetectStateEnum.REVIEW_PASS.getCode().equals(detectRequest.getDetectResult())) {
                        rbd.setPassNum(rbd.getPassNum() + 1);
                    }
                    //检测不合格
                    if (BillDetectStateEnum.NO_PASS.getCode().equals(detectRequest.getDetectResult()) || BillDetectStateEnum.REVIEW_NO_PASS.getCode().equals(detectRequest.getDetectResult())) {
                        rbd.setNopassNum(rbd.getNopassNum() + 1);
                    }
                }

                if (null != detectRequest.getDetectType()) {
                    //检测采样
                    if (SampleSourceEnum.SAMPLE_CHECK.getCode().equals(detectRequest.getDetectType())) {
                        rbd.setCheckNum(rbd.getCheckNum() + 1);
                    }
                    //复检
                    if (DetectTypeEnum.RECHECK.getCode().equals(detectRequest.getDetectType())) {
                        rbd.setRecheckNum(rbd.getRecheckNum() + 1);
                    }
                }
                //主动送检
                if (null != detectRequest.getDetectSource() && SampleSourceEnum.AUTO_CHECK.getCode().equals(detectRequest.getDetectSource())) {
                    rbd.setAutoCheckNum(rbd.getAutoCheckNum() + 1);
                }
            }
            //有无打印报告
            if (null != b.getCheckSheetId()) {
                rbd.setHasCheckSheetNum(rbd.getHasCheckSheetNum() + 1);
            }
            //打印
            if (null != b.getIsPrintCheckSheet()) {
                rbd.setDiffCheckSheetNum(rbd.getDiffCheckSheetNum() + 1);
            }
        });
        return rbd;
    }

    /**
     * 查询统计数据
     *
     * @param registerBill
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listStaticsPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listStaticsPage(@RequestBody RegisterBillDto registerBill) throws Exception {
        registerBill.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        registerBill.setIsDeleted(YesOrNoEnum.NO.getCode());

        return this.registerBillService.listStaticsPage(registerBill);
    }

    /**
     * 查询各个统计数据(与列表查询条件一致)
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listStaticsPageNum.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput listStaticsPageNum(@RequestBody RegisterBillDto registerBill) throws Exception {
        registerBill.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        registerBill.setIsDeleted(YesOrNoEnum.NO.getCode());

        RegisterBillStaticsDto registerBillStaticsDto = buildBillStatic(registerBill);
        return BaseOutput.success().setData(registerBillStaticsDto);
    }

    /**
     * 查询统计数据
     *
     * @param registerBill
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listStaticsData.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> listStaticsData(RegisterBillDto registerBill) {
        registerBill.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        registerBill.setIsDeleted(YesOrNoEnum.NO.getCode());
        registerBill.setAttrValue(StringUtils.trimToEmpty(registerBill.getAttrValue()));
        RegisterBillStaticsDto staticsDto = this.registerBillService.groupByState(registerBill);
        return BaseOutput.success().setData(staticsDto);
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
        if (registerBill.getRegisterSource().intValue() == RegisterSourceEnum.TALLY_AREA.getCode().intValue()) {
            // 理货区
            UserInfo user = userService.findByTallyAreaNo(firstTallyAreaNo, this.uapRpcService.getCurrentFirm().get().getId());

            if (user != null) {
                userInfoDto.setUserId(String.valueOf(user.getId()));
                userInfoDto.setName(user.getName());
                userInfoDto.setIdCardNo(user.getCardNo());
                userInfoDto.setPhone(user.getPhone());
                userInfoDto.setAddr(user.getAddr());

            }

        } else {

            Customer condition = new Customer();
            condition.setCustomerId(StringUtils.trimToNull(registerBill.getTradeAccount()));
            condition.setPrintingCard(StringUtils.trimToNull(registerBill.getTradePrintingCard()));
            Customer customer = this.customerService.findCustomer(condition, this.uapRpcService.getCurrentFirm().get().getId()).orElse(null);
            if (customer != null) {
                userInfoDto.setUserId(customer.getCustomerId());
                userInfoDto.setName(customer.getName());
                userInfoDto.setIdCardNo(customer.getIdNo());
                userInfoDto.setPhone(customer.getPhone());
                userInfoDto.setAddr(customer.getAddress());
                userInfoDto.setPrintingCard(customer.getPrintingCard());
            }

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
        return dto.mask(!SessionContext.hasAccess("post", "registerBill/create.html#user"));
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
        if (SessionContext.hasAccess("post", "registerBill/create.html#user")) {
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
            return StreamEx.of(this.registerBillService.queryEvents(billIdList.get(0))).append(list).map(msg -> {
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
            return StreamEx.of(this.registerBillService.queryEvents(bid));
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