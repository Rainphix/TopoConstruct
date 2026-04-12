package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoNe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 网元 Mapper
 */
@Mapper
public interface TopoNeMapper {

    /**
     * 根据DN查询网元
     */
    TopoNe selectByDn(String dn);

    /**
     * 根据父DN查询网元列表
     */
    List<TopoNe> selectByParentDn(String parentDn);

    /**
     * 根据根子网DN查询网元列表
     */
    List<TopoNe> selectByRootSubnetDn(String rootSubnetDn);

    /**
     * 按类型和父DN统计
     */
    List<TopoNe> selectByNeTypeAndParentDn(String neType, String parentDn);

    /**
     * 查询所有网元
     */
    List<TopoNe> selectAll();

    /**
     * 插入网元
     */
    int insert(TopoNe ne);

    /**
     * 批量插入网元
     */
    int batchInsert(List<TopoNe> list);

    /**
     * 更新网元
     */
    int update(TopoNe ne);

    /**
     * 删除网元
     */
    int deleteByDn(String dn);

    /**
     * 批量删除网元
     */
    int batchDeleteByDn(List<String> dnList);

    /**
     * 检查网元是否存在
     */
    boolean existsByDn(String dn);

    /**
     * 按类型统计数量
     */
    int countByNeType(String neType);

    /**
     * 批量更新父节点
     */
    int batchUpdateParentDn(@Param("dnList") List<String> dnList,
                            @Param("newParentDn") String newParentDn,
                            @Param("updatedTime") long updatedTime);

    /**
     * 更新单个网元的父节点和根子网
     */
    int updateParentDn(@Param("dn") String dn, @Param("newParentDn") String newParentDn,
                       @Param("rootSubnetDn") String rootSubnetDn,
                       @Param("updatedTime") long updatedTime);
}
