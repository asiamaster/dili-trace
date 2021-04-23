package com.dili.trace.enums;

import java.util.Optional;

import one.util.streamex.StreamEx;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 */
public enum TruckTypeEnum {

	/**
	 * 整车
	 */
	FULL(10, "整车"),
	/**
	 * 拼车
	 */
	POOL(20, "拼车"),

	;

	private String name;
	private Integer code;

	TruckTypeEnum(Integer code, String name) {
		this.code = code;
		this.name = name;
	}

	public static Optional<TruckTypeEnum> fromCode(Integer code) {
		return StreamEx.of(TruckTypeEnum.values()).filterBy(TruckTypeEnum::getCode, code).findFirst();
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

	//获取字符串类型编码  用于页面判断
	public String getStringCode() {
		return String.valueOf(this.code);
	}
}
