
DROP TABLE "DB"."INTLIB"."EXEC_DATAUNIT_INFO";
DROP TABLE "DB"."INTLIB"."EXEC_CONTEXT_DPU";
DROP TABLE "DB"."INTLIB"."EXEC_RECORD";
DROP TABLE "DB"."INTLIB"."EXEC_PIPELINE";
DROP TABLE "DB"."INTLIB"."EXEC_CONTEXT_PIPELINE";
DROP TABLE "DB"."INTLIB"."EXEC_SCHEDULE_AFTER";
DROP TABLE "DB"."INTLIB"."PPL_EDGE";
DROP TABLE "DB"."INTLIB"."PPL_NODE";
DROP TABLE "DB"."INTLIB"."PPL_GRAPH";
DROP TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION_EMAIL";
DROP TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION";
DROP TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION_EMAIL";
DROP TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION";
DROP TABLE "DB"."INTLIB"."EXEC_SCHEDULE";
DROP TABLE "DB"."INTLIB"."PPL_MODEL";
DROP TABLE "DB"."INTLIB"."DPU_INSTANCE";
DROP TABLE "DB"."INTLIB"."DPU_TEMPLATE";
DROP TABLE "DB"."INTLIB"."PPL_POSITION";
DROP TABLE "DB"."INTLIB"."USR_USER_ROLE";
DROP TABLE "DB"."INTLIB"."USR_USER";
DROP TABLE "DB"."INTLIB"."SCH_EMAIL";


CREATE TABLE "DB"."INTLIB"."DPU_INSTANCE"
(
-- DPURecord
  "id" INTEGER IDENTITY,
  "name" VARCHAR(45),
  "description" VARCHAR(255),
  "type" SMALLINT,
  "jar_path" VARCHAR(255),
  "configuration" LONG VARBINARY,
-- DPUInstaceRecord
  "dpu_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."DPU_TEMPLATE"
(
-- DPURecord
  "id" INTEGER IDENTITY,
  "name" VARCHAR(45),
  "description" VARCHAR(255),
  "type" SMALLINT,
  "jar_path" VARCHAR(255),
  "configuration" LONG VARBINARY,
  "parent_id" INTEGER,
-- DPUTemplateRecord
  "visibility" SMALLINT,
  "jar_description" VARCHAR(512),
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_DATAUNIT_INFO"
(
  "id" INTEGER IDENTITY,
  "name" VARCHAR(45),
  "idx" INTEGER,
  "type" SMALLINT,
  "is_input" SMALLINT,
  "exec_context_dpu_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_CONTEXT_PIPELINE"
(
  "id" INTEGER IDENTITY,
  "directory" VARCHAR(255),
  "dummy" SMALLINT, -- remove if table contains a column without default value
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_CONTEXT_DPU"
(
  "id" INTEGER IDENTITY,
  "exec_context_pipeline_id" INTEGER,
  "dpu_instance_id" INTEGER,
  "state" SMALLINT,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_RECORD"
(
  "id" INTEGER IDENTITY,
  "r_time" DATETIME,
  "r_type" SMALLINT,
  "dpu_id" INTEGER,
  "execution_id" INTEGER,
  "short_message" LONG VARCHAR,
  "full_message" LONG VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_PIPELINE"
(
  "id" INTEGER IDENTITY,
  "status" INTEGER,
  "pipeline_id" INTEGER,
  "debug_mode" SMALLINT,
  "t_start" DATETIME,
  "t_end" DATETIME,
  "context_id" INTEGER NOT NULL,
  "schedule_id" INTEGER,
  "silent_mode" SMALLINT,
  "debugnode_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_SCHEDULE"
(
  "id" INTEGER IDENTITY,
  "name" VARCHAR(45),
  "description" VARCHAR(255),
  "pipeline_id" INTEGER NOT NULL,
  "user_id" INTEGER, -- TODO set NOT NULL when users are implemented in frontend
  "just_once" SMALLINT,
  "enabled" SMALLINT,
  "type" SMALLINT,
  "first_exec" DATETIME,
  "last_exec" DATETIME,
  "time_period" INTEGER,
  "period_unit" SMALLINT,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."EXEC_SCHEDULE_AFTER"
(
  "schedule_id" INTEGER,
  "pipeline_id" INTEGER,
  PRIMARY KEY ("schedule_id", "pipeline_id")
);

CREATE TABLE "DB"."INTLIB"."PPL_MODEL"
(
  "id" INTEGER IDENTITY,
  "name" VARCHAR(45),
  "description" VARCHAR(255),
  "user_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."PPL_EDGE"
(
  "id" INTEGER IDENTITY,
  "graph_id" INTEGER,
  "node_from_id" INTEGER,
  "node_to_id" INTEGER,
  "data_unit_name" VARCHAR(45),
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."PPL_NODE"
(
  "id" INTEGER IDENTITY,
  "graph_id" INTEGER,
  "instance_id" INTEGER,
  "position_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."PPL_GRAPH"
(
  "id" INTEGER IDENTITY,
  "pipeline_id" INTEGER,
  PRIMARY KEY ("id"),
  UNIQUE (pipeline_id)
);

CREATE TABLE "DB"."INTLIB"."PPL_POSITION"
(
  "id" INTEGER IDENTITY,
  "pos_x" INTEGER,
  "pos_y" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION"
(
  "id" INTEGER IDENTITY,
  "schedule_id" INTEGER NOT NULL,
  "type_success" SMALLINT,
  "type_error" SMALLINT,
  PRIMARY KEY ("id"),
  UNIQUE (schedule_id)
);

CREATE TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION"
(
  "id" INTEGER IDENTITY,
  "user_id" INTEGER NOT NULL,
  "type_success" SMALLINT,
  "type_error" SMALLINT,
  PRIMARY KEY ("id"),
  UNIQUE (user_id)
);

CREATE TABLE "DB"."INTLIB"."SCH_EMAIL"
(
  "id" INTEGER IDENTITY,
  "e_user" VARCHAR(85),
  "e_domain" VARCHAR(45),
  PRIMARY KEY ("id")
);

CREATE TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION_EMAIL"
(
  "notification_id" INTEGER IDENTITY,
  "email_id" SMALLINT,
  PRIMARY KEY ("notification_id", "email_id")
);

CREATE TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION_EMAIL"
(
  "notification_id" INTEGER IDENTITY,
  "email_id" SMALLINT,
  PRIMARY KEY ("notification_id", "email_id")
);

CREATE TABLE "DB"."INTLIB"."USR_USER"
(
  "id" INTEGER IDENTITY,
  "username" VARCHAR(25) NOT NULL,
  "email_id" INTEGER,
  "u_password" CHAR(132) NOT NULL,
  "full_name" VARCHAR(55),
  PRIMARY KEY ("id"),
  UNIQUE ("username")
);

CREATE TABLE "DB"."INTLIB"."USR_USER_ROLE"
(
  "user_id" INTEGER NOT NULL,
  "role_id" INTEGER NOT NULL,
  PRIMARY KEY ("user_id", "role_id")
);

-- CONSTRAINTS #################################################################


-- Table "DB"."INTLIB"."DPU_INSTANCE"
ALTER TABLE "DB"."INTLIB"."DPU_INSTANCE"
  ADD CONSTRAINT "DPU_INSTANCE_DPU_TEMPLATE_id_id" FOREIGN KEY ("dpu_id")
    REFERENCES "DB"."INTLIB"."DPU_TEMPLATE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."DPU_TEMPLATE"
ALTER TABLE "DB"."INTLIB"."DPU_TEMPLATE"
  ADD CONSTRAINT "DPU_TEMPLATE_DPU_TEMPLATE_id_id" FOREIGN KEY ("parent_id")
    REFERENCES "DB"."INTLIB"."DPU_TEMPLATE" ("id")
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table "DB"."INTLIB"."EXEC_DATAUNIT_INFO"
ALTER TABLE "DB"."INTLIB"."EXEC_DATAUNIT_INFO"
  ADD CONSTRAINT "EXEC_DATAUNIT_INFO_EXEC_CONTEXT_DPU_id_id" FOREIGN KEY ("exec_context_dpu_id")
    REFERENCES "DB"."INTLIB"."EXEC_CONTEXT_DPU" ("id")
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table "DB"."INTLIB"."EXEC_CONTEXT_DPU"
ALTER TABLE "DB"."INTLIB"."EXEC_CONTEXT_DPU"
  ADD CONSTRAINT "EXEC_CONTEXT_DPU_EXEC_CONTEXT_PIPELINE_id_id" FOREIGN KEY ("exec_context_pipeline_id")
    REFERENCES "DB"."INTLIB"."EXEC_CONTEXT_PIPELINE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."EXEC_CONTEXT_DPU"
  ADD CONSTRAINT "EXEC_CONTEXT_DPU_DPU_INSTANCE_id_id" FOREIGN KEY ("dpu_instance_id")
    REFERENCES "DB"."INTLIB"."DPU_INSTANCE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."EXEC_RECORD"
ALTER TABLE "DB"."INTLIB"."EXEC_RECORD"
  ADD CONSTRAINT "EXEC_RECORD_DPU_INSTANCE_id_id" FOREIGN KEY ("dpu_id")
    REFERENCES "DB"."INTLIB"."DPU_INSTANCE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."EXEC_RECORD"
  ADD CONSTRAINT "EXEC_RECORD_EXEC_PIPELINE_id_id" FOREIGN KEY ("execution_id")
    REFERENCES "DB"."INTLIB"."EXEC_PIPELINE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."EXEC_PIPELINE"
ALTER TABLE "DB"."INTLIB"."EXEC_PIPELINE"
  ADD CONSTRAINT "EXEC_PIPELINE_PPL_MODEL_id_id" FOREIGN KEY ("pipeline_id")
    REFERENCES "DB"."INTLIB"."PPL_MODEL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."EXEC_PIPELINE"
  ADD CONSTRAINT "EXEC_PIPELINE_EXEC_CONTEXT_PIPELINE_id_id" FOREIGN KEY ("context_id")
    REFERENCES "DB"."INTLIB"."EXEC_CONTEXT_PIPELINE" ("id")
	ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE "DB"."INTLIB"."EXEC_PIPELINE"
  ADD CONSTRAINT "EXEC_PIPELINE_EXEC_SCHEDULE_id_id" FOREIGN KEY ("schedule_id")
    REFERENCES "DB"."INTLIB"."EXEC_SCHEDULE" ("id")
	ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE "DB"."INTLIB"."EXEC_PIPELINE"
  ADD CONSTRAINT "EXEC_PIPELINE_PPL_NODE_id_id" FOREIGN KEY ("debugnode_id")
    REFERENCES "DB"."INTLIB"."PPL_NODE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

-- Table "DB"."INTLIB"."EXEC_SCHEDULE"
ALTER TABLE "DB"."INTLIB"."EXEC_SCHEDULE"
  ADD CONSTRAINT "EXEC_SCHEDULE_PPL_MODEL_id_id" FOREIGN KEY ("pipeline_id")
    REFERENCES "DB"."INTLIB"."PPL_MODEL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."EXEC_SCHEDULE"
  ADD CONSTRAINT "EXEC_SCHEDULE_USR_USER_id_id" FOREIGN KEY ("user_id")
    REFERENCES "DB"."INTLIB"."USR_USER" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."EXEC_SCHEDULE_AFTER"
ALTER TABLE "DB"."INTLIB"."EXEC_SCHEDULE_AFTER"
  ADD CONSTRAINT "EXEC_SCHEDULE_AFTER_EXEC_SCHEDULE_id_id" FOREIGN KEY ("schedule_id")
    REFERENCES "DB"."INTLIB"."EXEC_SCHEDULE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."EXEC_SCHEDULE_AFTER"
  ADD CONSTRAINT "EXEC_SCHEDULE_AFTER_PPL_MODEL_id_id" FOREIGN KEY ("pipeline_id")
    REFERENCES "DB"."INTLIB"."PPL_MODEL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."PPL_MODEL"
ALTER TABLE "DB"."INTLIB"."PPL_MODEL"
  ADD CONSTRAINT "PPL_MODEL_USR_USER_id_id" FOREIGN KEY ("user_id")
    REFERENCES "DB"."INTLIB"."USR_USER" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."PPL_GRAPH"
ALTER TABLE "DB"."INTLIB"."PPL_GRAPH"
  ADD CONSTRAINT "PPL_GRAPH_PPL_MODEL_id_id" FOREIGN KEY ("pipeline_id")
    REFERENCES "DB"."INTLIB"."PPL_MODEL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."PPL_NODE"
ALTER TABLE "DB"."INTLIB"."PPL_NODE"
  ADD CONSTRAINT "PPL_NODE_PPL_GRAPH_id_id" FOREIGN KEY ("graph_id")
    REFERENCES "DB"."INTLIB"."PPL_GRAPH" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."PPL_NODE"
  ADD CONSTRAINT "PPL_NODE_DPU_INSTANCE_id_id" FOREIGN KEY ("instance_id")
    REFERENCES "DB"."INTLIB"."DPU_INSTANCE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."PPL_NODE"
  ADD CONSTRAINT "PPL_NODE_PPL_POSITION_id_id" FOREIGN KEY ("position_id")
    REFERENCES "DB"."INTLIB"."PPL_POSITION" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."PPL_EDGE"
ALTER TABLE "DB"."INTLIB"."PPL_EDGE"
  ADD CONSTRAINT "PPL_EDGE_PPL_GRAPH_id_id" FOREIGN KEY ("graph_id")
    REFERENCES "DB"."INTLIB"."PPL_GRAPH" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."PPL_EDGE"
  ADD CONSTRAINT "PPL_EDGE_PPL_NODE_FROM_id_id" FOREIGN KEY ("node_from_id")
    REFERENCES "DB"."INTLIB"."PPL_NODE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."PPL_EDGE"
  ADD CONSTRAINT "PPL_EDGE_PPL_NODE_TO_id_id" FOREIGN KEY ("node_to_id")
    REFERENCES "DB"."INTLIB"."PPL_NODE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."SCH_SCH_NOTIFICATION"
ALTER TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION"
  ADD CONSTRAINT "SCH_SCH_NOTIFICATION_EXEC_SCHEDULE_id_id" FOREIGN KEY ("schedule_id")
    REFERENCES "DB"."INTLIB"."EXEC_SCHEDULE" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."SCH_SCH_NOTIFICATION_EMAIL"
ALTER TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION_EMAIL"
  ADD CONSTRAINT "SCH_SCH_NOTIFICATION_EMAIL_SCH_SCH_NOTIFICATION_id_id" FOREIGN KEY ("notification_id")
    REFERENCES "DB"."INTLIB"."SCH_SCH_NOTIFICATION" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."SCH_SCH_NOTIFICATION_EMAIL"
  ADD CONSTRAINT "SCH_SCH_NOTIFICATION_EMAIL_SCH_EMAIL_id_id" FOREIGN KEY ("email_id")
    REFERENCES "DB"."INTLIB"."SCH_EMAIL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."SCH_USR_NOTIFICATION"
ALTER TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION"
  ADD CONSTRAINT "SCH_USR_NOTIFICATION_USR_USER_id_id" FOREIGN KEY ("user_id")
    REFERENCES "DB"."INTLIB"."USR_USER" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."SCH_USR_NOTIFICATION_EMAIL"
ALTER TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION_EMAIL"
  ADD CONSTRAINT "SCH_USR_NOTIFICATION_EMAIL_SCH_USR_NOTIFICATION_id_id" FOREIGN KEY ("notification_id")
    REFERENCES "DB"."INTLIB"."SCH_USR_NOTIFICATION" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "DB"."INTLIB"."SCH_USR_NOTIFICATION_EMAIL"
  ADD CONSTRAINT "SCH_USR_NOTIFICATION_EMAIL_SCH_EMAIL_id_id" FOREIGN KEY ("email_id")
    REFERENCES "DB"."INTLIB"."SCH_EMAIL" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table "DB"."INTLIB"."USR_USER"
ALTER TABLE "DB"."INTLIB"."USR_USER"
  ADD CONSTRAINT "USR_USER_SCH_EMAIL_id_id" FOREIGN KEY ("email_id")
    REFERENCES "DB"."INTLIB"."SCH_EMAIL" ("id")
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table "DB"."INTLIB"."USR_USER_ROLE"
ALTER TABLE "DB"."INTLIB"."USR_USER_ROLE"
  ADD CONSTRAINT "USR_USER_USR_USER_ROLE_id_id" FOREIGN KEY ("user_id")
    REFERENCES "DB"."INTLIB"."USR_USER" ("id")
	ON UPDATE CASCADE ON DELETE CASCADE;


-- workaround for bug in virtuoso's implementation of cascades on delete
-- see https://github.com/openlink/virtuoso-opensource/issues/56
CREATE TRIGGER delete_node_fix BEFORE DELETE ON "DB"."INTLIB"."PPL_NODE" REFERENCING old AS n
{
	DELETE FROM ppl_edge
	 WHERE node_from_id = n.id
	  OR node_to_id = n.id;
};

-- TABLES FOR LOGBACK

DROP TABLE "DB"."INTLIB"."LOGGING_EVENT_PROPERTY";
DROP TABLE "DB"."INTLIB"."LOGGING_EVENT_EXCEPTION";
DROP TABLE "DB"."INTLIB"."LOGGING_EVENT";

CREATE TABLE "DB"."INTLIB"."LOGGING_EVENT"
(
  timestmp BIGINT NOT NULL,
  formatted_message LONG VARCHAR NOT NULL,
  logger_name VARCHAR(254) NOT NULL,
  level_string VARCHAR(254) NOT NULL,
  thread_name VARCHAR(254),
  reference_flag SMALLINT,
  arg0 VARCHAR(254),
  arg1 VARCHAR(254),
  arg2 VARCHAR(254),
  arg3 VARCHAR(254),
  caller_filename VARCHAR(254) NOT NULL,
  caller_class VARCHAR(254) NOT NULL,
  caller_method VARCHAR(254) NOT NULL,
  caller_line CHAR(4) NOT NULL,
  event_id BIGINT NOT NULL IDENTITY,
  PRIMARY KEY (event_id)
);

CREATE TABLE "DB"."INTLIB"."LOGGING_EVENT_PROPERTY"
(
  event_id BIGINT NOT NULL,
  mapped_key VARCHAR(254) NOT NULL,
  mapped_value VARCHAR(254),
  PRIMARY KEY (event_id, mapped_key),
  FOREIGN KEY (event_id) REFERENCES "DB"."INTLIB"."LOGGING_EVENT"(event_id)
);

CREATE TABLE "DB"."INTLIB"."LOGGING_EVENT_EXCEPTION"
(
  event_id BIGINT NOT NULL,
  i SMALLINT NOT NULL,
  trace_line VARCHAR(254) NOT NULL,
  PRIMARY KEY(event_id, i),
  FOREIGN KEY (event_id) REFERENCES "DB"."INTLIB"."LOGGING_EVENT"(event_id)
);

