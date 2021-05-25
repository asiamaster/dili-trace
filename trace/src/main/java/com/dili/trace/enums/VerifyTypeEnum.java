package com.dili.trace.enums;

import java.util.stream.Stream;

public enum VerifyTypeEnum {
    NONE(0, "无"),
    /**
     * 预审通过
     */
    VERIFY_BEFORE_CHECKIN(10, "预审通过"),
    /**
     * 场内通过
     */
    CHECKIN_WITHOUT_VERIFY(20, "场内通过"),;

    private Integer code;
    private String desc;

    VerifyTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static VerifyTypeEnum fromCode(Integer code) {
        return Stream.of(VerifyTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
    }
    public boolean equalsToCode(Integer code) {
        return this.getCode().equals(code);

    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
