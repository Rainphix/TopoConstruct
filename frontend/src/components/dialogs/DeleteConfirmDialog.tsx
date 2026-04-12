import { Modal, message } from 'antd';
import { subnetApi } from '@/api/subnetApi';
import { neApi } from '@/api/neApi';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import type { ElementType } from '@/types/api';

interface Props {
  open: boolean;
  dn: string;
  name: string;
  nodeType: ElementType;
  onClose: () => void;
}

export default function DeleteConfirmDialog({ open, dn, name, nodeType, onClose }: Props) {
  const fetchTree = useTreeStore((s) => s.fetchTree);
  const refreshView = useTopologyStore((s) => s.refreshView);

  const isNe = nodeType === 'NE';
  const label = isNe ? '网元' : '子网';

  const handleOk = async () => {
    try {
      if (isNe) {
        await neApi.remove(dn);
      } else {
        await subnetApi.remove(dn);
      }
      message.success('删除成功');
      onClose();
      fetchTree();
      refreshView();
    } catch {
      // error handled by interceptor
    }
  };

  return (
    <Modal
      title="确认删除"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      okText="删除"
      okButtonProps={{ danger: true }}
      cancelText="取消"
    >
      <p>
        确定要删除{label} <strong>{name}</strong> 吗？
      </p>
      {!isNe && (
        <p style={{ color: '#ff4d4f' }}>此操作不可撤销，子网下存在设备或子网时无法删除。</p>
      )}
    </Modal>
  );
}
