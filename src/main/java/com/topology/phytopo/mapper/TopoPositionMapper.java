package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoPosition;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 坐标 Mapper
 */
@Mapper
public interface TopoPositionMapper {

    /**
     * 根据子网DN和元素DN查询坐标
     */
    TopoPosition selectBySubnetAndElement(String subnetDn, String elementDn);

    /**
     * 查询子网下所有坐标
     */
    List<TopoPosition> selectBySubnetDn(String subnetDn);

        /**
     * 插入坐标
         */
        void insert(TopoPosition position);

        /**
         * 更新坐标
         */
        void update(TopoPosition position);

        /**
         * 批量插入或更新坐标
         */
        int batchInsertOrUpdate(List<TopoPosition> list);

        /**
         * 删除坐标
         */
        int deleteBySubnetAndElement(String subnetDn, String elementDn);

    /**
     * 删除子网下所有坐标
     */
    int deleteBySubnetDn(String subnetDn);
}
