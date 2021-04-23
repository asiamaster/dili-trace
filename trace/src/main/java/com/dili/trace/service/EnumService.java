package com.dili.trace.service;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.dili.trace.enums.ImageCertTypeEnum;
import com.dili.trace.enums.MeasureTypeEnum;
import com.dili.trace.enums.TruckTypeEnum;
import com.google.common.collect.Lists;
import de.cronn.reflection.util.PropertyUtils;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 枚举service
 */
@Service
public class EnumService {
    @Autowired
    FieldConfigDetailService fieldConfigDetailService;

    /**
     * 查询图片凭证类型
     *
     * @param marketId
     * @param moduleTypeEnum
     * @return
     */
    public List<ImageCertTypeEnum> listImageCertType(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum) {
        String propName = PropertyUtils.getPropertyDescriptor(RegisterBill.class, RegisterBill::getImageCertList).getName();
        Set<String> valueSet = this.selectAvailableValueList(marketId, moduleTypeEnum, propName);
        return StreamEx.of(ImageCertTypeEnum.values()).filter(e -> {
            return valueSet.contains(String.valueOf(e.getCode()));
        }).nonNull().toList();
    }

    /**
     * 查询接车
     *
     * @param marketId
     * @param moduleTypeEnum
     * @return
     */
    public List<TruckTypeEnum> listTruckType(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum) {
        String propName = PropertyUtils.getPropertyDescriptor(RegisterBill.class, RegisterBill::getTruckType).getName();
        Set<String> valueSet = this.selectAvailableValueList(marketId, moduleTypeEnum, propName);

        return StreamEx.of(TruckTypeEnum.values()).filter(e -> {
            return valueSet.contains(String.valueOf(e.getCode()));
        }).nonNull().toList();
    }

    /**
     * 查询计重方式
     *
     * @param marketId
     * @param moduleTypeEnum
     * @return
     */
    public List<MeasureTypeEnum> listMeasureType(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum) {
        String propName = PropertyUtils.getPropertyDescriptor(RegisterBill.class, RegisterBill::getMeasureType).getName();
        Set<String> valueSet = this.selectAvailableValueList(marketId, moduleTypeEnum, propName);
        return StreamEx.of(MeasureTypeEnum.values()).filter(e -> {
            return valueSet.contains(String.valueOf(e.getCode()));
        }).nonNull().toList();
    }

    /**
     * 查询 可用值
     * @param marketId
     * @param moduleTypeEnum
     * @param propName
     * @return
     */
    private Set<String> selectAvailableValueList(Long marketId, FieldConfigModuleTypeEnum moduleTypeEnum, String propName) {
        Set<String> valueSet = StreamEx.of(this.fieldConfigDetailService.findByMarketIdAndModuleType(marketId, moduleTypeEnum))
                .filter(dto -> {
                    return dto.getDefaultFieldDetail().getFieldName().equalsIgnoreCase(propName);
                }).filter(dto -> {
                    return YesOrNoEnum.YES.getCode().equals(dto.getDisplayed());
                }).flatCollection(dto -> {
                    return dto.getAvailableValueList();
                }).nonNull().map(String::valueOf).map(StringUtils::trimToNull).nonNull().toSet();

        return valueSet;
    }
}
