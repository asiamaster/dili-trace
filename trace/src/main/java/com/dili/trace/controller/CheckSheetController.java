package com.dili.trace.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.*;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.service.*;
import com.dili.trace.util.MarketUtil;
import com.dili.uap.sdk.session.SessionContext;
import one.util.streamex.StreamEx;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.trace.dto.CheckSheetInputDto;
import com.dili.trace.dto.CheckSheetPrintOutput;
import com.dili.trace.dto.CheckSheetQueryDto;
import com.dili.trace.enums.BillTypeEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:35.
 */
@Api("/checkSheet")
@Controller
@RequestMapping("/checkSheet")
public class CheckSheetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckSheetController.class);

    @Autowired
    CheckSheetService checkSheetService;
    @Autowired
    BillService billService;
    @Autowired
    ApproverInfoService approverInfoService;
    @Autowired
    CheckSheetDetailService checkSheetDetailService;
    @Autowired
    DetectTaskService detectTaskService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    UapRpcService uapRpcService;

    /**
     * 跳转到CheckSheet页面
     *
     * @param modelMap
     * @param billType
     * @return
     */
    @ApiOperation("跳转到CheckSheet页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap,
                        @RequestParam(required = false, name = "billType", defaultValue = "1") String billType) {

        LocalDateTime now = LocalDateTime.now();
        modelMap.put("createdStart", now.withYear(2019).withMonth(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")));
        modelMap.put("createdEnd", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));

        try{
            BillTypeEnum billTypeEnum= BillTypeEnum.fromCode(Integer.parseInt(billType)).orElse(BillTypeEnum.REGISTER_BILL);
            modelMap.put("billType", billTypeEnum.getCode());
        }catch (Exception e){
            modelMap.put("billType", BillTypeEnum.REGISTER_BILL.getCode());
        }

        if(billType.equalsIgnoreCase(BillTypeEnum.REGISTER_BILL.getCode().toString())){
            modelMap.put("gridTitle", "进场检测报告");
        }else{
            modelMap.put("gridTitle", "委托检测报告");
        }

        return "checkSheet/index";
    }

    /**
     * 编辑页面
     *
     * @param modelMap
     * @param idList
     * @return
     */
    @RequestMapping(value = "/edit.html", method = RequestMethod.GET)
    public String edit(ModelMap modelMap, @RequestParam("idList") List<Long> idList) {

        modelMap.put("itemList", Collections.emptyList());
        modelMap.put("userNameList", Collections.emptyList());
        modelMap.put("detectOperatorNameList", Collections.emptyList());
        List<ApproverInfo> approverInfoList = this.approverInfoService
                .listByExample(new ApproverInfo());
        modelMap.put("approverInfoList", approverInfoList);

        if (idList != null && !idList.isEmpty()) {

            List<RegisterBill> itemList = StreamEx.of(this.billService.findByIdList(idList)).nonNull()
                    .filter(item -> item.getCheckSheetId() == null)
                    .filter(bill -> {
                        DetectRequest detectRequest = this.detectRequestService.get(bill.getDetectRequestId());
                        if (null == detectRequest) {
                            return false;
                        }
                        return DetectResultEnum.PASSED.equalsToCode(detectRequest.getDetectResult());

                    })
                    .collect(Collectors.toList());
            List<String> detectOperatorNameList = StreamEx.of(itemList).nonNull()
                    .map(RegisterBill::getLatestDetectOperator).filter(StringUtils::isNotBlank).distinct()
                    .collect(Collectors.toList());

            List<String> userNameList = itemList.stream().map(item -> {
                if (StringUtils.isNotBlank(item.getCorporateName())) {
                    return item.getCorporateName();
                } else {
                    return item.getName();
                }

            }).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

            modelMap.put("itemList", itemList);
            modelMap.put("userNameList", userNameList);
            modelMap.put("detectOperatorNameList", detectOperatorNameList);
        }

        return "checkSheet/edit";
    }

    /**
     * 分页查询CheckSheet
     *
     * @param checkSheet
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "分页查询CheckSheet", notes = "分页查询CheckSheet，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CheckSheet", paramType = "form", value = "CheckSheet的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody CheckSheetQueryDto checkSheet) throws Exception {

        if (checkSheet.getBillType() == null) {
            checkSheet.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        }


        List<String> sqlList = new ArrayList<String>();
        if (StringUtils.isNotBlank(checkSheet.getLikeApproverUserName())) {
            sqlList.add("  (approver_info_id in (select id from approver_info where user_name='"
                    + checkSheet.getLikeApproverUserName().trim() + "')) ");
        }
        Long marketId = MarketUtil.returnMarket();
        Integer deleteCode = YesOrNoEnum.NO.getCode();

        sqlList.add("bill_type="
                + checkSheet.getBillType()
                + " and id in(select check_sheet_id from check_sheet_detail where register_bill_id in (select id from register_bill where bill_type="
                + checkSheet.getBillType()
                + " and market_id = "
                + marketId
                + " and is_deleted = "
                + deleteCode
                + ") )");

        checkSheet.setMetadata(IDTO.AND_CONDITION_EXPR, String.join(" AND ", sqlList));
        EasyuiPageOutput out = this.checkSheetService.listEasyuiPageByExample(checkSheet, true);
        return out.toString();
    }

    /**
     * 新增CheckSheet
     *
     * @param input
     * @return
     */
    @ApiOperation("新增CheckSheet")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CheckSheet", paramType = "form", value = "CheckSheet的form信息", required = true, dataType = "string")})
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput<CheckSheetPrintOutput> insert(@RequestBody CheckSheetInputDto input) {

        try {
            CheckSheetPrintOutput resultMapDto = this.checkSheetService.createCheckSheet(input, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success("新增成功").setData(resultMapDto);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

    /**
     * 预览CheckSheet
     *
     * @param input
     * @return
     */
    @ApiOperation("预览CheckSheet")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CheckSheet", paramType = "form", value = "CheckSheet的form信息", required = true, dataType = "string")})
    @RequestMapping(value = "/prePrint.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput<Object> prePrint(@RequestBody CheckSheetInputDto input) {

        try {
            CheckSheetPrintOutput resultMapDto = this.checkSheetService.prePrint(input, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success().setData(resultMapDto);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }
    }

    /**
     * 预览CheckSheet
     *
     * @param input
     * @return
     */
    @ApiOperation("预览CheckSheet")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CheckSheet", paramType = "form", value = "CheckSheet的form信息", required = true, dataType = "string")})
    @RequestMapping(value = "/findPrintableCheckSheet.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput<Object> findPrintableCheckSheet(@RequestBody CheckSheetInputDto input) {
        try {
            CheckSheetPrintOutput resultMapDto = this.checkSheetService.findPrintableCheckSheet(input.getId());
            return BaseOutput.success().setData(resultMapDto);
        } catch (TraceBizException e) {
            LOGGER.error("checksheet", e);
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return BaseOutput.failure();
        }
    }

    /**
     * 跳转到CheckSheet页面
     *
     * @param modelMap
     * @param id
     * @return
     */
    @ApiOperation("跳转到CheckSheet页面")
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @RequestParam(name = "id", required = true) Long id) {
        modelMap.put("item", null);
        modelMap.put("checkSheetDetailList", Collections.emptyList());
        modelMap.put("billIdBillMap", Collections.emptyMap());
        if (id != null) {
            CheckSheet checkSheet = this.checkSheetService.get(id);
            modelMap.put("item", checkSheet);
            if (checkSheet != null) {
                ApproverInfo approverInfo = this.approverInfoService.get(checkSheet.getApproverInfoId());
                modelMap.put("approverInfo", approverInfo);

                List<CheckSheetDetail> checkSheetDetailList = this.checkSheetDetailService
                        .findCheckSheetDetailByCheckSheetId(checkSheet.getId()).stream().map(detail -> {
                            if ( DetectResultEnum.PASSED.equalsToCode(detail.getDetectResult())) {
                                detail.setDetectStateView("合格");
                            } else {
                                detail.setDetectStateView("未知");
                            }
                            return detail;

                        }).collect(Collectors.toList());
                modelMap.put("checkSheetDetailList", checkSheetDetailList);

                List<Long> registerBillIdList = checkSheetDetailList.stream().map(CheckSheetDetail::getRegisterBillId)
                        .filter(Objects::nonNull).collect(Collectors.toList());
                if (!registerBillIdList.isEmpty()) {
                    Map<Long, RegisterBill> billIdBillMap = this.billService.findByIdList(registerBillIdList).stream()
                            .collect(Collectors.toMap(item -> item.getId(), Function.identity()));

                    modelMap.put("billIdBillMap", billIdBillMap);
                }

            }
        }

        return "checkSheet/view";
    }

    /**
     * 跳转到CheckSheet二维码请求页面
     *
     * @param modelMap
     * @param checkSheetCode
     * @return
     */
    @ApiOperation("跳转到CheckSheet二维码请求页面")
    @RequestMapping(value = "/detail.html", method = RequestMethod.GET)
    public String detail(ModelMap modelMap, String checkSheetCode) {
        modelMap.put("item", new CheckSheetPrintOutput());
        this.checkSheetService.findCheckSheetByCode(checkSheetCode).ifPresent(checkSheet -> {
            CheckSheetPrintOutput resultMap = this.checkSheetService.findPrintableCheckSheet(checkSheet.getId());
            modelMap.put("item", resultMap);
        });

        return "checkSheet/detail";
    }
}