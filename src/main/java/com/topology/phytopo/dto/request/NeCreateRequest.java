package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 网元创建请求
 */
@Data
public class NeCreateRequest {

    /** 网元名称 */
    @NotBlank(message = "网元名称不能为空")
    @Size(max = 100, message = "名称最多100个字符")
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 网元类型 */
    @NotBlank(message = "网元类型不能为空")
    private String neType;

    /** 所属子网DN */
    @NotBlank(message = "所属子网DN不能为空")
    private String parentDn;

    /** IP地址 */
    private String address;

    /** 物理位置 */
    private String location;

    /** 维护人 */
    private String maintainer;

    /** 联系方式 */
    private String contact;
}
