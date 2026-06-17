DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS CASCADE;
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_CALENDARS CASCADE;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS CASCADE;
DROP TABLE IF EXISTS QRTZ_LOCKS CASCADE;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE CASCADE;
CREATE TABLE QRTZ_JOB_DETAILS (
    SCHED_NAME varchar(120) NOT NULL,
    JOB_NAME varchar(200) NOT NULL,
    JOB_GROUP varchar(200) NOT NULL,
    DESCRIPTION varchar(250) DEFAULT NULL,
    JOB_CLASS_NAME varchar(250) NOT NULL,
    IS_DURABLE varchar(1) NOT NULL,
    IS_NONCONCURRENT varchar(1) NOT NULL,
    IS_UPDATE_DATA varchar(1) NOT NULL,
    REQUESTS_RECOVERY varchar(1) NOT NULL,
    JOB_DATA bytea,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);
CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP);
CREATE TABLE QRTZ_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    JOB_NAME varchar(200) NOT NULL,
    JOB_GROUP varchar(200) NOT NULL,
    DESCRIPTION varchar(250) DEFAULT NULL,
    NEXT_FIRE_TIME bigint DEFAULT NULL,
    PREV_FIRE_TIME bigint DEFAULT NULL,
    PRIORITY integer DEFAULT NULL,
    TRIGGER_STATE varchar(16) NOT NULL,
    TRIGGER_TYPE varchar(8) NOT NULL,
    START_TIME bigint NOT NULL,
    END_TIME bigint DEFAULT NULL,
    CALENDAR_NAME varchar(200) DEFAULT NULL,
    MISFIRE_INSTR smallint DEFAULT NULL,
    JOB_DATA bytea,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_TRIGGERS_ibfk_1
        FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);
CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    REPEAT_COUNT bigint NOT NULL,
    REPEAT_INTERVAL bigint NOT NULL,
    TIMES_TRIGGERED bigint NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_SIMPLE_TRIGGERS_ibfk_1
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);
CREATE TABLE QRTZ_CRON_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    CRON_EXPRESSION varchar(120) NOT NULL,
    TIME_ZONE_ID varchar(80) DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_CRON_TRIGGERS_ibfk_1
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);
CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    STR_PROP_1 varchar(512) DEFAULT NULL,
    STR_PROP_2 varchar(512) DEFAULT NULL,
    STR_PROP_3 varchar(512) DEFAULT NULL,
    INT_PROP_1 integer DEFAULT NULL,
    INT_PROP_2 integer DEFAULT NULL,
    LONG_PROP_1 bigint DEFAULT NULL,
    LONG_PROP_2 bigint DEFAULT NULL,
    DEC_PROP_1 decimal(13,4) DEFAULT NULL,
    DEC_PROP_2 decimal(13,4) DEFAULT NULL,
    BOOL_PROP_1 varchar(1) DEFAULT NULL,
    BOOL_PROP_2 varchar(1) DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_SIMPROP_TRIGGERS_ibfk_1
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);
CREATE TABLE QRTZ_BLOB_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    BLOB_DATA bytea,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_BLOB_TRIGGERS_ibfk_1
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);
CREATE TABLE QRTZ_FIRED_TRIGGERS (
    SCHED_NAME varchar(120) NOT NULL,
    ENTRY_ID varchar(200) NOT NULL,
    TRIGGER_NAME varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    INSTANCE_NAME varchar(200) NOT NULL,
    FIRED_TIME bigint NOT NULL,
    SCHED_TIME bigint NOT NULL,
    PRIORITY integer NOT NULL,
    STATE varchar(16) NOT NULL,
    JOB_NAME varchar(200) DEFAULT NULL,
    JOB_GROUP varchar(200) DEFAULT NULL,
    IS_NONCONCURRENT varchar(1) DEFAULT NULL,
    REQUESTS_RECOVERY varchar(1) DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
);
CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);
CREATE TABLE QRTZ_CALENDARS (
    SCHED_NAME varchar(120) NOT NULL,
    CALENDAR_NAME varchar(200) NOT NULL,
    CALENDAR bytea NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
);
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
    SCHED_NAME varchar(120) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
);
CREATE TABLE QRTZ_LOCKS (
    SCHED_NAME varchar(120) NOT NULL,
    LOCK_NAME varchar(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
);
CREATE TABLE QRTZ_SCHEDULER_STATE (
    SCHED_NAME varchar(120) NOT NULL,
    INSTANCE_NAME varchar(200) NOT NULL,
    LAST_CHECKIN_TIME bigint NOT NULL,
    CHECKIN_INTERVAL bigint NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
);
-- ----------------------------
-- Table structure for dv_actual_values
-- ----------------------------
DROP TABLE IF EXISTS dv_actual_values;
CREATE TABLE dv_actual_values (
  id bigserial,
  job_execution_id bigint DEFAULT NULL,
  metric_name varchar(255) DEFAULT NULL,
  unique_code varchar(255) DEFAULT NULL,
  actual_value decimal(20,4) DEFAULT NULL,
  data_time timestamp DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_common_task_command
-- ----------------------------
DROP TABLE IF EXISTS dv_common_task_command;
CREATE TABLE dv_common_task_command (
  id bigserial,
  task_id bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_catalog_entity_definition
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_definition;
CREATE TABLE dv_catalog_entity_definition (
  id bigserial,
  uuid varchar(64) NOT NULL,
  name varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  properties text,
  super_uuid varchar(64) NOT NULL DEFAULT '-1',
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (uuid)
);

-- ----------------------------
-- Table structure for dv_catalog_entity_instance
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_instance;
CREATE TABLE dv_catalog_entity_instance (
  id bigserial,
  uuid varchar(64) NOT NULL,
  type varchar(127) NOT NULL,
  datasource_id bigint NOT NULL,
  fully_qualified_name varchar(255)  NOT NULL,
  display_name varchar(255) NOT NULL,
  description varchar(1024) DEFAULT NULL,
  properties text,
  owner varchar(255) DEFAULT NULL,
  version varchar(64) NOT NULL DEFAULT '1.0',
  status varchar(255) DEFAULT 'active',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (uuid),
  UNIQUE (datasource_id,fully_qualified_name,status)
);
CREATE INDEX full_idx_display_name_description ON dv_catalog_entity_instance (display_name,description);

-- ----------------------------
-- Table structure for dv_catalog_entity_metric_job_rel
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_metric_job_rel;
CREATE TABLE dv_catalog_entity_metric_job_rel (
  id bigserial,
  entity_uuid varchar(64) NOT NULL,
  metric_job_id bigint NOT NULL,
  metric_job_type varchar(255) NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (entity_uuid,metric_job_id,metric_job_type)
);

-- ----------------------------
-- Table structure for dv_catalog_entity_profile
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_profile;
CREATE TABLE dv_catalog_entity_profile (
  id bigserial,
  entity_uuid varchar(64) NOT NULL,
  metric_name varchar(255) NOT NULL,
  actual_value text NOT NULL,
  actual_value_type varchar(255) DEFAULT NULL,
  data_date varchar(255) DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (entity_uuid,metric_name,data_date)
);

-- ----------------------------
-- Table structure for dv_catalog_entity_rel
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_rel;
CREATE TABLE dv_catalog_entity_rel (
  id bigserial,
  entity1_uuid varchar(64) NOT NULL,
  entity2_uuid varchar(64) NOT NULL,
  type varchar(64) NOT NULL,
  source_type varchar(64) DEFAULT NULL,
  related_script text DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (entity1_uuid,entity2_uuid,type)
);
CREATE INDEX idx_entity2_uuid ON dv_catalog_entity_rel (entity2_uuid);

-- ----------------------------
-- Table structure for dv_catalog_entity_tag_rel
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_entity_tag_rel;
CREATE TABLE dv_catalog_entity_tag_rel (
  id bigserial,
  entity_uuid varchar(64) NOT NULL,
  tag_uuid varchar(64) NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (entity_uuid,tag_uuid)
);
CREATE INDEX dv_catalog_entity_tag_rel_idx_entity2_uuid ON dv_catalog_entity_tag_rel (tag_uuid);

-- ----------------------------
-- Table structure for dv_catalog_schema_change
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_schema_change;
CREATE TABLE dv_catalog_schema_change (
  id bigserial,
  parent_uuid varchar(64) NOT NULL,
  entity_uuid varchar(64) NOT NULL,
  change_type varchar(64) NOT NULL,
  database_name varchar(255) DEFAULT NULL,
  table_name varchar(255) DEFAULT NULL,
  column_name varchar(255) DEFAULT NULL,
  change_before text DEFAULT NULL,
  change_after text DEFAULT NULL,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_catalog_tag
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_tag;
CREATE TABLE dv_catalog_tag (
  id bigserial,
  uuid varchar(64) NOT NULL,
  category_uuid varchar(64) NOT NULL,
  name varchar(256) NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (uuid,category_uuid,name)
);

-- ----------------------------
-- Table structure for dv_catalog_tag_category
-- ----------------------------
DROP TABLE IF EXISTS dv_catalog_tag_category;
CREATE TABLE dv_catalog_tag_category (
  id bigserial,
  uuid varchar(64) NOT NULL,
  name varchar(256) NOT NULL,
  workspace_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (uuid,name)
);

-- ----------------------------
-- Table structure for dv_common_task
-- ----------------------------
DROP TABLE IF EXISTS dv_common_task;
CREATE TABLE dv_common_task (
  id bigserial,
  task_type varchar(128) DEFAULT NULL,
  type varchar(128) DEFAULT NULL,
  datasource_id bigint NOT NULL DEFAULT '-1',
  database_name varchar(128) DEFAULT NULL,
  table_name varchar(128) DEFAULT NULL,
  status integer DEFAULT NULL,
  parameter text,
  execute_host varchar(255) DEFAULT NULL,
  submit_time timestamp DEFAULT NULL,
  schedule_time timestamp DEFAULT NULL,
  start_time timestamp DEFAULT NULL,
  end_time timestamp DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_common_task_schedule
-- ----------------------------
DROP TABLE IF EXISTS dv_common_task_schedule;
CREATE TABLE dv_common_task_schedule (
  id bigserial,
  task_type varchar(128) DEFAULT NULL,
  type varchar(255) NOT NULL,
  param text,
  datasource_id bigint NOT NULL,
  cron_expression varchar(255) DEFAULT NULL,
  status boolean DEFAULT NULL,
  start_time timestamp DEFAULT NULL,
  end_time timestamp DEFAULT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_command
-- ----------------------------
DROP TABLE IF EXISTS dv_command;
CREATE TABLE dv_command (
  id bigserial,
  type smallint NOT NULL DEFAULT '0',
  parameter text,
  execute_host varchar(255),
  job_execution_id bigint NOT NULL,
  priority integer DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_datasource
-- ----------------------------
DROP TABLE IF EXISTS dv_datasource;
CREATE TABLE dv_datasource (
  id bigserial,
  uuid varchar(64) NOT NULL,
  name varchar(255) NOT NULL,
  category varchar(255) DEFAULT 'database',
  type varchar(255) NOT NULL,
  param text NOT NULL,
  param_code text NULL,
  workspace_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (name)
);

-- ----------------------------
-- Table structure for dv_env
-- ----------------------------
DROP TABLE IF EXISTS dv_env;
CREATE TABLE dv_env (
  id bigserial,
  name varchar(255) NOT NULL,
  env text NOT NULL,
  workspace_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (name)
);

DROP TABLE IF EXISTS dv_access_token;
CREATE TABLE dv_access_token (
  id bigserial,
    workspace_id bigint NOT NULL,
    user_id bigint NOT NULL,
    token varchar(1024) NOT NULL,
    expire_time timestamp NOT NULL,
    create_by bigint NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by bigint NOT NULL,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_error_data_storage
-- ----------------------------
DROP TABLE IF EXISTS dv_error_data_storage;
CREATE TABLE dv_error_data_storage (
  id bigserial,
  name varchar(255) NOT NULL,
  type varchar(255) NOT NULL,
  param text NOT NULL,
  workspace_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (name,workspace_id)
);

-- ----------------------------
-- Table structure for dv_issue
-- ----------------------------
DROP TABLE IF EXISTS dv_issue;
CREATE TABLE dv_issue (
  id bigserial,
  title varchar(1024) DEFAULT NULL,
  content text NOT NULL,
  status varchar(255) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_job
-- ----------------------------
DROP TABLE IF EXISTS dv_job;
CREATE TABLE dv_job (
  id bigserial,
    name varchar(255) DEFAULT NULL,
    type integer NOT NULL DEFAULT '0',
    datasource_id bigint NOT NULL,
    datasource_id_2 bigint DEFAULT NULL,
    schema_name varchar(128) DEFAULT NULL,
    table_name varchar(128) DEFAULT NULL,
    column_name varchar(128) DEFAULT NULL,
    selected_column text,
    metric_type varchar(255) DEFAULT NULL,
    execute_platform_type varchar(128) DEFAULT NULL,
    execute_platform_parameter text,
    engine_type varchar(128) DEFAULT NULL,
    engine_parameter text,
    error_data_storage_id bigint DEFAULT NULL,
    is_error_data_output_to_datasource smallint DEFAULT '0',
    error_data_output_to_datasource_database varchar(255) DEFAULT NULL,
    parameter text,
    retry_times integer DEFAULT NULL,
    retry_interval integer DEFAULT NULL,
    timeout integer DEFAULT NULL,
    timeout_strategy integer DEFAULT NULL,
    pre_sql text DEFAULT NULL,
    post_sql text DEFAULT NULL,
    tenant_code bigint DEFAULT NULL,
    env bigint DEFAULT NULL,
    create_by bigint NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by bigint NOT NULL,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
  UNIQUE (name,datasource_id,schema_name,table_name,column_name)
);

-- ----------------------------
-- Table structure for dv_job_execution
-- ----------------------------
DROP TABLE IF EXISTS dv_job_execution;
CREATE TABLE dv_job_execution (
  id bigserial,
  name varchar(255) NOT NULL,
  job_id bigint NOT NULL DEFAULT '-1',
  job_type integer NOT NULL DEFAULT '0',
  schema_name varchar(128) DEFAULT NULL,
  table_name varchar(128) DEFAULT NULL,
  column_name varchar(128) DEFAULT NULL,
  metric_type varchar(255) DEFAULT NULL,
  datasource_id bigint NOT NULL DEFAULT '-1',
  execute_platform_type varchar(128) DEFAULT NULL,
  execute_platform_parameter text,
  engine_type varchar(128) DEFAULT NULL,
  engine_parameter text,
  error_data_storage_type varchar(128) DEFAULT NULL,
  error_data_storage_parameter text,
  error_data_file_name varchar(255) DEFAULT NULL,
  parameter text NOT NULL,
  status integer DEFAULT NULL,
  retry_times integer DEFAULT NULL,
  retry_interval integer DEFAULT NULL,
  timeout integer DEFAULT NULL,
  timeout_strategy integer DEFAULT NULL,
  pre_sql text DEFAULT NULL,
  post_sql text DEFAULT NULL,
  tenant_code varchar(255) DEFAULT NULL,
  execute_host varchar(255) DEFAULT NULL,
  application_id varchar(255) DEFAULT NULL,
  application_tag varchar(255) DEFAULT NULL,
  process_id integer DEFAULT NULL,
  execute_file_path varchar(255) DEFAULT NULL,
  log_path varchar(255) DEFAULT NULL,
  env text,
  submit_time timestamp DEFAULT NULL,
  schedule_time timestamp DEFAULT NULL,
  start_time timestamp DEFAULT NULL,
  end_time timestamp DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_job_execution_result
-- ----------------------------
DROP TABLE IF EXISTS dv_job_execution_result;
CREATE TABLE dv_job_execution_result (
  id bigserial,
  job_execution_id bigint DEFAULT NULL,
  metric_unique_key varchar(255) DEFAULT NULL,
  metric_type varchar(255) DEFAULT NULL,
  metric_dimension varchar(255) DEFAULT NULL,
  metric_name varchar(255) DEFAULT NULL,
  database_name varchar(128) DEFAULT NULL,
  table_name varchar(128) DEFAULT NULL,
  column_name varchar(128) DEFAULT NULL,
  actual_value decimal(20,4) DEFAULT NULL,
  expected_value decimal(20,4) DEFAULT NULL,
  expected_type varchar(255) DEFAULT NULL,
  result_formula varchar(255) DEFAULT NULL,
  operator varchar(255) DEFAULT NULL,
  threshold decimal(20,4) DEFAULT NULL,
  score decimal(20,4) DEFAULT 0,
  state integer NOT NULL DEFAULT '0',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (job_execution_id,metric_unique_key)
);

-- ----------------------------
-- Table structure for dv_job_quality_report
-- ----------------------------
DROP TABLE IF EXISTS dv_job_quality_report;
CREATE TABLE dv_job_quality_report (
  id bigserial,
    datasource_id bigint DEFAULT NULL,
    entity_level varchar(128) DEFAULT NULL,
    database_name varchar(128) DEFAULT NULL,
    table_name varchar(128) DEFAULT NULL,
    column_name varchar(128) DEFAULT NULL,
    score decimal(20,4) DEFAULT NULL,
    report_date date DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_job_execution_result_report_rel
-- ----------------------------
DROP TABLE IF EXISTS dv_job_execution_result_report_rel;
CREATE TABLE dv_job_execution_result_report_rel (
  id bigserial,
    quality_report_id bigint NOT NULL,
    job_execution_result_id bigint NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
  UNIQUE (job_execution_result_id,quality_report_id)
);

-- ----------------------------
-- Table structure for dv_job_issue_rel
-- ----------------------------
DROP TABLE IF EXISTS dv_job_issue_rel;
CREATE TABLE dv_job_issue_rel (
  id bigserial,
  job_id bigint NOT NULL,
  issue_id bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (job_id,issue_id)
);
CREATE INDEX dv_job_issue_rel_idx_entity2_uuid ON dv_job_issue_rel (issue_id);

-- ----------------------------
-- Table structure for dv_job_schedule
-- ----------------------------
DROP TABLE IF EXISTS dv_job_schedule;
CREATE TABLE dv_job_schedule (
  id bigserial,
  type varchar(255) NOT NULL,
  param text,
  job_id bigint NOT NULL,
  cron_expression varchar(255) DEFAULT NULL,
  status boolean DEFAULT NULL,
  start_time timestamp DEFAULT NULL,
  end_time timestamp DEFAULT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_server
-- ----------------------------
DROP TABLE IF EXISTS dv_server;
CREATE TABLE dv_server (
  id serial,
  host varchar(255) NOT NULL,
  port integer NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (host,port)
);

DROP TABLE IF EXISTS dv_registry_lock;
CREATE TABLE dv_registry_lock
(
  id bigserial,
  lock_key varchar(256) NOT NULL,
  lock_owner varchar(256) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (lock_key)
);
CREATE INDEX idx_upt ON dv_registry_lock (update_time);

-- ----------------------------
-- Table structure for dv_sla
-- ----------------------------
DROP TABLE IF EXISTS dv_sla;
CREATE TABLE dv_sla (
  id bigserial,
  workspace_id bigint NOT NULL,
  name varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_sla_job
-- ----------------------------
DROP TABLE IF EXISTS dv_sla_job;
CREATE TABLE dv_sla_job (
  id bigserial,
  workspace_id bigint NOT NULL,
  sla_id bigint NOT NULL,
  job_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (workspace_id,sla_id,job_id)
);

-- ----------------------------
-- Table structure for dv_sla_notification
-- ----------------------------
DROP TABLE IF EXISTS dv_sla_notification;
CREATE TABLE dv_sla_notification (
  id bigserial,
  type varchar(40) NOT NULL,
  workspace_id bigint NOT NULL,
  sla_id bigint NOT NULL,
  sender_id bigint NOT NULL,
  config text,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_sla_sender
-- ----------------------------
DROP TABLE IF EXISTS dv_sla_sender;
CREATE TABLE dv_sla_sender (
  id bigserial,
  type varchar(40) NOT NULL,
  name varchar(255) NOT NULL,
  workspace_id bigint NOT NULL,
  config text NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_tenant
-- ----------------------------
DROP TABLE IF EXISTS dv_tenant;
CREATE TABLE dv_tenant (
  id bigserial,
  tenant varchar(255) NOT NULL,
  workspace_id bigint NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (tenant)
);

-- ----------------------------
-- Table structure for dv_user
-- ----------------------------
DROP TABLE IF EXISTS dv_user;
CREATE TABLE dv_user (
  id bigserial,
  username varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  phone varchar(127) DEFAULT NULL,
  admin smallint NOT NULL DEFAULT '0',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (username)
);

-- ----------------------------
-- Table structure for dv_user_workspace
-- ----------------------------
DROP TABLE IF EXISTS dv_user_workspace;
CREATE TABLE dv_user_workspace (
  id bigserial,
  user_id bigint NOT NULL,
  workspace_id bigint NOT NULL,
  role_id bigint DEFAULT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dv_workspace
-- ----------------------------
DROP TABLE IF EXISTS dv_workspace;
CREATE TABLE dv_workspace (
  id bigserial,
  name varchar(255) NOT NULL,
  create_by bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by bigint NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (name)
);

-- ----------------------------
-- Table structure for dv_config
-- ----------------------------
DROP TABLE IF EXISTS dv_config;
CREATE TABLE dv_config (
  id bigserial,
    workspace_id bigint NOT NULL,
    var_key varchar(255) NOT NULL,
    var_value text NOT NULL,
    is_default smallint NOT NULL,
    create_by bigint NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by bigint NOT NULL,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

INSERT INTO dv_config VALUES ('1', '-1', 'data.quality.jar.name', '/libs/datavines-engine-spark-core-1.0.0-SNAPSHOT.jar', '1', '1', '2023-09-02 16:52:56', '1', '2023-09-03 09:56:12');
INSERT INTO dv_config VALUES ('2', '-1', 'yarn.mode', 'standalone', '1', '1', '2023-09-02 18:28:59', '1', '2023-09-03 12:46:24');
INSERT INTO dv_config VALUES ('3', '-1', 'yarn.application.status.address', 'http://%s:%s/ws/v1/cluster/apps/%s', '1', '1', '2023-09-03 09:57:01', '1', '2023-09-03 09:57:01');
INSERT INTO dv_config VALUES ('4', '-1', 'yarn.resource.manager.http.address.port', '8088', '1', '1', '2023-09-03 09:57:34', '1', '2023-09-03 09:57:34');
INSERT INTO dv_config VALUES ('5', '-1', 'yarn.resource.manager.ha.ids', '192.168.0.x,192.168.0.x', '1', '1', '2023-09-03 09:58:17', '1', '2023-09-03 09:58:17');
INSERT INTO dv_config VALUES ('7', '-1', 'max.cpu.load.avg', '10', '1', '1', '2023-09-03 09:59:06', '1', '2023-09-03 09:59:06');
INSERT INTO dv_config VALUES ('8', '-1', 'reserved.memory', '0.3f', '1', '1', '2023-09-03 09:59:28', '1', '2023-09-03 09:59:28');
INSERT INTO dv_config VALUES ('9', '-1', 'file.max.length', '10000000', '1', '1', '2023-09-03 14:57:33', '1', '2023-09-03 14:57:33');
INSERT INTO dv_config VALUES ('10', '-1', 'error.data.dir', '/tmp/datavines/error-data', '1', '1', '2023-09-03 14:58:01', '1', '2023-09-03 14:58:01');
INSERT INTO dv_config VALUES ('11', '-1', 'validate.result.data.dir', '/tmp/datavines/validate-result-data', '1', '1', '2023-09-03 14:58:29', '1', '2023-09-03 14:58:29');
INSERT INTO dv_config VALUES ('12', '-1', 'local.execution.threshold', '1000', '1', '1', '2023-09-03 15:02:38', '1', '2023-09-03 15:02:38');
INSERT INTO dv_config VALUES ('13', '-1', 'spark.execution.threshold', '1000', '1', '1', '2023-09-03 15:02:38', '1', '2023-09-03 15:02:38');
INSERT INTO dv_config VALUES ('14', '-1', 'livy.uri', 'http://localhost:8998/batches', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('15', '-1', 'livy.task.appId.retry.count', '3', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('16', '-1', 'livy.need.kerberos', 'false', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('17', '-1', 'livy.server.auth.kerberos.principal', 'livy/kerberos.principal', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('18', '-1', 'livy.server.auth.kerberos.keytab', '/path/to/livy/keytab/file', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('19', '-1', 'livy.task.proxyUser', 'root', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('20', '-1', 'livy.task.jar.lib.path', 'hdfs:///datavines/lib', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('21', '-1', 'livy.execution.threshold', '1000', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('22', '-1', 'livy.task.jars', CONCAT('datavines-common-1.0.0-SNAPSHOT.jar,datavines-spi-1.0.0-SNAPSHOT.jar,'
                                                                    'datavines-engine-spark-api-1.0.0-SNAPSHOT.jar,datavines-engine-spark-connector-jdbc-1.0.0-SNAPSHOT.jar,'
                                                                    'datavines-engine-core-1.0.0-SNAPSHOT.jar,datavines-engine-common-1.0.0-SNAPSHOT.jar,datavines-engine-spark-transform-sql-1.0.0-SNAPSHOT.jar,'
                                                                    'datavines-engine-api-1.0.0-SNAPSHOT.jar,mysql-connector-j-8.4.0.jar,httpclient-4.4.1.jar,'
                                                                    'httpcore-4.4.1.jar,postgresql-42.2.6.jar,presto-jdbc-0.283.jar,trino-jdbc-407.jar,clickhouse-jdbc-0.1.53.jar,'
                                                                    'mongo-java-driver-3.9.0.jar,mongo-spark-connector_2.11-2.4.0.jar,datavines-engine-spark-connector-mongodb-1.0.0-SNAPSHOT.jar'),
                                '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('23', '-1', 'profile.execute.engine', 'local', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('24', '-1', 'spark.engine.parameter.deploy.mode', 'cluster', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('25', '-1', 'spark.engine.parameter.num.executors', '1', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('26', '-1', 'spark.engine.parameter.driver.cores', '1', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('27', '-1', 'spark.engine.parameter.driver.memory', '512M', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('28', '-1', 'spark.engine.parameter.executor.cores', '1', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('29', '-1', 'spark.engine.parameter.executor.memory', '512M', '1', '1', '2023-09-05 21:02:38', '1', '2023-09-05 21:02:38');
INSERT INTO dv_config VALUES ('30', '-1', 'datavines.fqdn', 'http://127.0.0.1:5600', '1', '1', '2024-05-21 15:15:38', '1', '2024-05-21 15:15:38');
INSERT INTO dv_config VALUES ('31', '-1', 'data.quality.flink.jar.name', '/libs/datavines-engine-flink-core-1.0.0-SNAPSHOT.jar', '1', '1', '2025-02-02 11:43:04', '1', '2025-02-02 11:43:04');

INSERT INTO dv_user (id, username, password, email, phone, admin) VALUES ('1', 'admin', '$2a$10$9ZcicUYFl/.knBi9SE53U.Nml8bfNeArxr35HQshxXzimbA6Ipgqq', 'admin@gmail.com', NULL, '0');
INSERT INTO dv_workspace (id, name, create_by, update_by) VALUES ('1', 'admin''s default', '1', '1');
INSERT INTO dv_user_workspace (id, user_id, workspace_id, role_id,create_by,update_by) VALUES ('1', '1', '1', '1','1', '1');
