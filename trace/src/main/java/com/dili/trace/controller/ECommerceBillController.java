package com.dili.trace.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.dili.trace.events.ECommerceBillMessageEvent;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.*;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.service.*;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.SeperatePrintReport;
import com.dili.trace.dto.ECommerceBillInputDto;
import com.dili.trace.dto.ECommerceBillPrintOutput;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Api("/ecommerceBill")
@Controller
@RequestMapping("/ecommerceBill")
public class ECommerceBillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SgRegisterBillController.class);

    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    TradeTypeService tradeTypeService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    ECommerceBillService eCommerceBillService;
    @Autowired
    BillService billService;

    @Autowired
    ApproverInfoService approverInfoService;

    @Autowired
    SgRegisterBillService registerBillService;

    @Autowired
    SeperatePrintReportService seperatePrintReportService;
    @Autowired
    UapRpcService uapRpcService;

    /**
     * 跳转到CommissionBill页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到CommissionBill页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        Date now = new Date();
        modelMap.put("createdStart", DateUtils.format(now, "yyyy-MM-dd 00:00:00"));
        modelMap.put("createdEnd", DateUtils.format(now, "yyyy-MM-dd 23:59:59"));
        // modelMap.put("state", RegisterBillStateEnum.WAIT_CHECK.getCode());
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        modelMap.put("user", user);

        return "ecommerceBill/index";
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
            RegisterBill bill = this.eCommerceBillService.findHighLightEcommerceBill(dto, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success().setData(bill);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

    /**
     * 分页查询CommissionBill
     *
     * @param input
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询CommissionBill", notes = "分页查询CommissionBill，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CommissionBill", paramType = "form", value = "CommissionBill的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody RegisterBillDto input) throws Exception {
        return this.eCommerceBillService.listPage(input);

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
    BaseOutput insert(@RequestBody ECommerceBillInputDto input) {
        try {
            if (input == null || input.getBill() == null) {
                return BaseOutput.failure("参数错误");
            }
            input.getBill().setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
            List<SeparateSalesRecord> separateInputList = StreamEx.ofNullable(input.getSeparateSalesRecordList()).flatCollection(Function.identity()).nonNull().filter(r -> {
                return !StringUtils.isAllBlank(r.getTallyAreaNo(), r.getSalesUserName(), r.getSalesPlate());
            }).toList();
            this.eCommerceBillService.createECommerceBill(input.getBill(), separateInputList,
                    uapRpcService.getCurrentOperator().get());
            return BaseOutput.success("新增成功");
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
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

        return "ecommerceBill/create";
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
     */
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap,Long id) {
        RegisterBill bill = this.billService.get(id);
        if (bill == null) {
            return "";
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(bill.getCode());
        modelMap.put("detectRecordList", detectRecordList);

        // 分销
        List<SeparateSalesRecord> records = separateSalesRecordService.findByRegisterBillCode(bill.getCode());
        modelMap.put("separateSalesRecords", records);

        List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(bill.getBillId());
        bill.setImageCertList(imageCerts);

        modelMap.put("item", bill);
        return "ecommerceBill/view";
    }

    /**
     * 审核
     *
     * @param inputBill
     * @return
     */
    @RequestMapping(value = "/doAudit.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doAudit(@RequestBody RegisterBill inputBill) {
        if (inputBill == null || inputBill.getId() == null) {

            return BaseOutput.failure("参数错误");
        }
        try {
            this.eCommerceBillService.doAuditEcommerceBill(inputBill, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success("操作成功");
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }
    }

    /**
     * 审核
     *
     * @param inputBill
     * @return
     */
    @RequestMapping(value = "/doDelete.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doDelete(@RequestBody RegisterBill inputBill) {

        try {
            this.eCommerceBillService.doDeleteEcommerceBill(inputBill, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success("操作成功");
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }
    }

    /**
     * 所有状态列表
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/prePrint.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<ECommerceBillPrintOutput> prePrint(@RequestBody RegisterBill input) {
        if (input == null || input.getId() == null) {
            return BaseOutput.failure("参数错误");
        }

        try {
            ECommerceBillPrintOutput data = this.eCommerceBillService.prePrint(input.getId());
            return BaseOutput.success().setData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

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
        if (bill == null) {
            modelMap.put("detectRecordList", CollectionUtils.emptyCollection());
            modelMap.put("separateSalesRecords", CollectionUtils.emptyCollection());
            modelMap.put("item", new RegisterBill());
            return "ecommerceBill/qrDetail";
        }
        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(bill.getCode());
        modelMap.put("detectRecordList", detectRecordList);

        // 分销
        List<SeparateSalesRecord> records = separateSalesRecordService.findByRegisterBillCode(bill.getCode());
        modelMap.put("separateSalesRecords", records);

        modelMap.put("item", bill);
        return "ecommerceBill/billQRDetail";
    }

    /**
     * (微信扫码)电商登记单分销记录扫码页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/separateSalesQRDetail.html", method = RequestMethod.GET)
    public String separateSalesQRDetail(ModelMap modelMap, Long id) {

        SeparateSalesRecord separateSalesRecord = separateSalesRecordService.get(id);
        if (separateSalesRecord == null) {
            modelMap.put("separateSalesRecord", null);
            modelMap.put("item", new RegisterBill());
            return "ecommerceBill/separateSalesQRDetail";
        }
        RegisterBill bill = billService.findByCode(separateSalesRecord.getRegisterBillCode());
        modelMap.put("item", bill);
        modelMap.put("separateSalesRecord", separateSalesRecord);

        return "ecommerceBill/separateSalesQRDetail";
    }

    /**
     * 进入分销打印页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/prePrintSeperatePrintReport.html")
    public String prePrintSeperatePrintReport(ModelMap modelMap, Long billId) {
        SeperatePrintReport condition = new SeperatePrintReport();
        condition.setBillId(billId);
        Map<Long, SeperatePrintReport> map = StreamEx.of(this.seperatePrintReportService.listByExample(condition))
                .toMap(SeperatePrintReport::getSeperateRecocrdId, Function.identity());
//		List<Long>seperatePrintReportIdList=StreamEx.of(this.seperatePrintReportService.listByExample(condition)).map(SeperatePrintReport::getId).toList();

        List<ApproverInfo> approverInfoList = this.approverInfoService
                .listByExample(new ApproverInfo());
        modelMap.put("approverInfoList", approverInfoList);

        RegisterBill item = this.billService.get(billId);
        if (item != null && this.eCommerceBillService.supportedBillType().equalsToCode(item.getBillType())) {
            Map<Long, ApproverInfo> approverInfoMap = StreamEx.of(approverInfoList).toMap(ApproverInfo::getId,
                    Function.identity());
            List<SeperatePrintReport> separateSalesRecordList = (item == null) ? Lists.newArrayList()
                    : StreamEx.of(this.separateSalesRecordService.findByRegisterBillCode(item.getCode())).map(r -> {

                SeperatePrintReport obj = map.getOrDefault(r.getId(), new SeperatePrintReport());
                if (obj.getId() == null) {
                    obj.setSeperateRecocrdId(r.getId());
                    obj.setTallyAreaNo(r.getTallyAreaNo());
                    obj.setSalesUserName(r.getSalesUserName());
                    obj.setSalesPlate(r.getSalesPlate());
                    if (r.getSalesWeight() != null) {
                        obj.setSalesWeight(r.getSalesWeight());
                    }

                    obj.setPrintState("未打印");
                } else {
                    obj.setPrintState("已打印");
                    obj.setApproverUserName(approverInfoMap
                            .getOrDefault(obj.getApproverInfoId(), new ApproverInfo())
                            .getUserName());

                }
                return obj;
            }).toList();

            modelMap.put("separateSalesRecordList", separateSalesRecordList);
            modelMap.put("item", item);
        } else {
            modelMap.put("separateSalesRecordList", Lists.newArrayList());
            modelMap.put("item", new RegisterBill());
        }

        return "ecommerceBill/prePrintSeperatePrintReport";
    }

    /**
     * 查询eventlist
     * @param billIdList
     * @return
     */
    @RequestMapping("/queryEvents.action")
    @ResponseBody
    public List<String> queryEvents(@RequestBody List<Long> billIdList) {
        List<String>list=StreamEx.of(this.queryEventsByIdList(billIdList)).map(ECommerceBillMessageEvent::getCode).toList();
        return list;
    }
    /**
     * 查询eventlist
     * @param billIdList
     * @return
     */
    private  List<ECommerceBillMessageEvent> queryEventsByIdList(@RequestBody List<Long> billIdList) {
        List<ECommerceBillMessageEvent>list=new ArrayList<>();
        list.add(ECommerceBillMessageEvent.ADD);
        list.add(ECommerceBillMessageEvent.EXPORT);
        if(billIdList==null||billIdList.size()==0||billIdList.size()>1){
            return  list;
        }

        Long billId=billIdList.get(0);
        if(billId==null){
            return list;
        }

        RegisterBill billItem=this.billService.get(billId);
        if(billItem==null){
            return list;
        }
        list.add(ECommerceBillMessageEvent.PRINT);
        list.add(ECommerceBillMessageEvent.PRINT_SEPERATE);

        list.add(ECommerceBillMessageEvent.DETAIL);
        if(YesOrNoEnum.NO.getCode().equals(billItem.getIsDeleted())){
            list.add(ECommerceBillMessageEvent.DELETE);
        }

        if(BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(billItem.getVerifyStatus())){
            list.add(ECommerceBillMessageEvent.AUDIT);
        }
        return list;
    }

}
