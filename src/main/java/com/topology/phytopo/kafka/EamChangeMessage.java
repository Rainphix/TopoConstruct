package com.topology.phytopo.kafka;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * EAM 变更消息
 */
@Data
public class EamChangeMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 变更类型: CREATE/UPDATE/DELETE */
    private String changeType;

    /** 对象DN */
    private String dn;

    /** 对象类型: SUBNET/NE */
    private String objectType;

    /** 名称 */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 父节点DN */
    private String parentDn;

    /** 地址 */
    private String address;

    /** 位置 */
    private String location;

    /** 维护人 */
    private String maintainer;

    /** 联系方式 */
    private String contact;

    /** 对象数据(JSON) */
    private String objectData;

    /** 时间戳 */
    private Long timestamp;
}
