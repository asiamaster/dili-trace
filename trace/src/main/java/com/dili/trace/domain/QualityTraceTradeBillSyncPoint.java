package com.dili.trace.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

/**
 * 由MyBatis Generator工具自动生成
 * 
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`quality_trace_trade_bill_syncpoint`")
public interface QualityTraceTradeBillSyncPoint extends IBaseDomain {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "`id`")
	@FieldDef(label = "id")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getId();

	void setId(Long id);

	@Column(name = "`bill_id`")
	@FieldDef(label = "流水号")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getBillId();

	void setBillId(Long billId);
	@Column(name = "`order_id`")
	@FieldDef(label = "订单号")
	@EditMode(editor = FieldEditor.Text, required = true)
	String getOrderId();

	void setOrderId(String orderId);
	
//	@ApiModelProperty(value = "交易单版本")
//	@Column(name = "`order_version`")
//	@FieldDef(label = "交易单版本")
//	@EditMode(editor = FieldEditor.Number, required = true)
//	Integer getOrderVersion();
//
//	void setOrderVersion(Integer orderVersion);

}