import { useEffect, useState } from 'react';
import { Drawer, Descriptions, Tag, Spin, Badge } from 'antd';
import { FolderOutlined, AlertOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useTopologyStore } from '@/stores/useTopologyStore';
import { getNeIcon, getStatusColor } from '@/utils/iconUtils';
import { neApi } from '@/api/neApi';
import { subnetApi } from '@/api/subnetApi';
import type { TopoNodeData } from '@/utils/topoUtils';
import type { NeDetailVO, SubnetDetailVO } from '@/types/topology';

const ALARM_LEVEL_CONFIG = [
  { key: 'critical', label: '严重', color: '#cf1322', bg: '#fff1f0' },
  { key: 'major', label: '主要', color: '#d46b08', bg: '#fff7e6' },
  { key: 'minor', label: '一般', color: '#096dd9', bg: '#e6f7ff' },
  { key: 'warning', label: '警告', color: '#d48806', bg: '#fffbe6' },
];

function AlarmSummary({
  critical,
  major,
  minor,
  warning,
}: {
  critical: number;
  major: number;
  minor: number;
  warning: number;
}) {
  const total = critical + major + minor + warning;
  const counts = [critical, major, minor, warning];

  return (
    <div style={{ marginTop: 20 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 12 }}>
        {total > 0 ? (
          <Badge count={total} size="small" color="#cf1322">
            <AlertOutlined style={{ fontSize: 16, color: '#cf1322' }} />
          </Badge>
        ) : (
          <CheckCircleOutlined style={{ fontSize: 16, color: '#52c41a' }} />
        )}
        <span style={{ fontWeight: 600, fontSize: 13, color: '#262626' }}>
          {total > 0 ? `告警统计（共 ${total} 条）` : '告警统计'}
        </span>
      </div>

      {total > 0 ? (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          {ALARM_LEVEL_CONFIG.map((level, i) => (
            <div
              key={level.key}
              style={{
                padding: '8px 12px',
                borderRadius: 6,
                background: level.bg,
                border: `1px solid ${level.color}20`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <span style={{ fontSize: 12, color: level.color, fontWeight: 500 }}>{level.label}</span>
              <span style={{ fontSize: 16, fontWeight: 700, color: level.color }}>{counts[i]}</span>
            </div>
          ))}
        </div>
      ) : (
        <div
          style={{
            padding: '12px 0',
            textAlign: 'center',
            color: '#8c8c8c',
            fontSize: 13,
            background: '#fafafa',
            borderRadius: 6,
          }}
        >
          暂无告警
        </div>
      )}
    </div>
  );
}

export default function NodeDetailPanel() {
  const selectedNodeId = useTopologyStore((s) => s.selectedNodeId);
  const nodes = useTopologyStore((s) => s.nodes);
  const setSelectedNodeId = useTopologyStore((s) => s.setSelectedNodeId);

  const [neData, setNeData] = useState<NeDetailVO | null>(null);
  const [subnetData, setSubnetData] = useState<SubnetDetailVO | null>(null);
  const [loading, setLoading] = useState(false);

  const selectedNode = nodes.find((n) => n.id === selectedNodeId);
  const data = selectedNode ? (selectedNode.data as TopoNodeData) : null;

  useEffect(() => {
    setNeData(null);
    setSubnetData(null);
    if (!selectedNodeId || !data) return;
    let cancelled = false;
    setLoading(true);

    if (data.type === 'NE') {
      neApi.getDetail(selectedNodeId)
        .then((res) => {
          if (!cancelled) setNeData(res.data?.data ?? null);
        })
        .catch(() => {})
        .finally(() => {
          if (!cancelled) setLoading(false);
        });
    } else if (data.type === 'SUBNET') {
      subnetApi.getDetail(selectedNodeId)
        .then((res) => {
          if (!cancelled) setSubnetData(res.data?.data ?? null);
        })
        .catch(() => {})
        .finally(() => {
          if (!cancelled) setLoading(false);
        });
    } else {
      setLoading(false);
    }

    return () => { cancelled = true; };
  }, [selectedNodeId]);

  if (!selectedNode || !data) return null;

  // ========== NE 详情面板 ==========
  if (data.type === 'NE') {
    const Icon = getNeIcon(data.neType);
    const statusColor = getStatusColor(data.status);
    return (
      <Drawer
        title="节点详情"
        placement="right"
        width={360}
        open={!!selectedNodeId}
        onClose={() => setSelectedNodeId(null)}
      >
        <div style={{ textAlign: 'center', marginBottom: 16 }}>
          <Icon style={{ fontSize: 40, color: '#595959' }} />
          <div style={{ marginTop: 8, fontSize: 16, fontWeight: 600 }}>{data.name}</div>
        </div>
        <Descriptions column={1} size="small" bordered>
          <Descriptions.Item label="DN">{data.dn}</Descriptions.Item>
          <Descriptions.Item label="类型"><Tag>{data.type}</Tag></Descriptions.Item>
          {data.neType && (
            <Descriptions.Item label="网元类型"><Tag color="blue">{data.neType}</Tag></Descriptions.Item>
          )}
          <Descriptions.Item label="状态">
            <Tag color={statusColor === '#52c41a' ? 'success' : statusColor === '#ff4d4f' ? 'error' : 'warning'}>
              {data.status || 'UNKNOWN'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>
        {loading ? (
          <div style={{ textAlign: 'center', marginTop: 16 }}><Spin size="small" /></div>
        ) : (
          <AlarmSummary
            critical={neData?.criticalCount ?? 0}
            major={neData?.majorCount ?? 0}
            minor={neData?.minorCount ?? 0}
            warning={neData?.warningCount ?? 0}
          />
        )}
      </Drawer>
    );
  }

  // ========== 子网详情面板 ==========
  if (data.type === 'SUBNET') {
    const detail = subnetData;
    const c = detail?.criticalCount ?? 0;
    const m = detail?.majorCount ?? 0;
    const n = detail?.minorCount ?? 0;
    const w = detail?.warningCount ?? 0;

    return (
      <Drawer
        title="子网详情"
        placement="right"
        width={360}
        open={!!selectedNodeId}
        onClose={() => setSelectedNodeId(null)}
      >
        <div style={{ textAlign: 'center', marginBottom: 16 }}>
          <FolderOutlined style={{ fontSize: 40, color: '#1890ff' }} />
          <div style={{ marginTop: 8, fontSize: 16, fontWeight: 600 }}>{data.name}</div>
        </div>
        <Descriptions column={1} size="small" bordered>
          <Descriptions.Item label="DN">{data.dn}</Descriptions.Item>
          <Descriptions.Item label="类型"><Tag color="blue">SUBNET</Tag></Descriptions.Item>
          {data.isMergeGroup && (
            <Descriptions.Item label="合并组"><Tag color="purple">是</Tag></Descriptions.Item>
          )}
          {data.childCount > 0 && (
            <Descriptions.Item label="子节点数">{data.childCount}</Descriptions.Item>
          )}
          {detail && (
            <>
              <Descriptions.Item label="网元总数">{detail.neCount ?? 0}</Descriptions.Item>
              <Descriptions.Item label="子网数">{detail.subnetCount ?? 0}</Descriptions.Item>
              <Descriptions.Item label="在线">
                <Tag color="success">{detail.onlineCount ?? 0}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="离线">
                <Tag color="error">{detail.offlineCount ?? 0}</Tag>
              </Descriptions.Item>
            </>
          )}
        </Descriptions>
        {loading ? (
          <div style={{ textAlign: 'center', marginTop: 16 }}><Spin size="small" /></div>
        ) : (
          <AlarmSummary critical={c} major={m} minor={n} warning={w} />
        )}
      </Drawer>
    );
  }

  return null;
}
