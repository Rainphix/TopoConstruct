package com.topology.phytopo.service;

import com.topology.phytopo.dto.request.MergeConfigRequest;
import com.topology.phytopo.entity.TopoConfig;
import com.topology.phytopo.mapper.TopoConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 配置服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {

    private final TopoConfigMapper configMapper;

    /**
     * 获取配置值
     */
    public String getConfigValue(String key) {
        TopoConfig config = configMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置值（带默认值）
     */
    public String getConfigValue(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 设置配置值
     */
    @Transactional(rollbackFor = Exception.class)
    public void setConfigValue(String key, String value) {
        TopoConfig config = configMapper.selectByKey(key);
        long now = System.currentTimeMillis();

        if (config == null) {
            config = new TopoConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCreatedTime(now);
            config.setUpdatedTime(now);
            configMapper.insert(config);
        } else {
            config.setConfigValue(value);
            config.setUpdatedTime(now);
            configMapper.update(config);
        }
    }

    /**
     * 获取合并配置
     */
    public MergeConfigRequest getMergeConfig() {
        MergeConfigRequest config = new MergeConfigRequest();
        config.setEnabled(Boolean.parseBoolean(getConfigValue("MERGE_ENABLED", "false")));
        String thresholdStr = getConfigValue("MERGE_THRESHOLD", "20");
        config.setThreshold(Integer.parseInt(thresholdStr));
        config.setMode(getConfigValue("MERGE_MODE", "THRESHOLD"));
        return config;
    }

    /**
     * 更新合并配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMergeConfig(MergeConfigRequest request) {
        if (request.getEnabled() != null) {
            setConfigValue("MERGE_ENABLED", request.getEnabled().toString());
        }
        if (request.getThreshold() != null) {
            setConfigValue("MERGE_THRESHOLD", request.getThreshold().toString());
        }
        if (request.getMode() != null) {
            setConfigValue("MERGE_MODE", request.getMode());
        }
    }
}
