import { useCallback, useRef, useState } from 'react';
import { Button, Layout } from 'antd';
import { SettingOutlined } from '@ant-design/icons';
import SubnetTree from '@/components/tree/SubnetTree';
import TopologyCanvas from '@/components/canvas/TopologyCanvas';

const { Content, Header } = Layout;

export default function AppLayout() {
  const [siderWidth, setSiderWidth] = useState(280);
  const dragging = useRef(false);
  const startX = useRef(0);
  const startWidth = useRef(0);

  const onMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    dragging.current = true;
    startX.current = e.clientX;
    startWidth.current = siderWidth;
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';

    const onMouseMove = (ev: MouseEvent) => {
      if (!dragging.current) return;
      const delta = ev.clientX - startX.current;
      const next = Math.min(Math.max(startWidth.current + delta, 180), 600);
      setSiderWidth(next);
    };

    const onMouseUp = () => {
      dragging.current = false;
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }, [siderWidth]);

  return (
    <Layout style={{ height: '100vh' }}>
      <Header
        style={{
          background: '#001529',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 24px',
          height: 48,
          lineHeight: '48px',
          fontSize: 18,
          fontWeight: 600,
        }}
      >
        <span>PhyTopo 物理拓扑管理</span>
        <Button
          type="text"
          icon={<SettingOutlined />}
          style={{ color: '#fff' }}
          onClick={() =>
            window.dispatchEvent(new CustomEvent('openMergeConfig'))
          }
        >
          合并配置
        </Button>
      </Header>
      <Layout style={{ flexDirection: 'row', flex: 1, overflow: 'hidden' }}>
        <div
          style={{
            width: siderWidth,
            minWidth: 180,
            maxWidth: 600,
            background: '#fff',
            borderRight: '1px solid #f0f0f0',
            overflow: 'auto',
          }}
        >
          <SubnetTree />
        </div>
        <div
          onMouseDown={onMouseDown}
          style={{
            width: 4,
            cursor: 'col-resize',
            background: 'transparent',
            flexShrink: 0,
            zIndex: 10,
            transition: 'background 0.15s',
          }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLDivElement).style.background = '#1890ff';
          }}
          onMouseLeave={(e) => {
            if (!dragging.current) {
              (e.currentTarget as HTMLDivElement).style.background = 'transparent';
            }
          }}
        />
        <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
          <TopologyCanvas />
        </div>
      </Layout>
    </Layout>
  );
}
