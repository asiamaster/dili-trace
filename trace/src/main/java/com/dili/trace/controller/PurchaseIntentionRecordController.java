package com.dili.trace.controller;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.dto.IndividualCustomerInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.*;
import com.dili.trace.dto.query.PurchaseIntentionRecordQueryDto;
import com.dili.trace.service.MarketService;
import com.dili.trace.service.PurchaseIntentionRecordService;
import com.dili.trace.service.UapRpcService;
import com.dili.trace.util.MarketUtil;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.domain.dto.FirmDto;
import com.dili.uap.sdk.rpc.FirmRpc;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 买家意向单信息
 */
@Controller
@RequestMapping("/purchaseIntentionRecord")
public class PurchaseIntentionRecordController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseIntentionRecordController.class);

    @Autowired
    PurchaseIntentionRecordService purchaseIntentionRecordService;
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    CustomerRpc customerRpc;
    @Autowired
    MarketService marketService;
    @Autowired
    FirmRpc firmRpc;


    /**
     * 跳转到PurchaseIntentionRecord页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到PurchaseIntentionRecord页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap, HttpServletRequest req) {

        return "purchaseIntentionRecord/index";
    }

    /**
     * 分页查询PurchaseIntentionRecord
     *
     * @param queryInput
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询PurchaseIntentionRecord", notes = "分页查询PurchaseIntentionRecord，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "PurchaseIntentionRecord", paramType = "form", value = "PurchaseIntentionRecord的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody PurchaseIntentionRecordQueryDto queryInput) throws Exception {
        queryInput.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        queryInput.setState(EnabledStateEnum.ENABLED.getCode());
        return this.purchaseIntentionRecordService.listEasyuiPageByExample(queryInput, true).toString();

    }

    /**
     * 买家报备查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id) {
        PurchaseIntentionRecord purchaseIntentionRecord = purchaseIntentionRecordService.get(id);
        modelMap.put("purchaseIntentionRecord", purchaseIntentionRecord);
        return "purchaseIntentionRecord/view";
    }

    /**
     * 买家报备新增页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap) {
        User userItem = DTOUtils.newDTO(User.class);
        modelMap.put("userItem", userItem);
        return "purchaseIntentionRecord/add";
    }

    /**
     * 买家报备编辑查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/edit.html", method = RequestMethod.GET)
    public String edit(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id) {
        PurchaseIntentionRecord purchaseIntentionRecord = purchaseIntentionRecordService.get(id);
        modelMap.put("purchaseIntentionRecord", purchaseIntentionRecord);
        return "purchaseIntentionRecord/edit";
    }

    /**
     * 买家报备查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add_buyer.html", method = RequestMethod.GET)
    public String addBuyer(ModelMap modelMap) throws Exception {
        FirmDto firmDto = DTOUtils.newDTO(FirmDto.class);
        firmDto.setDeleted(false);
        firmDto.setFirmState(EnabledStateEnum.ENABLED.getCode());
        BaseOutput<List<Firm>> baseOutput = firmRpc.listByExample(firmDto);
        if (null != baseOutput) {
            List<Firm> firmList = baseOutput.getData();
            modelMap.put("firmList", firmList);
        }
        return "purchaseIntentionRecord/add_buyer";
    }

    /**
     * 新增买家报备
     *
     * @param customer
     * @throws Exception
     */
    @RequestMapping(value = "/doAddBuyer.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput doAddBuyer(@RequestBody IndividualCustomerInput customer,HttpServletRequest request) {
        try {
            BaseOutput<CustomerExtendDto> output = customerRpc.registerIndividual(customer,request.getHeader("UAP_SessionId"));
            return output;
        } catch (TraceBizException e) {
            logger.error(e.getMessage());
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 新增买家报备
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/doDelete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput doDelete(Long id) {
        try {
            PurchaseIntentionRecord record = new PurchaseIntentionRecord();
            record.setId(id);
            record.setState(EnabledStateEnum.DISABLED.getCode());
            purchaseIntentionRecordService.updateSelective(record);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            logger.error(e.getMessage());
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return BaseOutput.failure("服务端出错");
        }
    }


    /**
     * 新增买家报备
     *
     * @param record
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/doAdd.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput doAdd(@RequestBody PurchaseIntentionRecord record) {
        try {
            record.setMarketId(MarketUtil.returnMarket());
            purchaseIntentionRecordService.doAddPurchaseIntentionRecord(record, this.uapRpcService.getCurrentOperator().get());
            logger.info(JSON.toJSONString(record));
            return BaseOutput.success();
        } catch (TraceBizException e) {
            logger.error(e.getMessage());
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 更新买家报备内容
     *
     * @param record
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/doUpdate.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput doUpdate(@RequestBody PurchaseIntentionRecord record) {
        try {
            record.setModified(DateUtils.getCurrentDate());
            purchaseIntentionRecordService.updateSelective(record);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            logger.error(e.getMessage());
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return BaseOutput.failure("服务端出错");
        }
    }

}
