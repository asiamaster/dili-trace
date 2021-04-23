package com.dili.trace.enums;

import one.util.streamex.StreamEx;

import java.util.Optional;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 */
public enum FieldConfigModuleTypeEnum {

    /**
     * 报备单(台账)
     */
    REGISTER(1, "报备单(台账)"),

    /**
     * 检测单
     */
    DETECT_REQUEST(2, "检测单"),
    ;
    private String name;
    private Integer code;

    FieldConfigModuleTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<FieldConfigModuleTypeEnum> fromCode(Integer code) {
        return StreamEx.of(FieldConfigModuleTypeEnum.values()).filterBy(FieldConfigModuleTypeEnum::getCode, code).findFirst();
    }

    public boolean equalsToCode(Integer code) {
        return this.getCode().equals(code);
    }


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
