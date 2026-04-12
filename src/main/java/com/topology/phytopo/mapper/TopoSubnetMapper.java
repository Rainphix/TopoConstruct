package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoSubnet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 子网 Mapper
 */
@Mapper
public interface TopoSubnetMapper {

    /**
     * 根据DN查询子网
     */
    TopoSubnet selectByDn(String dn);

    /**
     * 根据父DN查询子网列表
     */
    List<TopoSubnet> selectByParentDn(String parentDn);

    /**
     * 查询所有子网
     */
    List<TopoSubnet> selectAll();

    /**
     * 插入子网
     */
    int insert(TopoSubnet subnet);

    /**
     * 批量插入子网
     */
    int batchInsert(List<TopoSubnet> list);

    /**
     * 更新子网
     */
    int update(TopoSubnet subnet);

    /**
     * 删除子网
     */
    int deleteByDn(String dn);

    /**
     * 检查子网是否存在
     */
    boolean existsByDn(String dn);

    /**
     * 查询子网下的设备数量
     */
    int countNeByParentDn(String parentDn);

    /**
     * 查询子网下的子网数量
     */
    int countSubnetByParentDn(String parentDn);

    /**
     * 查询合并组子网
     */
    List<TopoSubnet> selectMergeGroups();

    /**
     * 查询指定父子网下的合并组
     */
    List<TopoSubnet> selectMergeGroupsByParent(String parentDn);

    /**
     * 更新子网的父节点和层级
     */
    int updateParentDn(@Param("dn") String dn, @Param("parentDn") String parentDn,
                       @Param("layer") int layer, @Param("updatedTime") long updatedTime);
}
