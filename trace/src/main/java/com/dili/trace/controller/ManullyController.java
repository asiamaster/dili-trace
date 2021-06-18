package com.dili.trace.controller;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.DetectRecord;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.enums.TradeTypeEnum;
import com.dili.trace.service.*;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/manully")
public class ManullyController {
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    ProductStockService productStockService;
    @Autowired
    TradeDetailService tradeDetailService;

    @RequestMapping(value = "/detectFailed.action", method = RequestMethod.GET)
    @ResponseBody
    public BaseOutput detectFailed(String billCode, Integer detectResult) {
        if (StringUtils.isBlank(billCode) || detectResult == null) {
            return BaseOutput.failure("参数不能为空");
        }

        RegisterBill q = new RegisterBill();
        q.setCode(billCode.trim());
        q.setIsDeleted(YesOrNoEnum.NO.getCode());
        q.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        RegisterBill registerBill = StreamEx.of(this.registerBillService.listByExample(q)).findFirst().orElse(null);

        if (registerBill == null) {
            return BaseOutput.failure("报备单不存在");
        }
        if (registerBill.getDetectRequestId() == null) {
            return BaseOutput.failure("没有关联的检测数据");
        }
        Long recordId = registerBill.getLatestDetectRecordId();
        DetectRequest detectRequest = this.detectRequestService.get(registerBill.getDetectRequestId());
        if (detectRequest == null) {
            return BaseOutput.failure("检测数据不存在");
        }
        try {
            DetectResultEnum toDetectResult = DetectResultEnum.fromCodeOrEx(detectResult);
            DetectResultEnum detectResultEnum = DetectResultEnum.fromCodeOrEx(detectRequest.getDetectResult());
            if (DetectResultEnum.NONE == detectResultEnum) {
                return BaseOutput.failure("还没有进行检测,不做处理");
            }
            if (detectResultEnum == toDetectResult) {
                return BaseOutput.failure("检测结果没有变化,不做处理");
            }
            TradeDetail tdq = new TradeDetail();
            tdq.setBillId(registerBill.getBillId());
            tdq.setTradeType(TradeTypeEnum.NONE.getCode());
            TradeDetail td = StreamEx.of(this.tradeDetailService.listByExample(tdq)).findFirst().orElse(null);
            if (td == null) {
                return BaseOutput.failure("还没有入库,不做处理");
            }
            DetectRequest detectRequestUp = new DetectRequest();
            detectRequestUp.setId(detectRequest.getId());
            detectRequestUp.setDetectResult(toDetectResult.getCode());
            detectRequestUp.setDetectTime(new Date());

            if (DetectResultEnum.PASSED == detectResultEnum && DetectResultEnum.FAILED == toDetectResult) {
                //合格变为不合格
            } else if (DetectResultEnum.FAILED == detectResultEnum && DetectResultEnum.PASSED == toDetectResult) {
                //不合格变为合格
            } else {
                return BaseOutput.failure("无法进行检测结果变更,请检查数据及参数");
            }
            RegisterBill rb = new RegisterBill();
            rb.setId(registerBill.getBillId());
            rb.setLatestDetectTime(new Date());
            rb.setLatestPdResult(toDetectResult.getName());
            this.registerBillService.updateSelective(rb);

            if (recordId != null) {
                DetectRecord detectRecord = new DetectRecord();
                detectRecord.setId(recordId);
                detectRecord.setDetectState(toDetectResult.getCode());
                detectRecord.setDetectTime(new Date());
                this.detectRecordService.updateSelective(detectRecord);
            }
            this.detectRequestService.updateSelective(detectRequestUp);
            this.productStockService.updateProductStockAndTradeDetailAfterDetect(registerBill.getBillId(), Optional.empty());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }

        return BaseOutput.success();

    }
}
