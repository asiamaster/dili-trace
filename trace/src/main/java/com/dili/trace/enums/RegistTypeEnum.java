package com.dili.trace.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import one.util.streamex.StreamEx;

import java.util.Optional;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 */
public enum RegistTypeEnum {

	/**
	 * 正常进场
	 */
	NONE(10, "正常进场"),
	/**
	 * 补单
	 */
	SUPPLEMENT(20, "补单"),
	/**
	 * 外冷分批进场
	 */
	PARTIAL(30, "分批进场"),
	;

	private String name;
	private Integer code;

	RegistTypeEnum(Integer code, String name) {
		this.code = code;
		this.name = name;
	}

	public static Optional<RegistTypeEnum> fromCode(Integer code) {
		return StreamEx.of(RegistTypeEnum.values()).filterBy(RegistTypeEnum::getCode, code).findFirst();
	}

	public static String name(Integer code){
		return RegistTypeEnum.fromCode(code).map(RegistTypeEnum::getName).orElse("");
	}
	public boolean equalsToCode(Integer code) {
		return this.getCode().equals(code);
	}

	public static boolean canDoVerify(Integer code) {
		return BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(code) || BillVerifyStatusEnum.RETURNED.equalsToCode(code);
	}

	@JsonValue
	public Integer getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
