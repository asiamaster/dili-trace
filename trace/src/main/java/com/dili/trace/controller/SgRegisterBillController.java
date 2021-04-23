package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.SeparateSalesRecord;
import com.dili.trace.domain.UsualAddress;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.dili.trace.rpc.service.CityRpcService;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
}