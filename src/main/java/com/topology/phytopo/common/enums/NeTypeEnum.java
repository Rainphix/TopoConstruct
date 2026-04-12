package com.topology.phytopo.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 网元类型枚举
 */
@Getter
@AllArgsConstructor
public enum NeTypeEnum {

    SUBNET("SUBNET", "子网", "subnet"),
    FIREWALL("FIREWALL", "防火墙", "firewall"),
    SWITCH("SWITCH", "交换机", "switch"),
    SERVER("SERVER", "服务器", "server"),
    STORAGE("STORAGE", "存储设备", "storage"),
    GATEWAY("GATEWAY", "网关", "gateway"),
    CHASSIS("CHASSIS", "机框", "chassis"),
    RACK("RACK", "机架", "rack"),
    DEFAULT("DEFAULT", "通用设备", "default");

    private final String code;
    private final String name;
    private final String icon;

    public static NeTypeEnum fromCode(String code) {
        for (NeTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
