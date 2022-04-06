DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

CREATE TABLE QRTZ_JOB_DETAILS (
  SCHED_NAME character varying(120) NOT NULL,
  JOB_NAME character varying(200) NOT NULL,
  JOB_GROUP character varying(200) NOT NULL,
  DESCRIPTION character varying(250) NULL,
  JOB_CLASS_NAME character varying(250) NOT NULL,
  IS_DURABLE boolean NOT NULL,
  IS_NONCONCURRENT boolean NOT NULL,
  IS_UPDATE_DATA boolean NOT NULL,
  REQUESTS_RECOVERY boolean NOT NULL,
  JOB_DATA bytea NULL
);

alter table QRTZ_JOB_DETAILS add primary key(SCHED_NAME,JOB_NAME,JOB_GROUP);

CREATE TABLE QRTZ_TRIGGERS (
  SCHED_NAME character varying(120) NOT NULL,
  TRIGGER_NAME character varying(200) NOT NULL,
  TRIGGER_GROUP character varying(200) NOT NULL,
  JOB_NAME character varying(200) NOT NULL,
  JOB_GROUP character varying(200) NOT NULL,
  DESCRIPTION character varying(250) NULL,
  NEXT_FIRE_TIME BIGINT NULL,
  PREV_FIRE_TIME BIGINT NULL,
  PRIORITY INTEGER NULL,
  TRIGGER_STATE character varying(16) NOT NULL,
  TRIGGER_TYPE character varying(8) NOT NULL,
  START_TIME BIGINT NOT NULL,
  END_TIME BIGINT NULL,
  CALENDAR_NAME character varying(200) NULL,
  MISFIRE_INSTR SMALLINT NULL,
  JOB_DATA bytea NULL
) ;

alter table QRTZ_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL
) ;

alter table QRTZ_SIMPLE_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CRON_TRIGGERS (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    CRON_EXPRESSION character varying(120) NOT NULL,
    TIME_ZONE_ID character varying(80)
) ;

alter table QRTZ_CRON_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    STR_PROP_1 character varying(512) NULL,
    STR_PROP_2 character varying(512) NULL,
    STR_PROP_3 character varying(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 boolean NULL,
    BOOL_PROP_2 boolean NULL
) ;

alter table QRTZ_SIMPROP_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_BLOB_TRIGGERS (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    BLOB_DATA bytea NULL
) ;

alter table QRTZ_BLOB_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CALENDARS (
    SCHED_NAME character varying(120) NOT NULL,
    CALENDAR_NAME character varying(200) NOT NULL,
    CALENDAR bytea NOT NULL
) ;

alter table QRTZ_CALENDARS add primary key(SCHED_NAME,CALENDAR_NAME);

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL
) ;

alter table QRTZ_PAUSED_TRIGGER_GRPS add primary key(SCHED_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_FIRED_TRIGGERS (
    SCHED_NAME character varying(120) NOT NULL,
    ENTRY_ID character varying(200) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    INSTANCE_NAME character varying(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    SCHED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE character varying(16) NOT NULL,
    JOB_NAME character varying(200) NULL,
    JOB_GROUP character varying(200) NULL,
    IS_NONCONCURRENT boolean NULL,
    REQUESTS_RECOVERY boolean NULL
) ;

alter table QRTZ_FIRED_TRIGGERS add primary key(SCHED_NAME,ENTRY_ID);

CREATE TABLE QRTZ_SCHEDULER_STATE (
    SCHED_NAME character varying(120) NOT NULL,
    INSTANCE_NAME character varying(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL
) ;

alter table QRTZ_SCHEDULER_STATE add primary key(SCHED_NAME,INSTANCE_NAME);

CREATE TABLE QRTZ_LOCKS (
    SCHED_NAME character varying(120) NOT NULL,
    LOCK_NAME character varying(40) NOT NULL
) ;

alter table QRTZ_LOCKS add primary key(SCHED_NAME,LOCK_NAME);

-- datavines.actual_values definition

CREATE TABLE `actual_values` (
    `id` bigserial NOT NULL,
    `task_id` int8 DEFAULT NULL,
    `metric_name` varchar(255) DEFAULT NULL,
    `unique_code` varchar(255) DEFAULT NULL,
    `actual_value` double DEFAULT NULL,
    `data_time` timestamp DEFAULT NULL,
    `create_time` timestamp DEFAULT NULL,
    `update_time` timestamp DEFAULT NULL,
    CONSTRAINT actual_values_pk PRIMARY KEY (id)
) ;

-- datavines.command definition

CREATE TABLE `command` (
    `id` bigserial NOT NULL ,
    `type` int2 DEFAULT '0' COMMENT 'Command type: 0 start task, 1 stop task, 2 recover fault-tolerant task, 3 resume waiting thread',
    `parameter` text COMMENT 'json command parameters',
    `task_id` int8 NOT NULL COMMENT 'task id',
    `priority` int4 DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
    `create_time` timestamp NOT NULL DEFAULT current_timestamp COMMENT 'create time',
    `update_time` timestamp NOT NULL DEFAULT current_timestamp COMMENT 'update time',
    CONSTRAINT command_pk PRIMARY KEY (id)
) ;

-- datavines.job definition

CREATE TABLE `job` (
    `id` bigserial NOT NULL,
    `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
    `type` int4 NOT NULL DEFAULT '0',
    `datasource_id` int8 NOT NULL,
    `parameter` text COMMENT '任务参数',
    `retry_times` int4 DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int4 DEFAULT NULL COMMENT '重试间隔',
    `timeout` int4 DEFAULT NULL COMMENT '任务超时时间',
    `timeout_strategy` int4 DEFAULT NULL COMMENT '超时策略',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '代理用户',
    `create_by` int4 DEFAULT NULL COMMENT '创建用户id',
    `create_time` timestamp NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
    `update_by` int4 DEFAULT NULL COMMENT '更新用户id',
    `update_time` timestamp NOT NULL DEFAULT current_timestamp COMMENT '更新时间',
    CONSTRAINT job_pk PRIMARY KEY (id),
    CONSTRAINT job_un UNIQUE (name)
) ;

-- datavines.server definition

CREATE TABLE `server` (
    `id` serial NOT NULL,
    `host` varchar(255) NOT NULL,
    `port` int(11) NOT NULL,
    `create_time` timestamp DEFAULT current_timestamp,
    `update_time` timestamp DEFAULT current_timestamp,
    CONSTRAINT server_pk PRIMARY KEY (id),
    CONSTRAINT server_un UNIQUE (host,port)
) ;

-- datavines.task definition

CREATE TABLE `task` (
    `id` bigserial NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `job_id` int8 NOT NULL DEFAULT '-1',
    `job_type` int4 NOT NULL DEFAULT '0',
    `datasource_id` int8 NOT NULL DEFAULT '-1',
    `execute_platform_type` varchar(128) DEFAULT NULL,
    `execute_platform_parameter` text,
    `engine_type` varchar(128) DEFAULT NULL,
    `engine_parameter` text,
    `parameter` text NOT NULL,
    `status` int4 DEFAULT NULL,
    `retry_times` int4 DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int4 DEFAULT NULL COMMENT '重试间隔',
    `timeout` int4 DEFAULT NULL COMMENT '超时时间',
    `timeout_strategy` int4 DEFAULT NULL COMMENT '超时处理策略',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '代理用户',
    `execute_host` varchar(255) DEFAULT NULL COMMENT '执行任务的主机',
    `application_id` varchar(255) DEFAULT NULL COMMENT 'yarn application id',
    `application_tag` varchar(255) DEFAULT NULL COMMENT 'yarn application tags',
    `process_id` int4 DEFAULT NULL COMMENT 'process id',
    `execute_file_path` varchar(255) DEFAULT NULL COMMENT 'execute file path',
    `log_path` varchar(255) DEFAULT NULL COMMENT 'log path',
    `env` text,
    `submit_time` timestamp DEFAULT NULL,
    `start_time` timestamp DEFAULT NULL,
    `end_time` timestamp DEFAULT NULL,
    `create_time` timestamp NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT current_timestamp ,
    CONSTRAINT task_pk PRIMARY KEY (id)
);

-- datavines.task_result definition

CREATE TABLE `task_result` (
    `id` bigserial NOT NULL,
    `metric_type` varchar(255) DEFAULT NULL,
    `metric_dimension` varchar(255) DEFAULT NULL,
    `metric_name` varchar(255) DEFAULT NULL,
    `task_id` int8 DEFAULT NULL,
    `actual_value` double DEFAULT NULL,
    `expected_value` double DEFAULT NULL,
    `expected_type` varchar(255) DEFAULT NULL,
    `result_formula` varchar(255) DEFAULT NULL,
    `operator` varchar(255) DEFAULT NULL,
    `threshold` double DEFAULT NULL,
    `failure_strategy` varchar(255) DEFAULT NULL,
    `state` varchar(255) NOT NULL DEFAULT 'none',
    `create_time` timestamp DEFAULT NULL,
    `update_time` timestamp DEFAULT NULL,
    CONSTRAINT task_result_pk PRIMARY KEY (id)
);