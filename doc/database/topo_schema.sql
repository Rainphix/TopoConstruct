-- ============================================================================
-- 拓扑服务数据库设计
-- 数据库: MySQL 8.0+
-- 版本: 1.1
-- 日期: 2026-04-11
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. 拓扑子网表 (T_TOPO_SUBNET)
-- 用途: 存储子网基本信息，与EAM数据同步
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_SUBNET;
CREATE TABLE T_TOPO_SUBNET (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    DN                VARCHAR(255) NOT NULL              COMMENT '唯一标识(来自EAM)',
    NAME              VARCHAR(256) NOT NULL              COMMENT '子网名称',
    DISPLAY_NAME      VARCHAR(256)                       COMMENT '显示名称',
    PARENT_DN         VARCHAR(255)                       COMMENT '父节点DN(子网或网元)',
    PARENT_TYPE       VARCHAR(32) DEFAULT 'SUBNET'       COMMENT '父节点类型: SUBNET/NE',
    LAYER             INT DEFAULT 0                      COMMENT '层级深度',
    IS_MERGE_GROUP    TINYINT(1) DEFAULT 0               COMMENT '是否为同类型合并组',
    MERGE_TYPE        VARCHAR(64)                        COMMENT '合并的设备类型(当IS_MERGE_GROUP=1时)',
    MED_NODE          VARCHAR(64)                        COMMENT 'MED节点标识',
    ADDRESS           VARCHAR(256)                       COMMENT '地址',
    LOCATION          VARCHAR(255)                       COMMENT '位置',
    MAINTAINER        VARCHAR(64)                        COMMENT '维护人',
    CONTACT           VARCHAR(128)                       COMMENT '联系方式',
    ALARM_STATUS      INT DEFAULT 0                      COMMENT '告警状态',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    SYNC_TIME         BIGINT                             COMMENT '同步时间(来自EAM)',
    VERSION           VARCHAR(256)                       COMMENT '版本号',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_SUBNET_DN (DN),
    KEY IDX_TOPO_SUBNET_PARENT (PARENT_DN),
    KEY IDX_TOPO_SUBNET_LAYER (LAYER),
    KEY IDX_TOPO_SUBNET_MERGE (IS_MERGE_GROUP, MERGE_TYPE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑子网表';

-- ----------------------------------------------------------------------------
-- 2. 拓扑网元表 (T_TOPO_NE)
-- 用途: 存储网元(网络设备)基本信息，与EAM数据同步
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_NE;
CREATE TABLE T_TOPO_NE (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    DN                VARCHAR(255) NOT NULL              COMMENT '唯一标识(来自EAM)',
    NAME              VARCHAR(256) NOT NULL              COMMENT '网元名称',
    DISPLAY_NAME      VARCHAR(256)                       COMMENT '显示名称',
    NE_TYPE           VARCHAR(64) NOT NULL               COMMENT '网元类型: FIREWALL/SWITCH/SERVER/STORAGE/GATEWAY等',
    PARENT_DN         VARCHAR(255)                       COMMENT '父节点DN',
    PARENT_TYPE       VARCHAR(32) DEFAULT 'SUBNET'       COMMENT '父节点类型: SUBNET/NE',
    ROOT_SUBNET_DN    VARCHAR(255)                       COMMENT '所属根子网DN',
    MED_NODE          VARCHAR(64)                        COMMENT 'MED节点标识',
    ADDRESS           VARCHAR(256)                       COMMENT 'IP地址',
    LOCATION          VARCHAR(255)                       COMMENT '物理位置',
    MAINTAINER        VARCHAR(64)                        COMMENT '维护人',
    CONTACT           VARCHAR(128)                       COMMENT '联系方式',
    STATUS            INT DEFAULT 1                      COMMENT '状态: 0-离线 1-在线',
    ALARM_STATUS      INT DEFAULT 0                      COMMENT '告警状态',
    SEQUENCE_NO       INT                                COMMENT '序列号',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    SYNC_TIME         BIGINT                             COMMENT '同步时间(来自EAM)',
    VERSION           VARCHAR(256)                       COMMENT '版本号',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_NE_DN (DN),
    KEY IDX_TOPO_NE_PARENT (PARENT_DN),
    KEY IDX_TOPO_NE_TYPE (NE_TYPE),
    KEY IDX_TOPO_NE_ROOT (ROOT_SUBNET_DN),
    KEY IDX_TOPO_NE_STATUS (STATUS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑网元表';

-- ----------------------------------------------------------------------------
-- 3. 拓扑坐标表 (T_TOPO_POSITION)
-- 用途: 存储子网/网元在拓扑图中的坐标位置
-- 说明: 每个编辑过的子网维护一套坐标
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_POSITION;
CREATE TABLE T_TOPO_POSITION (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    SUBNET_DN         VARCHAR(255) NOT NULL              COMMENT '坐标所属的子网视图',
    ELEMENT_DN        VARCHAR(255) NOT NULL              COMMENT '坐标对应的元素DN',
    ELEMENT_TYPE      VARCHAR(32) NOT NULL               COMMENT '元素类型: SUBNET/NE',
    POS_X             INT DEFAULT 0                      COMMENT 'X坐标',
    POS_Y             INT DEFAULT 0                      COMMENT 'Y坐标',
    WIDTH             INT                                COMMENT '宽度(可选)',
    HEIGHT            INT                                COMMENT '高度(可选)',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_POSITION (SUBNET_DN, ELEMENT_DN),
    KEY IDX_TOPO_POSITION_SUBNET (SUBNET_DN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑坐标表-存储元素在各子网视图中的坐标';

-- ----------------------------------------------------------------------------
-- 4. 拓扑合并组表 (T_TOPO_MERGE_GROUP)
-- 用途: 记录同类型合并组信息
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_MERGE_GROUP;
CREATE TABLE T_TOPO_MERGE_GROUP (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    SUBNET_DN         VARCHAR(255) NOT NULL              COMMENT '合并组子网DN',
    PARENT_DN         VARCHAR(255) NOT NULL              COMMENT '父子网DN',
    NE_TYPE           VARCHAR(64) NOT NULL               COMMENT '合并的网元类型',
    GROUP_INDEX       INT DEFAULT 1                      COMMENT '组编号(同类型可能有多个组)',
    MEMBER_COUNT      INT DEFAULT 0                      COMMENT '成员数量',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_MERGE_GROUP (PARENT_DN, NE_TYPE, GROUP_INDEX),
    KEY IDX_TOPO_MERGE_SUBNET (SUBNET_DN),
    KEY IDX_TOPO_MERGE_PARENT (PARENT_DN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑合并组表-记录同类型合并的分组信息';

-- ----------------------------------------------------------------------------
-- 5. 拓扑配置表 (T_TOPO_CONFIG)
-- 用途: 存储拓扑服务全局配置
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_CONFIG;
CREATE TABLE T_TOPO_CONFIG (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    CONFIG_KEY        VARCHAR(128) NOT NULL              COMMENT '配置键',
    CONFIG_VALUE      VARCHAR(512)                       COMMENT '配置值',
    CONFIG_DESC       VARCHAR(256)                       COMMENT '配置描述',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_CONFIG (CONFIG_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑配置表';

-- 初始化默认配置
INSERT INTO T_TOPO_CONFIG (CONFIG_KEY, CONFIG_VALUE, CONFIG_DESC, CREATED_TIME, UPDATED_TIME) VALUES
('MERGE_ENABLED', 'false', '是否开启同类型合并', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('MERGE_THRESHOLD', '20', '同类型合并阈值(超过此数量才合并)', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('MERGE_MODE', 'THRESHOLD', '合并模式: THRESHOLD-超过阈值合并, ALL-全部合并', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- ----------------------------------------------------------------------------
-- 6. 拓扑告警统计表 (T_TOPO_ALARM_STATS)
-- 用途: 缓存告警统计数据,避免频繁查询
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_ALARM_STATS;
CREATE TABLE T_TOPO_ALARM_STATS (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    ELEMENT_DN        VARCHAR(255) NOT NULL              COMMENT '元素DN(子网或网元)',
    ELEMENT_TYPE      VARCHAR(32) NOT NULL               COMMENT '元素类型: SUBNET/NE',
    CRITICAL_COUNT    INT DEFAULT 0                      COMMENT '严重告警数',
    MAJOR_COUNT       INT DEFAULT 0                      COMMENT '主要告警数',
    MINOR_COUNT       INT DEFAULT 0                      COMMENT '次要告警数',
    WARNING_COUNT     INT DEFAULT 0                      COMMENT '警告告警数',
    CLEARED_COUNT     INT DEFAULT 0                      COMMENT '已清除告警数',
    TOTAL_COUNT       INT DEFAULT 0                      COMMENT '总告警数',
    UPDATED_TIME      BIGINT NOT NULL                    COMMENT '更新时间戳',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_ALARM_STATS (ELEMENT_DN),
    KEY IDX_TOPO_ALARM_TYPE (ELEMENT_TYPE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑告警统计表-缓存告警统计数据';

-- ----------------------------------------------------------------------------
-- 7. 拓扑同步记录表 (T_TOPO_SYNC_LOG)
-- 用途: 记录与EAM的数据同步日志
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_SYNC_LOG;
CREATE TABLE T_TOPO_SYNC_LOG (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    SYNC_TYPE         VARCHAR(32) NOT NULL               COMMENT '同步类型: FULL/INCREMENTAL',
    SYNC_SOURCE       VARCHAR(32) DEFAULT 'EAM'          COMMENT '同步源',
    STATUS            VARCHAR(32) NOT NULL               COMMENT '状态: RUNNING/SUCCESS/FAILED',
    TOTAL_COUNT       INT DEFAULT 0                      COMMENT '总处理数量',
    SUCCESS_COUNT     INT DEFAULT 0                      COMMENT '成功数量',
    FAILED_COUNT      INT DEFAULT 0                      COMMENT '失败数量',
    ERROR_MSG         TEXT                               COMMENT '错误信息',
    START_TIME        BIGINT NOT NULL                    COMMENT '开始时间戳',
    END_TIME          BIGINT                             COMMENT '结束时间戳',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    PRIMARY KEY (ID),
    KEY IDX_TOPO_SYNC_STATUS (STATUS),
    KEY IDX_TOPO_SYNC_TIME (START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑同步记录表-记录与EAM的数据同步日志';

-- ----------------------------------------------------------------------------
-- 8. 网元类型枚举表 (T_TOPO_NE_TYPE)
-- 用途: 定义支持的网元类型及图标映射
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS T_TOPO_NE_TYPE;
CREATE TABLE T_TOPO_NE_TYPE (
    ID                BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    TYPE_CODE         VARCHAR(64) NOT NULL               COMMENT '类型编码',
    TYPE_NAME         VARCHAR(128) NOT NULL              COMMENT '类型名称',
    ICON_NAME         VARCHAR(64)                        COMMENT '图标名称',
    ICON_URL          VARCHAR(256)                       COMMENT '图标URL',
    SORT_ORDER        INT DEFAULT 0                      COMMENT '排序',
    IS_ACTIVE         TINYINT(1) DEFAULT 1               COMMENT '是否启用',
    CREATED_TIME      BIGINT NOT NULL                    COMMENT '创建时间戳',
    PRIMARY KEY (ID),
    UNIQUE KEY UK_TOPO_NE_TYPE (TYPE_CODE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网元类型枚举表-定义支持的网元类型及图标';

-- 初始化网元类型
INSERT INTO T_TOPO_NE_TYPE (TYPE_CODE, TYPE_NAME, ICON_NAME, SORT_ORDER, CREATED_TIME) VALUES
('SUBNET', '子网', 'subnet', 0, UNIX_TIMESTAMP() * 1000),
('FIREWALL', '防火墙', 'firewall', 1, UNIX_TIMESTAMP() * 1000),
('SWITCH', '交换机', 'switch', 2, UNIX_TIMESTAMP() * 1000),
('SERVER', '服务器', 'server', 3, UNIX_TIMESTAMP() * 1000),
('STORAGE', '存储设备', 'storage', 4, UNIX_TIMESTAMP() * 1000),
('GATEWAY', '网关', 'gateway', 5, UNIX_TIMESTAMP() * 1000),
('CHASSIS', '机框', 'chassis', 6, UNIX_TIMESTAMP() * 1000),
('RACK', '机架', 'rack', 7, UNIX_TIMESTAMP() * 1000),
('DEFAULT', '通用设备', 'default', 99, UNIX_TIMESTAMP() * 1000);

-- ----------------------------------------------------------------------------
-- 9. 子网统计视图 (V_TOPO_SUBNET_STATS)
-- 用途: 子网统计信息视图
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW V_TOPO_SUBNET_STATS AS
SELECT
    s.ID,
    s.DN,
    s.NAME,
    s.PARENT_DN,
    s.IS_MERGE_GROUP,
    s.MERGE_TYPE,
    (SELECT COUNT(*) FROM T_TOPO_NE n WHERE n.PARENT_DN = s.DN) AS NE_COUNT,
    (SELECT COUNT(*) FROM T_TOPO_SUBNET sub WHERE sub.PARENT_DN = s.DN) AS SUBNET_COUNT,
    (SELECT COUNT(*) FROM T_TOPO_NE n WHERE n.PARENT_DN = s.DN AND n.STATUS = 0) AS OFFLINE_COUNT,
    COALESCE(a.CRITICAL_COUNT, 0) AS CRITICAL_COUNT,
    COALESCE(a.MAJOR_COUNT, 0) AS MAJOR_COUNT,
    COALESCE(a.MINOR_COUNT, 0) AS MINOR_COUNT,
    COALESCE(a.WARNING_COUNT, 0) AS WARNING_COUNT
FROM T_TOPO_SUBNET s
LEFT JOIN T_TOPO_ALARM_STATS a ON a.ELEMENT_DN = s.DN AND a.ELEMENT_TYPE = 'SUBNET';

-- ----------------------------------------------------------------------------
-- 10. 网元类型统计视图 (V_TOPO_TYPE_STATS)
-- 用途: 按网元类型统计
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW V_TOPO_TYPE_STATS AS
SELECT
    NE_TYPE,
    COUNT(*) AS TOTAL_COUNT,
    SUM(CASE WHEN STATUS = 1 THEN 1 ELSE 0 END) AS ONLINE_COUNT,
    SUM(CASE WHEN STATUS = 0 THEN 1 ELSE 0 END) AS OFFLINE_COUNT,
    PARENT_DN
FROM T_TOPO_NE
GROUP BY NE_TYPE, PARENT_DN;

SET FOREIGN_KEY_CHECKS = 1;
