package com.dili.trace.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 由MyBatis Generator工具自动生成
 * 
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`quality_trace_trade_bill`")
public interface QualityTraceTradeBill extends IBaseDomain {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "`id`")
	@FieldDef(label = "id")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getId();

	void setId(Long id);
	

    @ApiModelProperty(value = "编号")
    @Column(name = "`register_bill_code`")
    @FieldDef(label="编号")
    @EditMode(editor = FieldEditor.Text, required = true)
    String getRegisterBillCode();

    void setRegisterBillCode(String registerBillCode);
    
    @ApiModelProperty(value = "1.分销 2.全销")
    @Column(name = "`sales_type`")
    @FieldDef(label="1.分销 2.全销")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getSalesType();

    void setSalesType(Integer salesType);

	@ApiModelProperty(value = "流水号")
	@Column(name = "`bill_id`")
	@FieldDef(label = "流水号")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getBillId();

	void setBillId(Long billId);

	@ApiModelProperty(value = "交易单号")
	@Column(name = "`order_id`")
	@FieldDef(label = "订单号")
	@EditMode(editor = FieldEditor.Text, required = true)
	String getOrderId();

	void setOrderId(String orderId);

	@ApiModelProperty(value = "订单状态")
	@Column(name = "`order_status`")
	@FieldDef(label = "orderStatus")
	@EditMode(editor = FieldEditor.Number, required = true)
	Boolean getOrderStatus();

	void setOrderStatus(Boolean orderStatus);

	@ApiModelProperty(value = "卖家账号")
	@Column(name = "`seller_account`")
	@FieldDef(label = "卖家账号")
	@EditMode(editor = FieldEditor.Text, required = true)
	String getSellerAccount();

	void setSellerAccount(String sellerAccount);

	@ApiModelProperty(value = "卖家名称")
	@Column(name = "`seller_name`")
	@FieldDef(label = "卖家名称", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	String getSellerName();

	void setSellerName(String sellerName);

	@ApiModelProperty(value = "卖家身份证")
	@Column(name = "`sellerIDNo`")
	@FieldDef(label = "卖家身份证", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	public String getSellerIDNo();

	public void setSellerIDNo(String sellerIDNo);

	@ApiModelProperty(value = "买家账号")
	@Column(name = "`buyer_account`")
	@FieldDef(label = "买家账号")
	@EditMode(editor = FieldEditor.Text, required = true)
	String getBuyerAccount();

	void setBuyerAccount(String buyerAccount);

	@ApiModelProperty(value = "买家名称")
	@Column(name = "`buyer_name`")
	@FieldDef(label = "买家名称", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	String getBuyerName();

	void setBuyerName(String buyerName);

	@ApiModelProperty(value = "买家身份证")
	@Column(name = "`buyerIDNo`")
	@FieldDef(label = "买家身份证", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	public String getBuyerIDNo();

	public void setBuyerIDNo(String buyerIDNo);

	@ApiModelProperty(value = "订单创建时间")
	@Column(name = "`order_create_date`")
	@FieldDef(label = "订单创建时间")
	@EditMode(editor = FieldEditor.Datetime, required = true)
	Date getOrderCreateDate();

	void setOrderCreateDate(Date orderCreateDate);

	@ApiModelProperty(value = "订单支付时间")
	@Column(name = "`order_pay_date`")
	@FieldDef(label = "订单支付时间")
	@EditMode(editor = FieldEditor.Datetime, required = true)
	Date getOrderPayDate();

	void setOrderPayDate(Date orderPayDate);

	@ApiModelProperty(value = "残留值")
	@Column(name = "`pdResult`")
	@FieldDef(label = "残留值")
	@EditMode(editor = FieldEditor.Text, required = false)
	BigDecimal getPdresult();

	void setPdresult(BigDecimal pdresult);

	@ApiModelProperty(value = "合格值  0-未知 1合格  2不合格 3作废")
	@Column(name = "`conclusion`")
	@FieldDef(label = "合格值  0-未知 1合格  2不合格 3作废")
	@EditMode(editor = FieldEditor.Number, required = false)
	Boolean getConclusion();

	void setConclusion(Boolean conclusion);

	@ApiModelProperty(value = "检测结算单唯一标志,NULL表示无检测信息")
	@Column(name = "`check_result_EID`")
	@FieldDef(label = "检测结算单唯一标志,NULL表示无检测信息")
	@EditMode(editor = FieldEditor.Number, required = false)
	Long getCheckResultEid();

	void setCheckResultEid(Long checkResultEid);

	@ApiModelProperty(value = "交易号")
	@Column(name = "`trade_flow_id`")
	@FieldDef(label = "交易号")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getTradeFlowId();

	void setTradeFlowId(Long tradeFlowId);

	@ApiModelProperty(value = "总金额")
	@Column(name = "`total_money`")
	@FieldDef(label = "总金额", maxLength = 30)
	@EditMode(editor = FieldEditor.Text, required = true)
	Long getTotalMoney();

	void setTotalMoney(Long totalMoney);

	@ApiModelProperty(value = "订单项号")
	@Column(name = "`order_item_id`")
	@FieldDef(label = "订单项号")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getOrderItemId();

	void setOrderItemId(Long orderItemId);

	@ApiModelProperty(value = "商品名称")
	@Column(name = "`product_name`")
	@FieldDef(label = "商品名称", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	String getProductName();

	void setProductName(String productName);

	@ApiModelProperty(value = "品类名称")
	@Column(name = "`cate_name`")
	@FieldDef(label = "品类名称", maxLength = 50)
	@EditMode(editor = FieldEditor.Text, required = true)
	String getCateName();

	void setCateName(String cateName);

	@ApiModelProperty(value = "单价(分)")
	@Column(name = "`price`")
	@FieldDef(label = "单价(分)")
	@EditMode(editor = FieldEditor.Number, required = true)
	Long getPrice();

	void setPrice(Long price);

//
//	@Column(name = "`amount`")
//	@FieldDef(label = "件数")
//	@EditMode(editor = FieldEditor.Number, required = true)
//	Long getAmount();
//
//	void setAmount(Long amount);
    @ApiModelProperty(value = "件数")
	@Column(name = "`piece_quantity`")
	@FieldDef(label = "件数")
	@EditMode(editor = FieldEditor.Number, required = true)
	public Long getPieceQuantity();

	public void setPieceQuantity(Long pieceQuantity);

	@ApiModelProperty(value = "件重（公斤")
	@Column(name = "`piece_weight`")
	@FieldDef(label = "件重（公斤）")
	@EditMode(editor = FieldEditor.Number, required = true)
	public Long getPieceWeight();

	public void setPieceWeight(Long pieceWeight);

	@ApiModelProperty(value = "总净重（公斤")
	@Column(name = "`net_weight`")
	@FieldDef(label = "总净重（公斤）")
	@EditMode(editor = FieldEditor.Number, required = true)
	public Long getNetWeight();

	public void setNetWeight(Long netWeight);

	@ApiModelProperty(value = "交易类型编码")
	@Column(name = "`tradetype_id`")
	@FieldDef(label = "交易类型编码")
	@EditMode(editor = FieldEditor.Number, required = true)
	public String getTradetypeId();

	public void setTradetypeId(String tradetypeId);

	@ApiModelProperty(value = "交易类型名称")
	@Column(name = "`tradetype_name`")
	@FieldDef(label = "交易类型名称")
	@EditMode(editor = FieldEditor.Text, required = true)

	public String getTradetypeName();

	public void setTradetypeName(String tradetypeName);

	@ApiModelProperty(value = "状态 0：正常1：作废")
	@Column(name = "`bill_active`")
	@FieldDef(label = "状态 0：正常1：作废")
	@EditMode(editor = FieldEditor.Number, required = true)
	public Integer getBillActive();

	public void setBillActive(Integer billActive);

	@ApiModelProperty(value = "销售单位 1:斤 2：件")
	@Column(name = "`sale_unit`")
	@FieldDef(label = "销售单位 1:斤 2：件")
	@EditMode(editor = FieldEditor.Number, required = true)
	Integer getSaleUnit();

	void setSaleUnit(Integer saleUnit);
	
	
	@ApiModelProperty(value = "匹配状态")
	@Column(name = "`match_status`")
	@FieldDef(label = "匹配状态")
	@EditMode(editor = FieldEditor.Number, required = true)
	Integer getMatchStatus();

	void setMatchStatus(Integer matchStatus);
	
	
//	@ApiModelProperty(value = "交易单版本")
//	@Column(name = "`order_version`")
//	@FieldDef(label = "交易单版本")
//	@EditMode(editor = FieldEditor.Number, required = true)
//	Integer getOrderVersion();
//
//	void setOrderVersion(Integer orderVersion);
}