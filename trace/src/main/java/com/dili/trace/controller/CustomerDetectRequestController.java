package com.dili.trace.controller;

import com.dili.trace.events.DetectRequestMessageEvent;
import com.dili.common.exception.TraceBizException;
import com.dili.sg.trace.glossary.SalesTypeEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.*;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.dto.*;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.service.*;
import com.dili.trace.util.MarketUtil;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
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

/**
 * 检测请求业务类
 */
@Api("/customerDetectRequest")
@Controller
@RequestMapping("/customerDetectRequest")
public class CustomerDetectRequestController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDetectRequestController.class);

    @Autowired
    DetectRequestService detectRequestService;

    @Autowired
    UserRpcService userRpcService;

    @Autowired
    BillService billService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    UpStreamService upStreamService;

    /**
     * 跳转到DetectRequest页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到DetectRequest页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap, HttpServletRequest req) {
        return "customerDetectRequest/index";
    }

    /**
     * 分页查询DetectRequest
     *
     * @param detectRequestDto 查询条件
     * @return DetectRequest 列表
     * @throws Exception
     */
    @ApiOperation(value = "分页查询DetectRequest", notes = "分页查询DetectRequest，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "DetectRequest", paramType = "form", value = "DetectRequest的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody DetectRequestWithBillDto detectRequestDto) throws Exception {
        detectRequestDto.setMarketId(MarketUtil.returnMarket());
        List<Integer> billTypes = new ArrayList<>();
        billTypes.add(BillTypeEnum.REGISTER_BILL.getCode());
        billTypes.add(BillTypeEnum.CHECK_ORDER.getCode());
        billTypes.add(BillTypeEnum.CHECK_DISPOSE.getCode());
        detectRequestDto.setBillTypes(billTypes);
        //detectRequestDto.setIsDeleted(YesOrNoEnum.NO.getCode());
        EasyuiPageOutput out = this.detectRequestService.listBasePageByExample(detectRequestDto);
        return out.toString();
    }

    /**
     * 接单页面
     *
     * @param modelMap
     * @param billId
     * @return
     */
    @RequestMapping(value = "/confirm.html", method = RequestMethod.GET)
    public String assign(ModelMap modelMap, @RequestParam(name = "billId", required = true) Long billId) {
        RegisterBill registerBill = this.billService.getAvaiableBill(billId).orElse(null);
        DetectRequest detectRequest = StreamEx.ofNullable(registerBill).map(RegisterBill::getDetectRequestId).nonNull().map(this.detectRequestService::get).findFirst().orElse(null);
        modelMap.put("detectRequest", detectRequest);
        modelMap.put("registerBill", registerBill);
        return "customerDetectRequest/confirm";
    }

    /**
     * 接单
     *
     * @param billId             检测请求ID
     * @param designatedId   检测员ID
     * @param designatedName 检测员姓名
     * @return 指派结果
     */
    @RequestMapping(value = "/doConfirm.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doAssign(@RequestParam(name = "billId", required = true) Long billId,
                        @RequestParam(name = "designatedId", required = false) Long designatedId,
                        @RequestParam(name = "designatedName", required = false) String designatedName,
                        @RequestParam(name = "detectTime", required = false) Date detectTime) {
        try {
            this.detectRequestService.confirm(billId, designatedId, designatedName, detectTime);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 退回
     *
     * @param input
     * @return 指派结果
     */
    @RequestMapping(value = "/doReturn.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doReturn(@RequestBody RegisterBill input) {
        try {
            this.detectRequestService.doReturn(input);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 撤销
     *
     * @param billId
     * @return
     */
    @RequestMapping(value = "/doUndo.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doUndo(@RequestParam(name = "billId", required = true) Long billId) {
        try {
            this.detectRequestService.undoDetectRequest(billId);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 根据名字查询检测员信息
     *
     * @param name
     * @return
     */
    @RequestMapping("/detector")
    @ResponseBody
    public Map<String, ?> listByName(String name) {
        Long marketId = MarketUtil.returnMarket();
        List<User> detectorUsers = userRpcService.findDetectDepartmentUsers(name, marketId);
        List<Map<String, Object>> list = Lists.newArrayList();
        for (User c : detectorUsers) {
            Map<String, Object> obj = Maps.newHashMap();
            obj.put("id", c.getId());
            obj.put("data", name);
            obj.put("value", c.getRealName());
            list.add(obj);
        }

        Map map = Maps.newHashMap();
        map.put("suggestions", list);
        return map;
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
        List<DetectRequestMessageEvent> list = Lists.newArrayList();
        if (billIdList == null || billIdList.size() == 0) {
            return StreamEx.of(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        }
        // 只要有记录，可以导出和查看详情
        list.add(DetectRequestMessageEvent.export);
        list.add(DetectRequestMessageEvent.detail);
        if (billIdList.size() == 1) {
            return StreamEx.of(this.detectRequestService.queryEvents(billIdList.get(0))).append(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        } else {
            return StreamEx.of(list).map(msg -> {
                return msg.getCode();
            }).nonNull().toList();
        }
    }

    /**
     * 预约申请
     *
     * @param billId
     * @return
     */
    @RequestMapping(value = "/doBookingRequest.action", method = RequestMethod.GET)
    public @ResponseBody
    BaseOutput doBookingRequest(@RequestParam(name = "billId", required = true) Long billId) {
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            this.detectRequestService.bookingRequest(billId, userTicket);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 人工检测
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doManualCheck.action")
    public @ResponseBody
    BaseOutput doManualCheck(@RequestBody DetectRecordInputDto input) {
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            input.setCreated(DateUtils.getCurrentDate());
            input.setModified(DateUtils.getCurrentDate());
            this.detectRecordService.saveDetectRecordManually(input, Optional.of(new OperatorUser(userTicket.getId(),userTicket.getRealName())));
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
    public String view(ModelMap modelMap, @RequestParam(required = true, name = "billId") Long billId
            , @RequestParam(required = false, name = "displayWeight") Boolean displayWeight) {
        RegisterBill item = billService.get(billId);
        if (item == null) {
            modelMap.put("registerBill", item);
            return "customerDetectRequest/view";
        }

        if (null != item.getPieceNum()) {
            modelMap.put("pieceNum", item.getPieceNum().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("pieceNum", null);
        }
        if (null != item.getPieceWeight()) {
            modelMap.put("pieceWeight", item.getPieceWeight().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("pieceWeight", null);
        }

        DetectRequest detectRequest = detectRequestService.get(item.getDetectRequestId());
        modelMap.put("detectRequest", detectRequest);
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
        if (null != item.getUpStreamId()) {
            UpStream upStream = upStreamService.get(item.getUpStreamId());
            String upStreamName = Optional.ofNullable(upStream).orElse(new UpStream()).getName();
            modelMap.put("upStreamName", upStreamName);
        }

//        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
//        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);

        List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
        item.setImageCertList(imageCerts);

        RegisterBillOutputDto bill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(item,bill);
        modelMap.put("registerBill", bill);

        return "customerDetectRequest/view";
    }

    /**
     * 登记单录查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/manualCheck_confirm.html", method = RequestMethod.GET)
    public String manualCheckConfirm(ModelMap modelMap, @RequestParam(required = true, name = "billId") Long billId) {
        try {
            RegisterBill item = billService.getAvaiableBill(billId).orElse(null);
            if (item == null) {
                modelMap.put("registerBill", item);
                return "customerDetectRequest/view";
            }
            List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
            modelMap.put("detectRecordList", detectRecordList);

            List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
            item.setImageCertList(imageCerts);
            modelMap.put("registerBill", item);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "customerDetectRequest/manualCheck_confirm";
    }


    /**
     * 抽检页面
     *
     * @param billId
     * @return
     */
    @RequestMapping(value = "/spotCheck.html", method = RequestMethod.GET)
    public String soptCheck(ModelMap modelMap,@RequestParam(name = "billId", required = true) Long billId) {
        try {
            modelMap.put("registerBill",billService.get(billId));
        } catch (TraceBizException e) {
            if(logger.isErrorEnabled()){
                logger.error(e.getMessage());
            }
        }
        return "customerDetectRequest/spotCheck";
    }

    /**
     * 抽检
     *
     * @param record
     * @return
     */
    @RequestMapping(value = "/doSpotCheck.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doSpotCheck(@RequestBody  DetectRecordParam record) {
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            this.detectRequestService.spotCheckBill(record, userTicket);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }


    /**
     * 上传抽检结果页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/unqualifiedHandle.html", method = RequestMethod.GET)
    public String unqualifiedHandle(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id) {
        try {
            RegisterBill item = billService.get(id);
            RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
            BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
            List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
            registerBill.setImageCertList(imageCerts);
            modelMap.put("registerBill", registerBill);
        }catch (Exception e){
            if(logger.isErrorEnabled()){
                logger.error(e.getMessage());
            }
        }
        return "customerDetectRequest/unqualified_handle";
    }

    /**
     * 不合格处置
     * @param bill
     * @return
     */
    @RequestMapping(value = "/uploadUnqualifiedHandle.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput uploadUnqualifiedHandle(@RequestBody  RegisterBillOutputDto bill) {
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            this.detectRequestService.uploadUnqualifiedHandle(bill, userTicket);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
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
}