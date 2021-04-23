package com.dili.trace.controller;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillStaticsDto;
import com.dili.trace.enums.DetectTypeEnum;
import com.dili.trace.glossary.BillDetectStateEnum;
import com.dili.trace.glossary.SampleSourceEnum;
import com.dili.trace.service.*;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 寿光报备单数据统计报表
 */
@Controller
@RequestMapping("/registerBillDataReport")
public class RegisterBillDataReportController {
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    BillService billService;

    @Autowired
    RegisterBillDataReportService registerBillDataReportService;
    @Autowired
    DetectRequestService detectRequestService;
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
        return "registerBillDataReport/statics";
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

        return this.registerBillDataReportService.listStaticsPage(registerBill);
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
        registerBill.setAttrValue(StringUtils.trimToEmpty(registerBill.getAttrValue()));
        RegisterBillStaticsDto staticsDto = this.registerBillDataReportService.groupByState(registerBill);
        return BaseOutput.success().setData(staticsDto);
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

}
