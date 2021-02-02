CREATE TABLE `trade_request_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trade_request_id` bigint DEFAULT NULL,
  `trade_detail_id` bigint DEFAULT NULL,
  `trade_weight` decimal(15,3) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `bill_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;