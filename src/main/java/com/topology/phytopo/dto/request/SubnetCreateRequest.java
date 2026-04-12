package com.topology.phytopo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 子网创建请求
 */
@Data
public class SubnetCreateRequest {

    @NotBlank(message = "子网名称不能为空")
    @Size(max = 256, message = "子网名称长度不能超过256")
    private String name;

    @Size(max = 256, message = "显示名称长度不能超过256")
    private String displayName;

    /** 父节点DN */
    private String parentDn;

    @Size(max = 256, message = "地址长度不能超过256")
    private String address;

    @Size(max = 255, message = "位置长度不能超过255")
    private String location;

    @Size(max = 64, message = "维护人长度不能超过64")
    private String maintainer;

    @Size(max = 128, message = "联系方式长度不能超过128")
    private String contact;
}
