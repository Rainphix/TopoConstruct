package com.topology.phytopo.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 元素类型枚举
 */
@Getter
@AllArgsConstructor
public enum ElementType {

    SUBNET("SUBNET", "子网"),
    NE("NE", "网元");

    private final String code;
    private final String desc;

    public static ElementType fromCode(String code) {
        for (ElementType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
