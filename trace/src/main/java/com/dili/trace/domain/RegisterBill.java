package com.dili.trace.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseDomain;
import com.dili.trace.api.output.ProductStockExtendDataDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.TFEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * <p>
 * This file was generated on 2019-07-26 09:20:34.
 */
@Table(name = "`register_bill`")
public class RegisterBill extends BaseDomain {
    /**
     * 报备单主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Long id;

    /**
     * 编号
     */
    @ApiModelProperty(value = "编号")
    @Column(name = "`code`")
    private String code;

    /**
     * 采样编号
     */
    @ApiModelProperty(value = "采样编号")
    @Column(name = "`sample_code`")
    private String sampleCode;

    /**
     * 业户姓名
     */
    @ApiModelProperty(value = "业户姓名")
    @Column(name = "`name`")
    private String name;

    /**
     * 身份证号
     */
    @ApiModelProperty(value = "身份证号")
    @Column(name = "`id_card_no`")
    private String idCardNo;

    /**
     * 身份地址
     */
    @ApiModelProperty(value = "身份地址")
    @Column(name = "`addr`")
    private String addr;

    /**
     * 业户手机号
     */
    @ApiModelProperty(value = "业户手机号")
    @Column(name = "`phone`")
    private String phone;

    /**
     * 用户iD
     */
    @ApiModelProperty(value = "用户iD")
    @Column(name = "`user_id`")
    private Long userId;
    /**
     * 摊位号
     */
//    @ApiModelProperty(value = "摊位号")
//    @Column(name = "`tally_area_no`")
//    private String tallyAreaNo;

    /**
     * 车牌
     */
    @ApiModelProperty(value = "车牌")
    @Column(name = "`plate`")
    private String plate;

    /**
     * 状态（1.待审核 2.待采样 3.已采样 4.待检测 5.检测中 6.已检测 7.复检中）
     */
    @ApiModelProperty(value = "1.待审核 2.待采样 3.已采样 4.待检测 5.检测中 6.已检测 7.复检中")
    @Column(name = "`state`")
    private Integer state;

    //@ApiModelProperty(value = "1:采样检测 2:主动送检")
//    @Column(name = "`sample_source`")
//    private Integer sampleSource;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    @Column(name = "`product_name`")
    private String productName;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID")
    @Column(name = "`product_id`")
    private Long productId;

    /**
     * 产地ID
     */
    @ApiModelProperty(value = "产地ID")
    @Column(name = "`origin_id`")
    private Long originId;

    /**
     * 产地名
     */
    @ApiModelProperty(value = "产地名")
    @Column(name = "`origin_name`")
    private String originName;

    /**
     * 重量KG
     */
    @ApiModelProperty(value = "重量KG")
    @Column(name = "`weight`")
    private BigDecimal weight;

    /**
     * 重量单位
     */
    @ApiModelProperty(value = "重量单位")
    @Column(name = "`weight_unit`")
    private Integer weightUnit;

    /**
     * 皮重
     */
//    @ApiModelProperty(value = "皮重")
//    @Column(name = "`tare_weight`")
//    private BigDecimal tareWeight;



     /* @ApiModelProperty(value = "1.合格 2.不合格 3.复检合格 4.复检不合格")
    @Column(name = "`detect_state`")
    private Integer detectState;*/

    /**
     * 检测记录ID
     */
    @ApiModelProperty(value = "检测记录ID")
    @Column(name = "`latest_detect_record_id`")
    private Long latestDetectRecordId;

    /**
     * 检测记录时间
     */
    @ApiModelProperty(value = "检测记录时间")
    @Column(name = "`latest_detect_time`")
    private Date latestDetectTime;

    /**
     * 检测人员
     */
    @ApiModelProperty(value = "检测人员")
    @Column(name = "`latest_detect_operator`")
    private String latestDetectOperator;

    /**
     * 检测值结果
     */
    @ApiModelProperty(value = "检测值结果")
    @Column(name = "`latest_pd_result`")
    private String latestPdResult;


    /**
     * 创建时间
     */
    @Column(name = "`created`")
    private Date created;

    /**
     * 修改时间
     */
    @Column(name = "`modified`")
    private Date modified;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    @Column(name = "`operator_name`")
    private String operatorName;

    /**
     * 操作人ID
     */
    @ApiModelProperty(value = "操作人ID")
    @Column(name = "`operator_id`")
    private Long operatorId;

    /**
     * 操作时间
     */
    @Column(name = "`operation_time`")
    private Date operationTime;

    /**
     * 上游信息ID
     */
    @ApiModelProperty(value = "上游信息ID")
    @Column(name = "`upstream_id`")
    private Long upStreamId;

    /**
     * 上游企业名
     */
    @Transient
    private String upStreamName;

    /**
     * 数据是否完整
     */
    @ApiModelProperty(value = "数据是否完整")
    @Column(name = "`complete`")
    private Integer complete;

    /**
     * /**查验状态值
     */
    @ApiModelProperty(value = "查验状态值")
    @Column(name = "`verify_status`")
    private Integer verifyStatus;

    /**
     * 保存类型
     */
    @ApiModelProperty(value = "保存类型")
    @Column(name = "`preserve_type`")
    private Integer preserveType;

    /**
     * 检测状态
     */
    @ApiModelProperty(value = "检测状态")
    @Column(name = "`detect_status`")
    private Integer detectStatus;


    /**
     * 查验类型
     */
    @ApiModelProperty(value = "查验类型")
    @Column(name = "`verify_type`")
    private Integer verifyType;

    /**
     * 规格名称
     */
    @ApiModelProperty(value = "规格名称")
    @Column(name = "`spec_name`")
    private String specName;

    /**
     * 登记单类型
     */
    @ApiModelProperty(value = "登记单类型")
    @Column(name = "`bill_type`")
    private Integer billType;


    /**
     * 报备方式
     */
    @ApiModelProperty(value = "报备方式")
    @Column(name = "`regist_type`")
    private Integer registType;
    /**
     * 拼车类型
     */
    @ApiModelProperty(value = "拼车类型")
    @Column(name = "`truck_type`")
    private Integer truckType;

    /**
     * 品牌名称
     */
    @ApiModelProperty(value = "品牌名称")
    @Column(name = "`brand_name`")
    private String brandName;

    /**
     * 品牌ID
     */
    @ApiModelProperty(value = "品牌ID")
    @Column(name = "`brand_id`")
    private Long brandId;

    /**
     * 原因
     */
    @ApiModelProperty(value = "原因")
    @Column(name = "`reason`")
    private String reason;

    /**
     * 是否被删除
     */
    @ApiModelProperty(value = "是否被删除")
    @Column(name = "`is_deleted`")
    private Integer isDeleted;

    /**
     * 市场ID
     */
    @ApiModelProperty(value = "市场ID")
    @Column(name = "market_id")
    private Long marketId;

    /**
     * 主台账编号
     */
    @ApiModelProperty(value = "主台账编号")
    @Column(name = "register_head_code")
    private String registerHeadCode;

    /**
     * 经营户卡号
     */
//    @ApiModelProperty(value = "经营户卡号")
//    @Column(name = "`third_party_code`")
//    private String cardNo;

    /**
     * 区号
     */
    @ApiModelProperty(value = "区号")
    @Column(name = "`area`")
    private String area;

    /**
     * 计量类型（10-计件 20-计重。默认计件。）
     */
    @ApiModelProperty(value = "计量类型。10-计件 20-计重。默认计件。")
    @Column(name = "`measure_type`")
    private Integer measureType;

    /**
     * 件数
     */
    @ApiModelProperty(value = "件数")
    @Column(name = "`piece_num`")
    private BigDecimal pieceNum;

    /**
     * 件重
     */
    @ApiModelProperty(value = "件重")
    @Column(name = "`piece_weight`")
    private BigDecimal pieceWeight;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @Column(name = "`remark`")
    private String remark;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    @Column(name = "`create_user`")
    private String createUser;

    /**
     * 作废人
     */
    @ApiModelProperty(value = "作废人")
    @Column(name = "`delete_user`")
    private String deleteUser;

    /**
     * 作废时间
     */
    @ApiModelProperty(value = "作废时间")
    @Column(name = "`delete_time`")
    private Date deleteTime;

    /**
     * 包装
     */
    @ApiModelProperty(value = "包装")
    @Column(name = "`packaging`")
    private String packaging;

    /**
     * 订单类型 1.报备单 2.进门登记单
     */
//    @ApiModelProperty(value = "订单类型 1.报备单 2.进门登记单")
//    @Column(name = "`order_type`")
//    private Integer orderType;

    /**
     * 销售类型1.分销 2.全销
     */
    @ApiModelProperty(value = "1.分销 2.全销")
    @Column(name = "`sales_type`")
    private Integer salesType;

    /**
     * 检测报告 0：无,1:有
     */
    @ApiModelProperty(value = "检测报告 0：无,1:有")
    @Column(name = "`has_detect_report`")
    private Integer hasDetectReport;

    /**
     * 产地证明 0：无,1:有
     * {@link com.dili.trace.glossary.TFEnum}
     */
    @ApiModelProperty(value = "产地证明 0：无,1:有")
    @Column(name = "`has_origin_certifiy`")
    private Integer hasOriginCertifiy;

    /**
     * 处理结果 0：无,1:有
     * {@link com.dili.trace.glossary.TFEnum}
     */
    @ApiModelProperty(value = "处理结果 0：无,1:有")
    @Column(name = "`has_handle_result`")
    private Integer hasHandleResult;

    @ApiModelProperty(value = "产地证明")
    @Column(name = "`origin_certifiy_url`")
    private String originCertifiyUrl;


    @ApiModelProperty(value = "检测报告")
    @Column(name = "`detect_report_url`")
    private String detectReportUrl;


    /**
     * 车辆皮重
     */
    @Column(name = "`truck_tare_weight`")
    private BigDecimal truckTareWeight;

    /**
     * 单价
     */
    @Column(name = "`unit_price`")
    private BigDecimal unitPrice;

    /**
     * 创建人角色。0-经营户 1-管理员
     */
    @ApiModelProperty(value = "创建人角色。0-经营户 1-管理员")
    @Column(name = "`creator_role`")
    private Integer creatorRole;


    /**
     * 是否打印
     */
    @Column(name = "`is_print_checksheet`")
    private Integer isPrintCheckSheet;

    /**
     * 是否进门
     */
    @Column(name = "`checkin_status`")
    private Integer checkinStatus;

    /**
     * 检测退回原因
     */
    @Column(name = "`return_reason`")
    private String returnReason;


    /**
     * 预计到场时间
     */
    @Column(name = "`arrival_datetime`")
    @JSONField(format = "yyyy-MM-dd HH:mm")
    private Date arrivalDatetime;


    /**
     * 到货摊位
     */
    @Transient
    private List<String> arrivalTallynos;

    public Date getArrivalDatetime() {
        return arrivalDatetime;
    }

    public void setArrivalDatetime(Date arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Integer getCheckinStatus() {
        return checkinStatus;
    }

    public void setCheckinStatus(Integer checkinStatus) {
        this.checkinStatus = checkinStatus;
    }

    public Integer getIsPrintCheckSheet() {
        return isPrintCheckSheet;
    }

    public void setIsPrintCheckSheet(Integer isPrintCheckSheet) {
        this.isPrintCheckSheet = isPrintCheckSheet;
    }


    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTruckTareWeight() {
        return truckTareWeight;
    }

    public void setTruckTareWeight(BigDecimal truckTareWeight) {
        this.truckTareWeight = truckTareWeight;
    }

    public Integer getDetectStatus() {
        return detectStatus;
    }

    @Transient
    public String getDetectStatusName() {
        return DetectStatusEnum.name(this.getDetectStatus());
    }

    public void setDetectStatus(Integer detectStatus) {
        this.detectStatus = detectStatus;
    }

    public Integer getHasDetectReport() {
        return hasDetectReport;
    }

    public void setHasDetectReport(Integer hasDetectReport) {
        this.hasDetectReport = hasDetectReport;
    }

    public Integer getHasOriginCertifiy() {
        return hasOriginCertifiy;
    }

    public void setHasOriginCertifiy(Integer hasOriginCertifiy) {
        this.hasOriginCertifiy = hasOriginCertifiy;
    }

    public Integer getHasHandleResult() {
        return hasHandleResult;
    }

    public void setHasHandleResult(Integer hasHandleResult) {
        this.hasHandleResult = hasHandleResult;
    }

    @Transient
    private List<ImageCert> imageCertList;

    @Transient
    private String tradeRequestCode;

    @Transient
    private List<TradePushLog> tradePushLogs;

    @Transient
    private ProductStockExtendDataDto productStockExtendDataDto;

    @Transient
    private BigDecimal headWeight;

    @Transient
    private BigDecimal remainWeight;

    @Transient
    private String orderTypeName;

    @Transient
    private String truckTypeName;

    @Transient
    private String isPrintCheckSheetName;

    public String getIsPrintCheckSheetName() {
        return this.getIsPrintCheckSheet() == null ? null : YesOrNoEnum.getYesOrNoEnum(this.getIsPrintCheckSheet()).getName();
    }

    public void setIsPrintCheckSheetName(String isPrintCheckSheetName) {
        this.isPrintCheckSheetName = isPrintCheckSheetName;
    }

    public Integer getSalesType() {
        return salesType;
    }


    public void setSalesType(Integer salesType) {
        this.salesType = salesType;
    }

    /**
     * 以下字段为市场合并字段
     */
    @ApiModelProperty(value = "1.理货区 2.交易区")
    @Column(name = "`register_source`")
    private Integer registerSource;


    @Column(name = "`source_id`")
    private String sourceId;

    @Column(name = "`source_name`")
    private String sourceName;


    @Column(name = "`trade_type_id`")
    private String tradeTypeId;

    @Column(name = "`trade_type_name`")
    private String tradeTypeName;

    /**
     *
     */
    @ApiModelProperty(value = "交易账号")
    @Column(name = "`trade_account`")
    private String tradeAccount;

    /**
     *
     */
    @ApiModelProperty(value = "园区卡号")
    @Column(name = "`card_no`")
    private String cardNo;


    /**
     *
     */
    @ApiModelProperty(value = "仪器编号")
    @Column(name = "`exe_machine_no`")
    String exeMachineNo;


    /**
     *
     */
    @ApiModelProperty(value = "处理结果备注")
    @Column(name = "`handle_result`")
    private String handleResult;

    /**
     *
     */
    @ApiModelProperty(value = "数据创建来源")
    @Column(name = "`creation_source`")
    Integer creationSource;

    /**
     *
     */
    @ApiModelProperty(value = "检测报告单ID")
    @Column(name = "`check_sheet_id`")
    private Long checkSheetId;

    /**
     *
     */
    @ApiModelProperty(value = "企业名称")
    @Column(name = "`corporate_name`")
    private String corporateName;

    /**
     * 商品别名
     */
    @ApiModelProperty(value = "商品别名")
    @Column(name = "`product_alias_name`")
    private String productAliasName;

    /**
     *
     */
    @ApiModelProperty(value = "检测请求ID")
    @Column(name = "`detect_request_id`")
    private Long detectRequestId;

    /**
     * 称重单ID
     */
    @Column(name = "`weighting_bill_id`")
    private Long weightingBillId;

    @Transient
    private DetectRequest detectRequest;

    public DetectRequest getDetectRequest() {
        return detectRequest;
    }

    public void setDetectRequest(DetectRequest detectRequest) {
        this.detectRequest = detectRequest;
    }

    public Long getDetectRequestId() {
        return detectRequestId;
    }

    public void setDetectRequestId(Long detectRequestId) {
        this.detectRequestId = detectRequestId;
    }

    public String getTruckTypeName() {
        return truckTypeName;
    }

    public void setTruckTypeName(String truckTypeName) {
        this.truckTypeName = truckTypeName;
    }

    public String getOrderTypeName() {
        return orderTypeName;
    }

    public void setOrderTypeName(String orderTypeName) {
        this.orderTypeName = orderTypeName;
    }

    public List<TradePushLog> getTradePushLogs() {
        return tradePushLogs;
    }

    public void setTradePushLogs(List<TradePushLog> tradePushLogs) {
        this.tradePushLogs = tradePushLogs;
    }

    public String getTradeRequestCode() {
        return tradeRequestCode;
    }

    public void setTradeRequestCode(String tradeRequestCode) {
        this.tradeRequestCode = tradeRequestCode;
    }

    public List<ImageCert> getImageCertList() {
        return imageCertList;
    }

    public void setImageCertList(List<ImageCert> imageCertList) {
        this.imageCertList = imageCertList;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    @Transient
    public String getFormattedWeight() {
        if (this.getWeight() == null) {
            return "0";
        }
        return this.getWeight().stripTrailingZeros().toPlainString();
        // return this.getWeight().
        // return BillVerifyStatusEnum.fromCode(this.getVerifyStatus()).map(BillVerifyStatusEnum::getName).orElse("");
    }

    @Transient
    public String getVerifyStatusName() {
        return BillVerifyStatusEnum.name(this.getVerifyStatus());
    }

    @Transient
    public String getRegistTypeName() {
        return RegistTypeEnum.name(this.getRegistType());
    }

    @Transient
    public String getWeightUnitName() {
        return WeightUnitEnum.toName(this.getWeightUnit());
    }

    @Transient
    public String getBillTypeName() {
        return BillTypeEnum.fromCode(this.getBillType()).map(BillTypeEnum::getName).orElse("");
    }

    @Transient
    public String getHasOriginCertifiyName() {
        return this.getHasOriginCertifiy() == null || this.getHasOriginCertifiy() == 0 ? "无" : "有";
    }

    @Transient
    public String getHasDetectReportName() {
        return this.getHasDetectReport() == null || this.getHasDetectReport() == 0 ? "无" : "有";
    }

    @Transient
    public String getHasHandleResultName() {
        return this.getHasHandleResult() == null || this.getHasHandleResult() == 0 ? "无" : "有";
    }

    @Transient
    public Long getBillId() {
        return this.getId();
    }

//    @Transient
//    public String getDetectStateName() {
//        try {
//            if (getDetectState() == null) {
//                return "";
//            }
//        } catch (Exception e) {
//            return "";
//        }
//        BillDetectStateEnum state = BillDetectStateEnum.getBillDetectStateEnum(getDetectState());
//        return state.getName();
//
//    }

//    @Transient
//    public String getStateName() {
//        try {
//            if (getState() == null) {
//                return "";
//            }
//        } catch (Exception e) {
//            return "";
//        }
//        return RegisterBillStateEnum.getRegisterBillStateEnum(getState()).getName();
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSampleCode() {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode) {
        this.sampleCode = sampleCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

//    public Integer getState() {
//        return state;
//    }
//
//    public void setState(Integer state) {
//        this.state = state;
//    }

//    public Integer getSampleSource() {
//        return sampleSource;
//    }
//
//    public void setSampleSource(Integer sampleSource) {
//        this.sampleSource = sampleSource;
//    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(Integer weightUnit) {
        this.weightUnit = weightUnit;
    }

//    public Integer getDetectState() {
//        return detectState;
//    }
//
//    public void setDetectState(Integer detectState) {
//        this.detectState = detectState;
//    }

    public Long getLatestDetectRecordId() {
        return latestDetectRecordId;
    }

    public void setLatestDetectRecordId(Long latestDetectRecordId) {
        this.latestDetectRecordId = latestDetectRecordId;
    }

    public Date getLatestDetectTime() {
        return latestDetectTime;
    }

    public void setLatestDetectTime(Date latestDetectTime) {
        this.latestDetectTime = latestDetectTime;
    }

    public String getLatestDetectOperator() {
        return latestDetectOperator;
    }

    public void setLatestDetectOperator(String latestDetectOperator) {
        this.latestDetectOperator = latestDetectOperator;
    }

    public String getLatestPdResult() {
        return latestPdResult;
    }

    public void setLatestPdResult(String latestPdResult) {
        this.latestPdResult = latestPdResult;
    }



    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getUpStreamId() {
        return upStreamId;
    }

    public void setUpStreamId(Long upStreamId) {
        this.upStreamId = upStreamId;
    }

    public Integer getComplete() {
        return complete;
    }

    public void setComplete(Integer complete) {
        this.complete = complete;
    }

    /**
     * @return Integer return the preserveType
     */
    public Integer getPreserveType() {
        return preserveType;
    }

    /**
     * @param preserveType the preserveType to set
     */
    public void setPreserveType(Integer preserveType) {
        this.preserveType = preserveType;
    }

    /**
     * @return Integer return the verifyType
     */
    public Integer getVerifyType() {
        return verifyType;
    }

    /**
     * @param verifyType the verifyType to set
     */
    public void setVerifyType(Integer verifyType) {
        this.verifyType = verifyType;
    }

    /**
     * @return String return the specName
     */
    public String getSpecName() {
        return specName;
    }

    /**
     * @param specName the specName to set
     */
    public void setSpecName(String specName) {
        this.specName = specName;
    }

    /**
     * @return Integer return the billType
     */
    public Integer getBillType() {
        return billType;
    }

    /**
     * @param billType the billType to set
     */
    public void setBillType(Integer billType) {
        this.billType = billType;
    }

    /**
     * @return Long return the brandId
     */
    public Long getBrandId() {
        return brandId;
    }

    /**
     * @param brandId the brandId to set
     */
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    /**
     * @return String return the brandName
     */
    public String getBrandName() {
        return brandName;
    }

    /**
     * @param brandName the brandName to set
     */
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    /**
     * @return Integer return the truckType
     */
    public Integer getTruckType() {
        return truckType;
    }

    /**
     * @param truckType the truckType to set
     */
    public void setTruckType(Integer truckType) {
        this.truckType = truckType;
    }

    /**
     * @return String return the tallyAreaNo
     */
    @Transient
    public String getTallyAreaNo() {
        if (RegisterSourceEnum.TALLY_AREA.equalsToCode(this.registerSource)) {
            return this.getSourceName();
        }
        return "";
    }
//
//    /**
//     * @param tallyAreaNo the tallyAreaNo to set
//     */
//    public void setTallyAreaNo(String tallyAreaNo) {
//        this.tallyAreaNo = tallyAreaNo;
//    }

    /**
     * @return String return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }


    /**
     * @return String return the isDeleted
     */
    @Transient
    public String getIsDeletedName() {
        return YesOrNoEnum.YES.getCode().equals(this.getIsDeleted()) ? YesOrNoEnum.YES.getName() : YesOrNoEnum.NO.getName();
    }


    /**
     * @return String return the isDeleted
     */
    @Transient
    public String getTruckTypeShow() {
        return TruckTypeEnum.FULL.getCode().equals(this.getTruckType()) ? TruckTypeEnum.FULL.getName() : TruckTypeEnum.POOL.getName();
    }

    /**
     * @return Integer return the isDeleted
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * @param isDeleted the isDeleted to set
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }


    /**
     * @return Date return the operationTime
     */
    public Date getOperationTime() {
        return operationTime;
    }

    /**
     * @param operationTime the operationTime to set
     */
    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getUpStreamName() {
        return upStreamName;
    }

    public void setUpStreamName(String upStreamName) {
        this.upStreamName = upStreamName;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public String getRegisterHeadCode() {
        return registerHeadCode;
    }

    public void setRegisterHeadCode(String registerHeadCode) {
        this.registerHeadCode = registerHeadCode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getMeasureType() {
        return measureType;
    }

    public void setMeasureType(Integer measureType) {
        this.measureType = measureType;
    }

    public BigDecimal getPieceNum() {
        return pieceNum;
    }

    public void setPieceNum(BigDecimal pieceNum) {
        this.pieceNum = pieceNum;
    }

    public BigDecimal getPieceWeight() {
        return pieceWeight;
    }

    public void setPieceWeight(BigDecimal pieceWeight) {
        this.pieceWeight = pieceWeight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

//    public Integer getOrderType() {
//        return orderType;
//    }
//
//    public void setOrderType(Integer orderType) {
//        this.orderType = orderType;
//    }

    public BigDecimal getHeadWeight() {
        return headWeight;
    }

    public void setHeadWeight(BigDecimal headWeight) {
        this.headWeight = headWeight;
    }

    public BigDecimal getRemainWeight() {
        return remainWeight;
    }

    public void setRemainWeight(BigDecimal remainWeight) {
        this.remainWeight = remainWeight;
    }

    public Integer getRegisterSource() {
        return registerSource;
    }

    @Transient
    public String getRegisterSourceName() {
        return RegisterSourceEnum.getRegisterSourceEnum(this.registerSource).map(RegisterSourceEnum::getName).orElse("");
    }

    @Transient
    public String getSourceDesc() {
        return this.getRegisterSourceName() + (StringUtils.isBlank(this.sourceName) ? "" : ("：" + this.sourceName));
    }

    public void setRegisterSource(Integer registerSource) {
        this.registerSource = registerSource;
    }

    public String getTradeAccount() {
        return tradeAccount;
    }

    public void setTradeAccount(String tradeAccount) {
        this.tradeAccount = tradeAccount;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

//    public String getTradeTypeId() {
//        return tradeTypeId;
//    }
//
//    public void setTradeTypeId(String tradeTypeId) {
//        this.tradeTypeId = tradeTypeId;
//    }
//
//    public String getTradeTypeName() {
//        return tradeTypeName;
//    }
//
//    public void setTradeTypeName(String tradeTypeName) {
//        this.tradeTypeName = tradeTypeName;
//    }


    public String getExeMachineNo() {
        return exeMachineNo;
    }

    public void setExeMachineNo(String exeMachineNo) {
        this.exeMachineNo = exeMachineNo;
    }


    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public Integer getCreationSource() {
        return creationSource;
    }

    public void setCreationSource(Integer creationSource) {
        this.creationSource = creationSource;
    }

    public Long getCheckSheetId() {
        return checkSheetId;
    }

    public void setCheckSheetId(Long checkSheetId) {
        this.checkSheetId = checkSheetId;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public String getProductAliasName() {
        return productAliasName;
    }

    public void setProductAliasName(String productAliasName) {
        this.productAliasName = productAliasName;
    }

    public String getOriginCertifiyUrl() {
        return originCertifiyUrl;
    }

    public void setOriginCertifiyUrl(String originCertifiyUrl) {
        this.originCertifiyUrl = originCertifiyUrl;
    }

    public String getDetectReportUrl() {
        return detectReportUrl;
    }

    public void setDetectReportUrl(String detectReportUrl) {
        this.detectReportUrl = detectReportUrl;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Integer getRegistType() {
        return registType;
    }

    public void setRegistType(Integer registType) {
        this.registType = registType;
    }

    public Integer getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(Integer creatorRole) {
        this.creatorRole = creatorRole;
    }

//    public BigDecimal getTareWeight() {
//        return tareWeight;
//    }
//
//    public void setTareWeight(BigDecimal tareWeight) {
//        this.tareWeight = tareWeight;
//    }
    @Transient
    public String getCheckInStatusName(){
        return CheckinStatusEnum.ALLOWED.equalsToCode(this.getCheckinStatus())?"是":"否";
    }

    public List<String> getArrivalTallynos() {
        return arrivalTallynos;
    }

    public void setArrivalTallynos(List<String> arrivalTallynos) {
        this.arrivalTallynos = arrivalTallynos;
    }

    public Long getWeightingBillId() {
        return weightingBillId;
    }

    public void setWeightingBillId(Long weightingBillId) {
        this.weightingBillId = weightingBillId;
    }

    public ProductStockExtendDataDto getProductStockExtendDataDto() {
        return productStockExtendDataDto;
    }

    public void setProductStockExtendDataDto(ProductStockExtendDataDto productStockExtendDataDto) {
        this.productStockExtendDataDto = productStockExtendDataDto;
    }
}