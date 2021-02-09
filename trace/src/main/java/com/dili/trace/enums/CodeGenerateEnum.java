package com.dili.trace.enums;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @author wangguofeng
 * @createTime 2020/06/03 15:43
 */
public enum CodeGenerateEnum {
	/**
	 * 登记单采样编号
	 */
	//REGISTER_BILL_CODE("REGISTER_BILL_CODE", "登记单编号", "yyyyMMdd", "d", 5),
	/**
	 * 登记单采样编号
	 */
	REGISTER_BILL_SAMPLECODE("REGISTER_BILL_SAMPLECODE", "登记单采样编号", "yyyyMMdd", "c", 5),
	/**
	 * 检测报告编号
	 */
	REGISTER_BILL_CHECKSHEET_CODE("REGISTER_BILL_CHECKSHEET_CODE", "检测报告编号", "yyyyMMdd", "SGJC", 6),
	
	/**
	 * 检测报告编号
	 */
	COMMISSION_BILL_CHECKSHEET_CODE("COMMISSION_BILL_CHECKSHEET_CODE", "检测报告编号", "yyyyMMdd", "SGJCW", 6),
	/**
	 * 委托单编号
	 */
	//COMMISSION_BILL_CODE("COMMISSION_BILL_CODE", "检测报告编号", "yyyyMMdd", "dw", 5),
	/**
	 * 委托单采样编号
	 */
	COMMISSION_BILL_SAMPLECODE("COMMISSION_BILL_SAMPLE_CODE", "检测报告编号", "yyyyMMdd", "cw", 5),

	/**
	 * 电商单编号
	 */
	ECOMMERCE_BILL_CODE("ECOMMISSION_BILL_CODE", "商务单编号", "yyyyMMdd", "SGDSD", 5),

	/**
	 * 电商单采样编号
	 */
	ECOMMERCE_BILL_SAMPLECODE("ECOMMISSION_BILL_SAMPLE_CODE", "商务单采样编号", "yyyyMMdd", "SGDSC", 5),

	/**
	 * 电商单分销打印编号
	 */
	ECOMMERCE_BILL_SEPERATE_REPORT_CODE("ECOMMERCE_BILL_SEPERATE_REPORT_CODE", "电商单分销打印编号", "yyyyMMdd", "SGDSR", 5),
	;

	private String name;
	private String type;
	private String pattern;
	private String prefix;
	private Integer len;

	CodeGenerateEnum(String type, String name, String pattern, String prefix, int len) {
		this.type = type;
		this.name = name;
		this.pattern = pattern;
		this.prefix = prefix;
		this.len = len;
	}

	public Integer getLen() {
		return len;
	}

	public String getType() {
		return type;
	}

	public String getPattern() {
		return pattern;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getName() {
		return name;
	}
}
