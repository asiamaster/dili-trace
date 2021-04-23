package com.dili.trace.enums;

import one.util.streamex.StreamEx;

import java.util.Optional;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 */
public enum QrHistoryEventTypeEnum {

	/**
	 * 无
	 */
	NONE(0, "无"),

	/**
	 * 新用户
	 */
	NEW_USER(1, "新用户"),

	/**
	 * 最近七天无数据
	 */
	NO_DATA(2, "最近七天无数据"),

	/**
	 * 报备单
	 */
	REGISTER_BILL(3, "报备单"),

	/**
	 * 交易请求
	 */
	TRADE_REQUEST(4, "交易请求"),

	;

	private String name;
	private Integer code;

	QrHistoryEventTypeEnum(Integer code, String name) {
		this.code = code;
		this.name = name;
	}

	public static Optional<QrHistoryEventTypeEnum> fromCode(Integer code) {
		return StreamEx.of(QrHistoryEventTypeEnum.values()).filterBy(QrHistoryEventTypeEnum::getCode, code).findFirst();
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
