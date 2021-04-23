package com.dili.trace.domain;

import com.dili.ss.domain.BaseDomain;
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
 * <p>
 * This file was generated on 2019-07-26 09:20:35.
 */
@Table(name = "`quality_trace_trade_bill`")
public class QualityTraceTradeBill extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    @ApiModelProperty(value = "编号")
    @Column(name = "`register_bill_code`")
    private String registerBillCode;

    @ApiModelProperty(value = "1.分销 2.全销")
    @Column(name = "`sales_type`")
    private Integer salesType;


    @ApiModelProperty(value = "流水号")
    @Column(name = "`bill_id`")
    private Long billId;


    @ApiModelProperty(value = "交易单号")
    @Column(name = "`order_id`")
    private String orderId;


    @ApiModelProperty(value = "订单状态")
    @Column(name = "`order_status`")
    private Boolean orderStatus;


    @ApiModelProperty(value = "卖家账号")
    @Column(name = "`seller_account`")
    private String sellerAccount;


    @ApiModelProperty(value = "卖家名称")
    @Column(name = "`seller_name`")
    private String sellerName;


    @ApiModelProperty(value = "卖家身份证")
    @Column(name = "`sellerIDNo`")
    private String sellerIDNo;

    @ApiModelProperty(value = "买家账号")
    @Column(name = "`buyer_account`")
    private String buyerAccount;


    @ApiModelProperty(value = "买家名称")
    @Column(name = "`buyer_name`")
    private String buyerName;


    @ApiModelProperty(value = "买家身份证")
    @Column(name = "`buyerIDNo`")
    private String buyerIDNo;


    @ApiModelProperty(value = "订单创建时间")
    @Column(name = "`order_create_date`")
    private Date orderCreateDate;


    @ApiModelProperty(value = "订单支付时间")
    @Column(name = "`order_pay_date`")
    private Date orderPayDate;

    @ApiModelProperty(value = "残留值")
    @Column(name = "`pdResult`")
    private BigDecimal pdresult;


    @ApiModelProperty(value = "合格值  0-未知 1合格  2不合格 3作废")
    @Column(name = "`conclusion`")
    private Boolean conclusion;


    @ApiModelProperty(value = "检测结算单唯一标志,NULL表示无检测信息")
    @Column(name = "`check_result_EID`")
    private Long checkResultEid;

    @ApiModelProperty(value = "交易号")
    @Column(name = "`trade_flow_id`")
    private Long tradeFlowId;


    @ApiModelProperty(value = "总金额")
    @Column(name = "`total_money`")
    private Long totalMoney;


    @ApiModelProperty(value = "订单项号")
    @Column(name = "`order_item_id`")
    private Long orderItemId;


    @ApiModelProperty(value = "商品名称")
    @Column(name = "`product_name`")
    private String productName;


    @ApiModelProperty(value = "品类名称")
    @Column(name = "`cate_name`")
    private String cateName;

    @ApiModelProperty(value = "单价(分)")
    @Column(name = "`price`")
    private Long price;

    @ApiModelProperty(value = "件数")
    @Column(name = "`piece_quantity`")
    private Long pieceQuantity;

    @ApiModelProperty(value = "件重（公斤")
    @Column(name = "`piece_weight`")
    private Long pieceWeight;


    @ApiModelProperty(value = "总净重（公斤")
    @Column(name = "`net_weight`")
    private Long netWeight;


    @ApiModelProperty(value = "交易类型编码")
    @Column(name = "`tradetype_id`")
    private String tradetypeId;

    @ApiModelProperty(value = "交易类型名称")
    @Column(name = "`tradetype_name`")

    private String tradetypeName;


    @ApiModelProperty(value = "状态 0：正常1：作废")
    @Column(name = "`bill_active`")
    private Integer billActive;


    @ApiModelProperty(value = "销售单位 1:斤 2：件")
    @Column(name = "`sale_unit`")
    private Integer saleUnit;


    @ApiModelProperty(value = "匹配状态")
    @Column(name = "`match_status`")
    private Integer matchStatus;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getRegisterBillCode() {
        return registerBillCode;
    }

    public void setRegisterBillCode(String registerBillCode) {
        this.registerBillCode = registerBillCode;
    }

    public Integer getSalesType() {
        return salesType;
    }

    public void setSalesType(Integer salesType) {
        this.salesType = salesType;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Boolean getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Boolean orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getSellerAccount() {
        return sellerAccount;
    }

    public void setSellerAccount(String sellerAccount) {
        this.sellerAccount = sellerAccount;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerIDNo() {
        return sellerIDNo;
    }

    public void setSellerIDNo(String sellerIDNo) {
        this.sellerIDNo = sellerIDNo;
    }

    public String getBuyerAccount() {
        return buyerAccount;
    }

    public void setBuyerAccount(String buyerAccount) {
        this.buyerAccount = buyerAccount;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerIDNo() {
        return buyerIDNo;
    }

    public void setBuyerIDNo(String buyerIDNo) {
        this.buyerIDNo = buyerIDNo;
    }

    public Date getOrderCreateDate() {
        return orderCreateDate;
    }

    public void setOrderCreateDate(Date orderCreateDate) {
        this.orderCreateDate = orderCreateDate;
    }

    public Date getOrderPayDate() {
        return orderPayDate;
    }

    public void setOrderPayDate(Date orderPayDate) {
        this.orderPayDate = orderPayDate;
    }

    public BigDecimal getPdresult() {
        return pdresult;
    }

    public void setPdresult(BigDecimal pdresult) {
        this.pdresult = pdresult;
    }

    public Boolean getConclusion() {
        return conclusion;
    }

    public void setConclusion(Boolean conclusion) {
        this.conclusion = conclusion;
    }

    public Long getCheckResultEid() {
        return checkResultEid;
    }

    public void setCheckResultEid(Long checkResultEid) {
        this.checkResultEid = checkResultEid;
    }

    public Long getTradeFlowId() {
        return tradeFlowId;
    }

    public void setTradeFlowId(Long tradeFlowId) {
        this.tradeFlowId = tradeFlowId;
    }

    public Long getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(Long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getPieceQuantity() {
        return pieceQuantity;
    }

    public void setPieceQuantity(Long pieceQuantity) {
        this.pieceQuantity = pieceQuantity;
    }

    public Long getPieceWeight() {
        return pieceWeight;
    }

    public void setPieceWeight(Long pieceWeight) {
        this.pieceWeight = pieceWeight;
    }

    public Long getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Long netWeight) {
        this.netWeight = netWeight;
    }

    public String getTradetypeId() {
        return tradetypeId;
    }

    public void setTradetypeId(String tradetypeId) {
        this.tradetypeId = tradetypeId;
    }

    public String getTradetypeName() {
        return tradetypeName;
    }

    public void setTradetypeName(String tradetypeName) {
        this.tradetypeName = tradetypeName;
    }

    public Integer getBillActive() {
        return billActive;
    }

    public void setBillActive(Integer billActive) {
        this.billActive = billActive;
    }

    public Integer getSaleUnit() {
        return saleUnit;
    }

    public void setSaleUnit(Integer saleUnit) {
        this.saleUnit = saleUnit;
    }

    public Integer getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(Integer matchStatus) {
        this.matchStatus = matchStatus;
    }
}