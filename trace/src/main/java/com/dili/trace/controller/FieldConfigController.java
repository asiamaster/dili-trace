package com.dili.trace.controller;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.*;
import com.dili.trace.dto.*;
import com.dili.trace.dto.input.FieldConfigInputDto;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.*;
import com.dili.trace.events.RegisterBillMessageEvent;
import com.dili.trace.glossary.*;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.EntryStream;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Api("/fieldConfig")
@Controller
@RequestMapping("/fieldConfig")
public class FieldConfigController {
    private static final Logger logger = LoggerFactory.getLogger(FieldConfigController.class);

    @Autowired
    FieldConfigDetailService fieldConfigDetailService;
    @Autowired
    FieldConfigService fieldConfigService;
    @Autowired
    DefaultFieldDetailService defaultFieldDetailService;
    @Autowired
    UapRpcService uapRpcService;


    /**
     * 跳转到报备配置页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/bill.html", method = RequestMethod.GET)
    public String bill(ModelMap modelMap) {
        FieldConfigModuleTypeEnum moduleType=FieldConfigModuleTypeEnum.REGISTER;
        Firm currentFirm = this.uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        modelMap.put("currentFirm", currentFirm);
        Map<String, Long> defaultFieldNameIdMap = StreamEx.of(this.defaultFieldDetailService.findByModuleType(moduleType)).toMap(DefaultFieldDetail::getFieldName, DefaultFieldDetail::getId);
        modelMap.put("defaultFieldNameIdMap", defaultFieldNameIdMap);
        Map<String,Integer>defaultFiledNameIndexMap= StreamEx.of(defaultFieldNameIdMap.keySet()).zipWith(IntStream.range(0, defaultFieldNameIdMap.size())).toMap();
        modelMap.put("defaultFiledNameIndexMap", defaultFiledNameIndexMap);


        Map<String,FieldConfigDetailRetDto>filedNameRetMap=   StreamEx.of(  this.fieldConfigDetailService.findByMarketIdAndModuleType(currentFirm.getId(),moduleType))
                .toMap(item->item.getDefaultFieldDetail().getFieldName(),Function.identity());
        modelMap.put("filedNameRetMap", filedNameRetMap);
        modelMap.put("imageCertTypeList", StreamEx.of(ImageCertTypeEnum.values()).filter(e->ImageCertTypeEnum.Handle_Result!=e).toList());

        modelMap.put("truckTypeEnumList", TruckTypeEnum.values());
        modelMap.put("measureTypeEnumList", MeasureTypeEnum.values());

        return "fieldConfig/bill";
    }
    /**
     * 跳转到检测配置页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/detectrequest.html", method = RequestMethod.GET)
    public String detectrequest(ModelMap modelMap) {

        Firm currentFirm = this.uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        modelMap.put("currentFirm", currentFirm);

        FieldConfigModuleTypeEnum moduleType=FieldConfigModuleTypeEnum.DETECT_REQUEST;
        Map<String, Long> defaultFieldNameIdMap = StreamEx.of(this.defaultFieldDetailService.findByModuleType(moduleType)).toMap(DefaultFieldDetail::getFieldName, DefaultFieldDetail::getId);
        modelMap.put("defaultFieldNameIdMap", defaultFieldNameIdMap);
        Map<String,Integer>defaultFiledNameIndexMap= StreamEx.of(defaultFieldNameIdMap.keySet()).zipWith(IntStream.range(0, defaultFieldNameIdMap.size())).toMap();
        modelMap.put("defaultFiledNameIndexMap", defaultFiledNameIndexMap);


        Map<String,FieldConfigDetailRetDto>filedNameRetMap=   StreamEx.of(  this.fieldConfigDetailService.findByMarketIdAndModuleType(currentFirm.getId(),moduleType))
                .toMap(item->item.getDefaultFieldDetail().getFieldName(),Function.identity());
        modelMap.put("filedNameRetMap", filedNameRetMap);

        modelMap.put("imageCertTypeList", ImageCertTypeEnum.values());

        modelMap.put("yesornoEnumList", YesOrNoEnum.values());

        return "fieldConfig/detectrequest";
    }

    /**
     * 更新
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doUpdate.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput doUpdate(@RequestBody FieldConfigInputDto input) {
        try {
            this.fieldConfigDetailService.doUpdate(input);
            return BaseOutput.success("操作成功");
        }catch (TraceBizException e){
            return BaseOutput.failure(e.getMessage());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return BaseOutput.failure("保存出错");
        }

    }

}