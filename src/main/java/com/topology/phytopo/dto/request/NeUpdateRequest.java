package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 网元更新请求
 */
@Data
public class NeUpdateRequest {

    /** 网元名称 */
    @Size(max = 100, message = "名称最多100个字符")
    private String name;

    /** 显示名称 */
    private String displayName;

    /** IP地址 */
    private String address;

    /** 物理位置 */
    private String location;

    /** 维护人 */
    private String maintainer;

    /** 联系方式 */
    private String contact;
}
