package com.topology.phytopo.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * EAM 服务客户端
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EamClient {

    @Value("${eam.base-url:http://localhost:8081/api/eam}")
    private String baseUrl;

    @Value("${eam.timeout:30000}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 查询Default分组下的所有子网和设备
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryDefaultGroup(String groupName) {
        String url = baseUrl + "/managedObjects?groupName=" + groupName;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("查询EAM失败: status={}", response.getStatusCode());
                return Collections.emptyList();
            }
            return objectMapper.readValue(response.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("查询EAM Default分组失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据DN查询对象
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryByDn(String dn) {
        String url = baseUrl + "/managedObjects/" + dn;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            return objectMapper.readValue(response.getBody(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("查询EAM对象失败: dn={}", dn, e);
            return null;
        }
    }

    /**
     * 根据父DN查询子元素
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryChildren(String parentDn) {
        String url = baseUrl + "/managedObjects/" + parentDn + "/children";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(response.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("查询EAM子元素失败: parentDn={}", parentDn, e);
            return Collections.emptyList();
        }
    }

    /**
     * 创建子网
     */
    public boolean createSubnet(Map<String, Object> subnet) {
        String url = baseUrl + "/managedObjects";
        subnet.put("createdTime", System.currentTimeMillis());
        subnet.put("updatedTime", System.currentTimeMillis());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(subnet, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("创建EAM子网失败", e);
            return false;
        }
    }

    /**
     * 更新子网
     */
    public boolean updateSubnet(String dn, Map<String, Object> subnet) {
        String url = baseUrl + "/managedObjects/" + dn;
        subnet.put("updatedTime", System.currentTimeMillis());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(subnet, headers);
            restTemplate.put(url, request);
            return true;
        } catch (RestClientException e) {
            log.error("更新EAM子网失败: dn={}", dn, e);
            return false;
        }
    }

    /**
     * 删除子网
     */
    public boolean deleteSubnet(String dn) {
        String url = baseUrl + "/managedObjects/" + dn;
        try {
            restTemplate.delete(url);
            return true;
        } catch (RestClientException e) {
            log.error("删除EAM子网失败: dn={}", dn, e);
            return false;
        }
    }

    /**
     * 批量更新父节点
     */
    public boolean batchUpdateParent(List<String> dnList, String newParentDn) {
        String url = baseUrl + "/managedObjects/batch-update-parent";
        try {
            Map<String, Object> request = Map.of(
                    "dnList", dnList,
                    "newParentDn", newParentDn
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.put(url, entity);
            return true;
        } catch (RestClientException e) {
            log.error("批量更新父节点失败", e);
            return false;
        }
    }
}
