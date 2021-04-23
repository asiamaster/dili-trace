package com.dili.trace.glossary;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author yuehongbo
 * @createTime 2018/11/8 18:43
 */
public enum SampleSourceEnum {
    /**
     * 初始
     */
    NONE(-1, "初始"),
    /**
     * 待处理
     */
    WAIT_HANDLE(0, "待处理"),
    /**
     * 采样检测
     */
    SAMPLE_CHECK(1, "采样检测"),
    /**
     * 主动送检
     */
    AUTO_CHECK(2, "主动送检"),
    /**
     * 抽检
     */
    SPOT_CHECK(3, "抽检"),

    /**
     * 人工
     */
    MANUALLY(4, "人工"),
    /**
     * 其他
     */
    OTHERS(9999, "其他"),
    ;

    private String name;
    private Integer code ;

    SampleSourceEnum(Integer code, String name){
        this.code = code;
        this.name = name;
    }


    public static Optional<SampleSourceEnum> fromCode(Integer code) {
        for (SampleSourceEnum anEnum : SampleSourceEnum.values()) {
            if (anEnum.getCode().equals(code)) {
                return Optional.ofNullable(anEnum);
            }
        }
        return Optional.empty();
    }
    public static String name(Integer code){
        return SampleSourceEnum.fromCode(code).map(SampleSourceEnum::getName).orElse("");
    }
    @JsonValue
    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}