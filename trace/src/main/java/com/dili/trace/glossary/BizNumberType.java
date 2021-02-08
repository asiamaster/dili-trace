package com.dili.trace.glossary;

/**
 * 业务编号类型及生成规则配置
 *
 * Created by asiam on 2018/5/21.
 */
public enum BizNumberType {
    //数据库表
//    CREATE TABLE `biz_number` (
//            `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
//	`type` VARCHAR(50) NOT NULL COMMENT '业务类型',
//            `value` BIGINT(20) NULL DEFAULT NULL COMMENT '编号值',
//            `memo` VARCHAR(50) NULL DEFAULT NULL COMMENT '备注',
//            `version` VARCHAR(20) NULL DEFAULT NULL COMMENT '版本号',
//            `modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
//            `created` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
//    PRIMARY KEY (`id`)
//)
//    COMMENT='业务号\r\n记录所有业务的编号\r\n如：\r\n回访编号:HF201712080001'
//    COLLATE='utf8mb4_general_ci'
//    ENGINE=InnoDB
//            AUTO_INCREMENT=9
//            ;

    //线上订单编号需要区别下测试和开发环境
    REGISTER_BILL("register_bill","登记单编号", "d", "yyyyMMdd", 5, "1"),
	REGISTER_BILL_SAMPLE_CODE("register_bill_sample_code","登记单采样编号", "c", "yyyyMMdd", 5, "1"),
    REGISTER_HEAD("register_head","进门主台账单编号", "h", "yyyyMMdd", 5, "1"),
    DETECT_REQUEST("detect_request","检测编号", "Bd", "yyyyMMdd", 5, "1"),
    ECOMMERCE_BILL("ecommerce_bill","电商单编号", "SGDSD", "yyyyMMdd", 5, "1"),
    REGISTER_BILL_CHECKSHEET_CODE("register_bill_checksheet_code", "检测报告编号", "SGJC", "yyyyMMdd", 5, "1"),
    COMMISSION_BILL_CHECKSHEET_CODE("commission_bill_checksheet_code", "检测报告编号","SGJCW", "yyyyMMdd",6, "1"),
    TRADE_REQUEST_CODE("trade_request_code", "交易单编号","HZSY", "yyyyMMdd",5, "1"),
    ECOMMERCE_BILL_SEPERATE_REPORT_CODE("ecommerce_bill_seperate_report_code", "电商单分销打印编号", "SGDSR", "yyyyMMdd", 5, "1"),
    COMMISSION_BILL_CODE("commission_bill_code", "委托单编号", "dw","yyyyMMdd",  5, "1"),
    STOCK_CODE("stock_code", "库存编号", "st","yyyyMMdd",  5, "1"),
    TRUCK_ENTER_RECORD_CODE("truck_enter_record_code", "司机报备编号", "dr","yyyyMMdd",  5, "1"),
    PURCHASE_INTENTION_RECORD_CODE("purchase_intention_record_code", "买家报备编号", "pr","yyyyMMdd",  5, "1"),

    ;
    //业务号类型,对应biz_number表中的type
    private String type;
    //中文名称描述
    private String name;
    //前缀
    private String prefix;
    //日期格式
    private String dateFormat;
    //自增位数
    private int length;
    //自增步长范围,默认(null时)为1, 示例"5,20"，即5到20位随机步长
    private String range;

    /**
     * @param type  编码业务类型
     * @param name  名称
     * @param prefix    前缀
     * @param dateFormat    日期格式
     * @param length    编号长度
     */
    BizNumberType(String type, String name, String prefix, String dateFormat, int length, String range){
        this.type = type;
        this.name = name;
        this.prefix = prefix;
        this.dateFormat = dateFormat;
        this.length = length;
        this.range = range;
    }

    public static BizNumberType getBizNumberByType(String type) {
        for (BizNumberType bizNumberType : BizNumberType.values()) {
            if (bizNumberType.getType().equals(type)) {
                return bizNumberType;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getLength() {
        return length;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
}
