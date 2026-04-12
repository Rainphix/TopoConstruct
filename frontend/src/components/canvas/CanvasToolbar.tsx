import { Button, Space, Tooltip, Spin } from 'antd';
import {
  LayoutOutlined,
  SaveOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  ExpandOutlined,
} from '@ant-design/icons';
import { useReactFlow } from '@xyflow/react';
import { useTopologyStore } from '@/stores/useTopologyStore';
import { usePositionSave } from '@/hooks/usePositionSave';

export default function CanvasToolbar() {
  const { zoomIn, zoomOut, fitView } = useReactFlow();
  const loading = useTopologyStore((s) => s.loading);
  const { savePositions } = usePositionSave();

  return (
    <div
      style={{
        padding: '4px 16px',
        borderBottom: '1px solid #f0f0f0',
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        background: '#fafafa',
        height: 40,
      }}
    >
      <Space size={4}>
        <Tooltip title="自动布局">
          <Button
            type="text"
            size="small"
            icon={<LayoutOutlined />}
            onClick={() => useTopologyStore.getState().applyAutoLayout()}
          >
            自动布局
          </Button>
        </Tooltip>
        <Tooltip title="保存位置">
          <Button
            type="text"
            size="small"
            icon={<SaveOutlined />}
            onClick={savePositions}
          >
            保存位置
          </Button>
        </Tooltip>
        <span style={{ width: 1, height: 20, background: '#d9d9d9', display: 'inline-block' }} />
        <Tooltip title="放大">
          <Button type="text" size="small" icon={<ZoomInOutlined />} onClick={() => zoomIn()} />
        </Tooltip>
        <Tooltip title="缩小">
          <Button type="text" size="small" icon={<ZoomOutOutlined />} onClick={() => zoomOut()} />
        </Tooltip>
        <Tooltip title="适应画布">
          <Button type="text" size="small" icon={<ExpandOutlined />} onClick={() => fitView()} />
        </Tooltip>
      </Space>
      {loading && <Spin size="small" style={{ marginLeft: 'auto' }} />}
    </div>
  );
}
