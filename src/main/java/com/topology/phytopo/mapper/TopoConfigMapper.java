package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 配置 Mapper
 */
@Mapper
public interface TopoConfigMapper {

    /**
     * 根据配置键查询
     */
    TopoConfig selectByKey(String configKey);

    /**
     * 查询所有配置
     */
    List<TopoConfig> selectAll();

    /**
     * 插入配置
     */
    int insert(TopoConfig config);

    /**
     * 更新配置
     */
    int update(TopoConfig config);

    /**
     * 删除配置
     */
    int deleteByKey(String configKey);
}
