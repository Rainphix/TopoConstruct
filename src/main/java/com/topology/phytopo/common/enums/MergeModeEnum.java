package com.topology.phytopo.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 合并模式枚举
 */
@Getter
@AllArgsConstructor
public enum MergeModeEnum {

    THRESHOLD("THRESHOLD", "超过阈值合并"),
    ALL("ALL", "全部合并");

    private final String code;
    private final String desc;

    public static MergeModeEnum fromCode(String code) {
        for (MergeModeEnum mode : values()) {
            if (mode.getCode().equalsIgnoreCase(code)) {
                return mode;
            }
        }
        return THRESHOLD;
    }
}
