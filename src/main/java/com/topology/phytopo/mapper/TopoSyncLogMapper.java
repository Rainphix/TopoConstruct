package com.topology.phytopo.mapper;

import com.topology.phytopo.entity.TopoSyncLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 同步日志 Mapper
 */
@Mapper
public interface TopoSyncLogMapper {

    /**
     * 插入日志
     */
    int insert(TopoSyncLog log);

    /**
     * 更新日志
     */
    int updateLog(TopoSyncLog log);

    /**
     * 查询最近的同步日志
     */
    TopoSyncLog selectLatest();

    /**
     * 根据状态查询
     */
    List<TopoSyncLog> selectByStatus(String status);
}
