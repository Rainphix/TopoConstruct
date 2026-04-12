package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoMergeGroup;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 合并组 Mapper
 */
@Mapper
public interface TopoMergeGroupMapper {

    /**
     * 根据子网DN查询合并组
     */
    TopoMergeGroup selectBySubnetDn(String subnetDn);

    /**
     * 根据父DN和类型查询合并组
     */
    List<TopoMergeGroup> selectByParentAndNeType(String parentDn, String neType);

    /**
     * 插入合并组
     */
    int insert(TopoMergeGroup group);

    /**
     * 更新合并组
     */
    int update(TopoMergeGroup group);

    /**
     * 删除合并组
     */
    int deleteBySubnetDn(String subnetDn);

    /**
     * 查询所有合并组
     */
    List<TopoMergeGroup> selectAll();
}
