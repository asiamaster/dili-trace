

DROP TABLE IF EXISTS category;
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上一级ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT 'categoryID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `full_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL  COMMENT '全名',
  `level` int(11) NOT NULL DEFAULT '1' COMMENT '层级',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `market_id` varchar(20) DEFAULT NULL,
  `code` varchar(20) DEFAULT NULL COMMENT '第三方编码',
  `is_show` varchar(1) DEFAULT '1' COMMENT '登记显示,1显示,2不显示',
  `type` tinyint(1) DEFAULT '1' COMMENT '商品类型,1杭水，2检测商品',
  `specification` varchar(20) DEFAULT NULL COMMENT '商品规格名',
  `parent_code` varchar(20) DEFAULT NULL COMMENT '父级第三方编码',
  `uap_id` bigint(20) DEFAULT NULL COMMENT '调用uap获取商品id',
  `uap_parent_id` bigint(20) DEFAULT NULL COMMENT '调用uap获取parentid(处理后)',
  `old_uap_parent_id` bigint(20) DEFAULT NULL COMMENT '调用uap获取商品parentid',
  `last_sync_time` DATETIME NULL COMMENT '最后的同步时间',
  `last_sync_success` INT NULL COMMENT '最后是否同步成功(1:成功,0:失败)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

ALTER TABLE dili_trace.category ADD CONSTRAINT category_market_unique UNIQUE KEY (category_id,market_id);

ALTER TABLE dili_trace.`user` ADD qr_content varchar(500) NULL COMMENT '最后的二维码变更内容';
ALTER TABLE dili_trace.`user` ADD qr_history_id bigint(20) NULL COMMENT '最后的二维码变更历史ID';

ALTER TABLE dili_trace.`user_qr_history` ADD qr_history_event_id bigint(20) NULL COMMENT 'qr事件ID';
ALTER TABLE dili_trace.`user_qr_history` ADD qr_history_event_type INT NULL COMMENT 'qr事件类型';
ALTER TABLE dili_trace.`user_qr_history` ADD user_info_id bigint(20) NULL COMMENT '市场ID';

ALTER TABLE dili_trace.`user` DROP COLUMN `password`;
ALTER TABLE dili_trace.`user` DROP COLUMN `version`;


ALTER TABLE dili_trace.user_qr_history DROP COLUMN user_id;
ALTER TABLE dili_trace.user_qr_history DROP COLUMN user_name;
ALTER TABLE dili_trace.user_qr_history DROP COLUMN bill_id;
ALTER TABLE dili_trace.user_qr_history DROP COLUMN trade_request_id;

ALTER TABLE dili_trace.quality_trace_trade_bill_syncpoint DROP COLUMN order_version;
ALTER TABLE dili_trace.quality_trace_trade_bill DROP COLUMN order_version;

ALTER TABLE dili_trace.`register_bill` DROP COLUMN `version`;



ALTER TABLE dili_trace.`user_history` DROP COLUMN `password`;
ALTER TABLE dili_trace.`user_history` DROP COLUMN `version`;
ALTER TABLE dili_trace.`user_history` DROP COLUMN `card_no_front_url`;
ALTER TABLE dili_trace.`user_history` DROP COLUMN `card_no_back_url`;
ALTER TABLE dili_trace.`user_history` DROP COLUMN `business_license_url`;

ALTER TABLE dili_trace.`register_bill` ADD arrival_datetime datetime NULL COMMENT '到场时间';
ALTER TABLE dili_trace.`register_bill` ADD weighting_bill_id bigint NULL COMMENT '称重单ID';

ALTER TABLE dili_trace.`register_head` ADD arrival_datetime datetime NULL COMMENT '到场时间';
ALTER TABLE dili_trace.`register_head` ADD truck_tare_weight decimal(11, 3) NULL COMMENT '车辆皮重';
ALTER TABLE dili_trace.register_head DROP COLUMN plate;

ALTER TABLE dili_trace.`product_stock` ADD detect_failed_weight decimal(17, 3) NOT NULL DEFAULT 0.000 COMMENT '检测失败重量';


ALTER TABLE dili_trace.register_head MODIFY COLUMN upstream_id bigint(20) NULL COMMENT '上游id';


CREATE TABLE `register_head_plate` (
            `id` bigint NOT NULL AUTO_INCREMENT,
            `register_head_id` bigint NOT NULL COMMENT '台账ID',
            `plate` varchar(50)  NULL COMMENT '车牌',
            `created` datetime NOT NULL DEFAULT now(),
            `modified` datetime NOT NULL DEFAULT now(),
            PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


CREATE TABLE `default_field_detail` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `module_type` int NOT NULL COMMENT '模块',
        `field_id` varchar(50)  NULL COMMENT '字段ID',
        `field_label` varchar(50) NOT  NULL COMMENT '字段Label',
        `field_name` varchar(50) NOT  NULL COMMENT '字段名称',
        `default_value`	varchar(50)	   NULL COMMENT '默认值',
        `json_path` varchar(50) NOT NULL  COMMENT 'jsonpath',
        `json_path_type` int NOT NULL  COMMENT 'jsonpath类型',
        `created` datetime NOT NULL DEFAULT now(),
        `modified` datetime NOT NULL DEFAULT now(),
        PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


CREATE TABLE `field_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `module_type` int NOT NULL COMMENT '模块',
    `market_id` bigint NOT NULL COMMENT '市场id',
    `operator_id` bigint NOT NULL COMMENT '操作人ID',
    `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
    `created` datetime NOT NULL DEFAULT now(),
    `modified` datetime NOT NULL DEFAULT now(),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
ALTER TABLE dili_trace.field_config ADD CONSTRAINT moduletype_market_unique UNIQUE KEY (module_type,market_id);

CREATE TABLE `field_config_detail` (
       `id` bigint NOT NULL AUTO_INCREMENT,
       `field_config_id` bigint NOT NULL COMMENT '配置ID',
       `default_id` bigint NOT NULL COMMENT '配置ID',
       `displayed` int NOT NULL DEFAULT 0 COMMENT '显示',
       `required` int NOT NULL DEFAULT 0 COMMENT '是否必填',
       `default_value` varchar(50)  NULL  COMMENT '默认值',
       `available_values` json  NULL  COMMENT '可用值',
       `is_valid` int  NOT NULL COMMENT '是否有效',
       `created` datetime NOT NULL DEFAULT now(),
       `modified` datetime NOT NULL DEFAULT now(),
       PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;



CREATE TABLE `process_config` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `market_id` int NOT NULL COMMENT '模块',
      `is_need_verify` int NOT NULL DEFAULT 1 COMMENT '是否登记审核',
      `can_docheckin_without_weight` int NOT NULL DEFAULT 1 COMMENT '是否进门称重',
      `is_manully_checkIn` int NOT NULL DEFAULT 1 COMMENT '是否进门审核',

      `created` datetime NOT NULL DEFAULT now(),
      `modified` datetime NOT NULL DEFAULT now(),
      PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


CREATE TABLE `register_tallyarea_no` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `bill_id` bigint NOT NULL COMMENT '报备ID',
      `bill_type` int NOT NULL COMMENT '类型',
      `tallyarea_no` varchar(50)  NULL COMMENT '摊位号',
      `created` datetime NOT NULL DEFAULT now(),
      PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;



INSERT INTO dili_trace.default_field_detail (module_type,field_id,field_label,field_name,default_value,json_path,json_path_type,created,modified) VALUES
(1,NULL,'皮重','truckTareWeight',NULL,'$.truckTareWeight',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'是否拼车','truckType',NULL,'$.truckType',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'商品单价','unitPrice',NULL,'$.unitPrice',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'商品规格','specName',NULL,'$.specName',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'品牌','brandName',NULL,'$.brandName',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'上游企业','upStreamId',NULL,'$.upStreamId',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'产地','originId',NULL,'$.originId',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'备注','remark',NULL,'$.remark',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'到场时间','arrivalDatetime',NULL,'$.arrivalDatetime',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0'),
(1,NULL,'到货摊位','arrivalTallynos',NULL,'$.arrivalTallynos',1,'2021-02-25 17:12:11.0','2021-02-25 17:12:11.0');
INSERT INTO dili_trace.default_field_detail (module_type,field_id,field_label,field_name,default_value,json_path,json_path_type,created,modified) VALUES
(1,NULL,'上传证明','imageCertList',NULL,'$.imageCertList[*].certType',1,'2021-03-12 12:23:27.0','2021-03-12 12:23:27.0'),
(1,NULL,'计重方式','measureType',NULL,'$.measureType',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'业户名称','name',NULL,'$.name',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'企业名称','corporateName',NULL,'$.corporateName',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'自定义商品别名','productAliasName',NULL,'$.productAliasName',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'商品重量','weight',NULL,'$.weight',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'产地','originId',NULL,'$.originId',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0'),
(2,NULL,'是否打印检测报告','isPrintCheckReport',NULL,'$.isPrintCheckReport',1,'2021-03-18 16:20:42.0','2021-03-18 16:20:42.0');