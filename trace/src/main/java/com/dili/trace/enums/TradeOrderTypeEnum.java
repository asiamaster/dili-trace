package com.dili.trace.enums;

import java.util.Optional;

import one.util.streamex.StreamEx;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 */
public enum TradeOrderTypeEnum {
	/**
	 * 自有
	 */
//	NONE(0, "自有"),
	/**
	 * 购买
	 */
	BUY(10, "购买"),
	/**
	 * 销售
	 */
	SELL(20, "销售"),
	/**
	 * 分销
	 */
	SEPREATE(30, "分销"),
	;

	private String name;
	private Integer code;

	TradeOrderTypeEnum(Integer code, String name) {
		this.code = code;
		this.name = name;
	}

	public static Optional<TradeOrderTypeEnum> fromCode(Integer code) {
		return StreamEx.of(TradeOrderTypeEnum.values()).filterBy(TradeOrderTypeEnum::getCode, code).findFirst();
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
