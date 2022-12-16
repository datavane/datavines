SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `BLOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `CALENDAR_NAME` varchar(200) NOT NULL,
    `CALENDAR` blob NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `CRON_EXPRESSION` varchar(120) NOT NULL,
    `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `ENTRY_ID` varchar(200) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `INSTANCE_NAME` varchar(200) NOT NULL,
    `FIRED_TIME` bigint(13) NOT NULL,
    `SCHED_TIME` bigint(13) NOT NULL,
    `PRIORITY` int(11) NOT NULL,
    `STATE` varchar(16) NOT NULL,
    `JOB_NAME` varchar(200) DEFAULT NULL,
    `JOB_GROUP` varchar(200) DEFAULT NULL,
    `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
    `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
    KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
    KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `JOB_NAME` varchar(200) NOT NULL,
    `JOB_GROUP` varchar(200) NOT NULL,
    `DESCRIPTION` varchar(250) DEFAULT NULL,
    `JOB_CLASS_NAME` varchar(250) NOT NULL,
    `IS_DURABLE` varchar(1) NOT NULL,
    `IS_NONCONCURRENT` varchar(1) NOT NULL,
    `IS_UPDATE_DATA` varchar(1) NOT NULL,
    `REQUESTS_RECOVERY` varchar(1) NOT NULL,
    `JOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `LOCK_NAME` varchar(40) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `INSTANCE_NAME` varchar(200) NOT NULL,
    `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
    `CHECKIN_INTERVAL` bigint(13) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `REPEAT_COUNT` bigint(7) NOT NULL,
    `REPEAT_INTERVAL` bigint(12) NOT NULL,
    `TIMES_TRIGGERED` bigint(10) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `STR_PROP_1` varchar(512) DEFAULT NULL,
    `STR_PROP_2` varchar(512) DEFAULT NULL,
    `STR_PROP_3` varchar(512) DEFAULT NULL,
    `INT_PROP_1` int(11) DEFAULT NULL,
    `INT_PROP_2` int(11) DEFAULT NULL,
    `LONG_PROP_1` bigint(20) DEFAULT NULL,
    `LONG_PROP_2` bigint(20) DEFAULT NULL,
    `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
    `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
    `BOOL_PROP_1` varchar(1) DEFAULT NULL,
    `BOOL_PROP_2` varchar(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
     `SCHED_NAME` varchar(120) NOT NULL,
     `TRIGGER_NAME` varchar(200) NOT NULL,
     `TRIGGER_GROUP` varchar(200) NOT NULL,
     `JOB_NAME` varchar(200) NOT NULL,
     `JOB_GROUP` varchar(200) NOT NULL,
     `DESCRIPTION` varchar(250) DEFAULT NULL,
     `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
     `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
     `PRIORITY` int(11) DEFAULT NULL,
     `TRIGGER_STATE` varchar(16) NOT NULL,
     `TRIGGER_TYPE` varchar(8) NOT NULL,
     `START_TIME` bigint(13) NOT NULL,
     `END_TIME` bigint(13) DEFAULT NULL,
     `CALENDAR_NAME` varchar(200) DEFAULT NULL,
     `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
     `JOB_DATA` blob,
     PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
     KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
     KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
     KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
     KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
     KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
     KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
     KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
     KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
     KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
     KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
     KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
     KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
     CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dv_actual_values` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `job_execution_id` bigint(20) DEFAULT NULL,
    `metric_name` varchar(255) DEFAULT NULL,
    `unique_code` varchar(255) DEFAULT NULL,
    `actual_value` double DEFAULT NULL,
    `data_time` datetime DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_command` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `type` tinyint(4) DEFAULT '0' COMMENT 'Command type: 0 start jobExecution, 1 stop jobExecution, 2 recover fault-tolerant jobExecution, 3 resume waiting thread',
    `parameter` text COMMENT 'json command parameters',
    `job_execution_id` bigint(20) NOT NULL COMMENT 'jobExecution id',
    `priority` int(11) DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_job` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
    `type` int(11) NOT NULL DEFAULT '0',
    `datasource_id` bigint(20) NOT NULL,
    `datasource_id_2` bigint(20) NOT NULL,
    `schema_name` varchar(128) DEFAULT NULL,
    `table_name` varchar(128) DEFAULT NULL,
    `column_name` varchar(128) DEFAULT NULL,
    `metric_type` varchar(128) DEFAULT NULL,
    `execute_platform_type` varchar(128) DEFAULT NULL,
    `execute_platform_parameter` text,
    `engine_type` varchar(128) DEFAULT NULL,
    `engine_parameter` text,
    `error_data_storage_id` bigint(20) DEFAULT NULL,
    `parameter` text COMMENT '任务参数',
    `retry_times` int(11) DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int(11) DEFAULT NULL COMMENT '重试间隔',
    `timeout` int(11) DEFAULT NULL COMMENT '任务超时时间',
    `timeout_strategy` int(11) DEFAULT NULL COMMENT '超时策略',
    `tenant_code` bigint(20) DEFAULT NULL COMMENT '代理用户',
    `env` bigint(20) DEFAULT NULL COMMENT '环境配置',
    `create_by` bigint(20) DEFAULT NULL COMMENT '创建用户id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` bigint(20) DEFAULT NULL COMMENT '更新用户id',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_name` (`name`,`schema_name`,`table_name`,`column_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_job_schedule` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `type` varchar(255) NOT NULL,
    `param` text DEFAULT NULL,
    `job_id` bigint(20) NOT NULL,
    `cron_expression` varchar(255) DEFAULT NULL,
    `status` tinyint(1) DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `create_by` bigint(20) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_by` bigint(20) DEFAULT NULL,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_server` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `host` varchar(255) NOT NULL,
    `port` int(11) NOT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `server_un` (`host`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_job_execution` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `job_id` bigint(20) NOT NULL DEFAULT '-1',
    `job_type` int(11) NOT NULL DEFAULT '0',
    `datasource_id` bigint(20) NOT NULL DEFAULT '-1',
    `execute_platform_type` varchar(128) DEFAULT NULL,
    `execute_platform_parameter` text,
    `engine_type` varchar(128) DEFAULT NULL,
    `engine_parameter` text,
    `error_data_storage_type` varchar(128) DEFAULT NULL,
    `error_data_storage_parameter` text,
    `error_data_file_name` varchar(255) DEFAULT NULL,
    `parameter` text NOT NULL,
    `status` int(11) DEFAULT NULL,
    `retry_times` int(11) DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int(11) DEFAULT NULL COMMENT '重试间隔',
    `timeout` int(11) DEFAULT NULL COMMENT '超时时间',
    `timeout_strategy` int(11) DEFAULT NULL COMMENT '超时处理策略',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '代理用户',
    `execute_host` varchar(255) DEFAULT NULL COMMENT '执行任务的主机',
    `application_id` varchar(255) DEFAULT NULL COMMENT 'yarn application id',
    `application_tag` varchar(255) DEFAULT NULL COMMENT 'yarn application tags',
    `process_id` int(11) DEFAULT NULL COMMENT 'process id',
    `execute_file_path` varchar(255) DEFAULT NULL COMMENT 'execute file path',
    `log_path` varchar(255) DEFAULT NULL COMMENT 'log path',
    `env` text,
    `submit_time` datetime DEFAULT NULL,
    `schedule_time` datetime DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_job_execution_result` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `job_execution_id` bigint(20) DEFAULT NULL,
    `metric_type` varchar(255) DEFAULT NULL,
    `metric_dimension` varchar(255) DEFAULT NULL,
    `metric_name` varchar(255) DEFAULT NULL,
    `database_name` varchar(255) DEFAULT NULL,
    `table_name` varchar(255) DEFAULT NULL,
    `column_name` varchar(255) DEFAULT NULL,
    `actual_value` double DEFAULT NULL,
    `expected_value` double DEFAULT NULL,
    `expected_type` varchar(255) DEFAULT NULL,
    `result_formula` varchar(255) DEFAULT NULL,
    `operator` varchar(255) DEFAULT NULL,
    `threshold` double DEFAULT NULL,
    `state` int(2) NOT NULL DEFAULT 0,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_datasource` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `uuid` varchar(64) NOT NULL COMMENT '唯一ID',
    `name` varchar(255) NOT NULL,
    `type` varchar(255) NOT NULL,
    `param` text NOT NULL,
    `workspace_id` bigint(20) NOT NULL,
    `create_by` bigint(20) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_by` bigint(20) DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `datasource_un` (`name`,`workspace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_workspace` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `create_by` bigint(20) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_by` bigint(20) DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `workspace_un` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL,
    `password` varchar(255) NOT NULL,
    `email` varchar(255) NOT NULL,
    `phone` varchar(127) DEFAULT NULL,
    `admin` tinyint(1) NOT NULL DEFAULT '0',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_un` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS dv_sla;
CREATE TABLE dv_sla (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    workspace_id bigint(20) NOT NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    create_by bigint(20) DEFAULT NULL,
    create_time datetime default current_timestamp,
    update_by bigint(20) DEFAULT NULL,
    update_time datetime default current_timestamp,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS dv_sla_job;
CREATE TABLE dv_sla_job (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    sla_id bigint(20) NOT NULL,
    workspace_id bigint(20) NOT NULL,
    job_id bigint(20) NOT NULL,
    create_by bigint(20) DEFAULT NULL,
    create_time datetime DEFAULT current_timestamp,
    update_by bigint(20) DEFAULT NULL,
    update_time datetime DEFAULT current_timestamp,
    PRIMARY KEY (`id`)
);

DROP TABLE if EXISTS dv_sla_notification;
CREATE TABLE dv_sla_notification(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    type VARCHAR(40) NOT NULL,
    workspace_id bigint(20) NOT NULL,
    sla_id bigint(20) NOT NULL,
    sender_id bigint(20) NOT null,
    config text DEFAULT NULL ,
    create_by bigint(20) DEFAULT NULL,
    create_time datetime DEFAULT current_timestamp,
    update_by bigint(20) DEFAULT NULL,
    update_time datetime DEFAULT current_timestamp,
    PRIMARY KEY (`id`)
);

DROP TABLE if exists dv_sla_sender;
CREATE TABLE dv_sla_sender(
    id bigint(20) NOT NULL AUTO_INCREMENT,
    type VARCHAR(40) NOT NULL,
    name VARCHAR(255) NOT NULL,
    workspace_id bigint(20) NOT NULL,
    config text NOT NULL,
    create_by bigint(20) DEFAULT NULL,
    create_time datetime DEFAULT current_timestamp,
    update_by bigint(20) DEFAULT NULL,
    update_time datetime DEFAULT current_timestamp,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `dv_env`;
CREATE TABLE `dv_env` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '服务器环境配置',
  `env` text NOT NULL,
  `workspace_id` bigint(20) NOT NULL,
  `create_by` bigint(20) NOT NULL COMMENT '创建用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新用户id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `env_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_tenant`;
CREATE TABLE `dv_tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant` varchar(255) NOT NULL COMMENT 'Linux服务器用户',
  `workspace_id` bigint(20) NOT NULL,
  `create_by` bigint(20) NOT NULL COMMENT '创建用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新用户id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_name` (`tenant`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_error_data_storage`;
CREATE TABLE `dv_error_data_storage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `param` text NOT NULL,
  `workspace_id` bigint(20) NOT NULL,
  `create_by` int(11) NOT NULL COMMENT '创建用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int(11) NOT NULL COMMENT '更新用户id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_wp_un` (`name`,`workspace_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_user_workspace`;
CREATE TABLE `dv_user_workspace` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `workspace_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `dv_user` (`id`, `username`, `password`, `email`, `phone`, `admin`, `create_time`, `update_time`) VALUES ('1', 'admin', '$2a$10$9ZcicUYFl/.knBi9SE53U.Nml8bfNeArxr35HQshxXzimbA6Ipgqq', 'admin@gmail.com', NULL, '0', NULL, '2022-05-04 22:08:24');
INSERT INTO `dv_workspace` (`id`, `name`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1', "admin\'s default", '1', '2022-05-20 23:01:18', '1', '2022-05-20 23:01:21');
INSERT INTO `dv_user_workspace` (`id`, `user_id`, `workspace_id`, `role_id`,`create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1', '1', '1', '1','1', '2022-07-16 20:34:02', '1', '2022-07-16 20:34:02');

CREATE TABLE `dv_catalog_entity_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) NOT NULL COMMENT '唯一ID',
  `name` varchar(255) NOT NULL COMMENT '实体定义的名字',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `properties` text COMMENT '实体参数，用List存储 例如 [{"name":"id","type":"string"}]',
  `super_uuid` varchar(64) NOT NULL DEFAULT '-1' COMMENT '父类ID',
  `create_by` bigint(20) NOT NULL COMMENT '创建用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_entity_definition_un` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体定义';

CREATE TABLE `dv_catalog_entity_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) NOT NULL COMMENT '唯一ID',
  `type` varchar(127) NOT NULL COMMENT '实体类型',
  `datasource_id` bigint(20) NOT NULL COMMENT '数据源id',
  `fully_qualified_name` varchar(255) NOT NULL COMMENT '全限定名',
  `display_name` varchar(255) NOT NULL COMMENT '展示名字',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `properties` text COMMENT '其他参数，用map存储',
  `owner` varchar(255) DEFAULT NULL COMMENT '拥有者',
  `version` varchar(64) NOT NULL DEFAULT '1.0' COMMENT '版本',
  `status` varchar(255) DEFAULT 'active' COMMENT '实体状态：active/deleted',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_entity_instance_un` (`uuid`),
  FULLTEXT KEY `full_idx_display_name_description` (`display_name`,`description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体';

CREATE TABLE `dv_catalog_entity_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity1_uuid` varchar(64) NOT NULL COMMENT '实体1uuid',
  `entity2_uuid` varchar(64) NOT NULL COMMENT '实体2uuid',
  `direction` varchar(64) NOT NULL COMMENT '关系方向，up-2是1上游，down-2是1下游',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_entity_rel_un` (`entity1_uuid`,`entity2_uuid`,`direction`),
  KEY `idx_entity2_uuid` (`entity2_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体关联关系';

CREATE TABLE `dv_catalog_schema_change` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `entity_uuid` varchar(64) NOT NULL COMMENT '实体uuid',
    `change_type` varchar(64) NOT NULL COMMENT '变更类型',
    `database` varchar(64) DEFAULT NULL COMMENT '数据库',
    `table` varchar(64) DEFAULT NULL COMMENT '表',
    `column` varchar(64) DEFAULT NULL COMMENT '列',
    `change_before` text NOT NULL COMMENT '变更前',
    `change_after` text NOT NULL COMMENT '变更后',
    `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema变更记录表';

CREATE TABLE `dv_catalog_command` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `task_id` bigint(20) NOT NULL COMMENT 'jobExecution id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_catalog_task` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `datasource_id` bigint(20) NOT NULL DEFAULT '-1',
    `status` int(11) DEFAULT NULL,
    `parameter` text NULL,
    `execute_host` varchar(255) DEFAULT NULL COMMENT '执行任务的主机',
    `submit_time` datetime DEFAULT NULL,
    `schedule_time` datetime DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_catalog_task_schedule`;
CREATE TABLE `dv_catalog_task_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `param` text,
  `datasource_id` bigint(20) NOT NULL,
  `cron_expression` varchar(255) DEFAULT NULL,
  `status` tinyint(1) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dv_catalog_entity_metric_job_rel` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `entity_uuid` varchar(64) NOT NULL COMMENT '实体uuid',
    `metric_job_id` bigint(20) NOT NULL COMMENT 'metric job id',
    `create_by` bigint(20) NOT NULL COMMENT '创建用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `dv_catalog_entity_metric_rel_un` (`entity_uuid`,`metric_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体和规则作业关联关系';

DROP TABLE IF EXISTS `dv_catalog_tag`;
CREATE TABLE `dv_catalog_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) NOT NULL ,
  `name` varchar(256) NOT NULL,
  `create_by` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_tag_uuid_un` (`uuid`),
  UNIQUE KEY `dv_tag_name_un` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_catalog_tag_category`;
CREATE TABLE `dv_catalog_tag_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) NOT NULL,
  `category_uuid` varchar(64) NOT NULL,
  `name` varchar(256) NOT NULL,
  `workspace_id` bigint(20) NOT NULL,
  `create_by` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_tag_category_uuid_un` (`uuid`),
  UNIQUE KEY `dv_tag_category_name_un` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `dv_catalog_entity_tag_rel`;
CREATE TABLE `dv_catalog_entity_tag_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_uuid` varchar(64) NOT NULL ,
  `tag_uuid` varchar(64) NOT NULL ,
  `create_by` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) NOT NULL COMMENT '更新用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dv_entity_tag_rel_un` (`entity_uuid`,`tag_uuid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='实体标签关联关系';
