package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoAlarmStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 告警统计 Mapper
 */
@Mapper
public interface TopoAlarmStatsMapper {

    /**
     * 根据元素DN查询
     */
    TopoAlarmStats selectByElementDn(String elementDn);

    /**
     * 根据元素类型查询
     */
    List<TopoAlarmStats> selectByElementType(String elementType);

    /**
     * 插入或更新告警统计
     */
    int insertOrUpdate(TopoAlarmStats stats);

    /**
     * 更新告警统计
     */
    int update(TopoAlarmStats stats);

    /**
     * 增量更新告警统计（并发安全，MySQL 行锁保证原子性）
     * 传入的值为增量（delta），INSERT 时用绝对值，ON DUPLICATE KEY UPDATE 时用累加
     */
    int incrementAndUpdate(TopoAlarmStats stats);

    /**
     * 带乐观锁的全量更新，返回受影响行数（0 表示版本冲突）
     */
    int updateWithVersion(TopoAlarmStats stats);

    /**
     * 根据一组元素DN批量查询告警统计
     */
    List<TopoAlarmStats> selectByElementDnList(@Param("list") List<String> elementDnList);

    /**
     * 删除告警统计
     */
    int deleteByElementDn(String elementDn);
}
