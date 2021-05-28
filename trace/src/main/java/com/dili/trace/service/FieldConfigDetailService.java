package com.dili.trace.service;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.DefaultFieldDetail;
import com.dili.trace.domain.FieldConfig;
import com.dili.trace.domain.FieldConfigDetail;
import com.dili.trace.dto.input.FieldConfigInputDto;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.dili.trace.enums.MeasureTypeEnum;
import com.dili.trace.enums.TruckTypeEnum;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import one.util.streamex.StreamEx;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 字段配置详情
 */
@Service
public class FieldConfigDetailService extends TraceBaseService<FieldConfigDetail, Long> {
    @Autowired
    DefaultFieldDetailService defaultFieldDetailService;

    @Autowired
    FieldConfigService fieldConfigService;

    /**
     * 更新(保存)
     *
     * @param input
     * @return
     */
    public int doUpdate(FieldConfigInputDto input) {
        if (input == null || input.getMarketId() == null || input.getModuleType() == null || input.getFieldConfigDetailList() == null) {
            throw new TraceBizException("参数错误");
        }
        FieldConfig fc = this.fieldConfigService.saveOrFind(input.getMarketId(), input.getModuleType());
        FieldConfigDetail fcd = new FieldConfigDetail();
        fcd.setFieldConfigId(fc.getId());
        this.deleteByExample(fcd);


        List<FieldConfigDetail> fieldConfigDetailList = StreamEx.of(input.getFieldConfigDetailList()).nonNull().map(fcdInput -> {
            fcdInput.setId(null);
            fcdInput.setFieldConfigId(fc.getId());
            fcdInput.setIsValid(YesOrNoEnum.YES.getCode());
            fcdInput.setCreated(LocalDateTime.now());
            if(fcdInput.getDisplayed()==null){
                fcdInput.setDisplayed(YesOrNoEnum.NO.getCode());
            }
            this.insertSelective(fcdInput);
            return fcdInput;
        }).toList();
        if (fieldConfigDetailList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }

        return fieldConfigDetailList.size();

    }

    /**
     * 转换为json
     *
     * @param object
     * @return
     */
    private String toJsonString(Object object) {

        return JSON.toJSONString(object);
    }

    /**
     * 检测可用值是否正确
     *
     * @param value
     * @param fieldLabel
     * @param availableValues
     */
    private void checkValueAvailable(Object value, String fieldLabel, List<String> availableValues) {
        if (value != null && !availableValues.isEmpty()) {
            if (!availableValues.contains(String.valueOf(value))) {
                throw new TraceBizException("请填写正确的" + fieldLabel + "的值");
            }
        }
    }

    /**
     * 检测可显示以及必填
     *
     * @param value
     * @param fieldLabel
     * @param isDisplayed
     * @param isRequired
     * @return
     */
    private boolean checkAndIsSetDefaultValue(Object value, String fieldLabel, boolean isDisplayed, boolean isRequired) {
        if (isDisplayed && isRequired && value == null) {
            throw new TraceBizException("请填写" + fieldLabel);
        } else if (isRequired && value == null) {
            return true;
        }
        return false;
    }



    /**
     * 保存或更新字段配置信息
     *
     * @param fieldConfigInput
     * @param fieldConfigDetailListInput
     * @return
     */
    public int saveOrUpdateFieldConfig(FieldConfig fieldConfigInput, List<FieldConfigDetail> fieldConfigDetailListInput) {
        if (fieldConfigInput == null || fieldConfigInput.getModuleType() == null || fieldConfigInput.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }

        FieldConfig fieldConfigItem = this.fieldConfigService.saveOrFind(fieldConfigInput.getMarketId(), fieldConfigInput.getModuleType());

        List<FieldConfigDetail> fieldConfigDetailList = StreamEx.ofNullable(fieldConfigDetailListInput).flatCollection(Function.identity())
                .nonNull().map(fc -> {
                    fc.setId(null);
                    fc.setIsValid(YesOrNoEnum.YES.getCode());
                    fc.setCreated(LocalDateTime.now());
                    fc.setModified(LocalDateTime.now());
                    fc.setFieldConfigId(fieldConfigItem.getId());
                    return fc;
                }).toList();
        if (fieldConfigDetailList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }

        //标注原来的isvalue为false
        FieldConfigDetail domain = new FieldConfigDetail();
        domain.setIsValid(YesOrNoEnum.NO.getCode());

        FieldConfigDetail condition = new FieldConfigDetail();
        condition.setFieldConfigId(fieldConfigItem.getId());
        this.updateSelectiveByExample(domain, condition);

        //插入新数据
        StreamEx.of(fieldConfigDetailList).forEach(fc -> {
            this.insertSelective(fc);
        });
        //更新config的时间
        FieldConfig updatableFieldConfig = new FieldConfig();
        updatableFieldConfig.setId(fieldConfigItem.getId());
        updatableFieldConfig.setModified(LocalDateTime.now());
        return this.fieldConfigService.updateSelective(updatableFieldConfig);
    }


    /**
     * 查询字段配置详情
     *
     * @param marketId
     * @param moduleTypeEnum
     * @return
     */
    public List<FieldConfigDetailRetDto> findByMarketIdAndModuleType(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum) {
        Map<Long, FieldConfigDetail> fieldConfigDetailMap = StreamEx.of(this.fieldConfigService.findByMarketIdAndModuleType(marketId, moduleTypeEnum)).map(fc -> {
            FieldConfigDetail q = new FieldConfigDetail();
            q.setFieldConfigId(fc.getId());
            q.setIsValid(YesOrNoEnum.YES.getCode());
            return this.listByExample(q);
        }).flatCollection(Function.identity()).distinct(FieldConfigDetail::getDefaultId).mapToEntry(FieldConfigDetail::getDefaultId, Function.identity()).toMap();


        return StreamEx.of(this.defaultFieldDetailService.findByModuleType(moduleTypeEnum)).map(df -> {

            FieldConfigDetail fd = fieldConfigDetailMap.get(df.getId());

            FieldConfigDetailRetDto ret = new FieldConfigDetailRetDto();
            if (fd != null) {
                try {
                    BeanUtils.copyProperties(ret, fd);
                } catch (Exception e) {
                }
            }else{
                ret.setDisplayed(YesOrNoEnum.YES.getCode());
                ret.setRequired(YesOrNoEnum.YES.getCode());
                ret.setDefaultId(df.getId());
                ret.setIsValid(YesOrNoEnum.YES.getCode());
            }
//            if(df.getFieldName().equals("truckType")&&ret.getAvailableValueList().isEmpty()){
//                String values=JSON.toJSONString(StreamEx.of(TruckTypeEnum.values()).map(TruckTypeEnum::getCode).map(String::valueOf).toList());
//                ret.setAvailableValues(values);
//            }
//            if(df.getFieldName().equals("measureType")&&ret.getAvailableValueList().isEmpty()){
//                String values=JSON.toJSONString(StreamEx.of(MeasureTypeEnum.values()).map(MeasureTypeEnum::getCode).map(String::valueOf).toList());
//                ret.setAvailableValues(values);
//            }
            ret.setDefaultFieldDetail(df);
            return ret;
        }).toList();

    }
}
