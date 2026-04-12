import { useEffect, useCallback, useState } from 'react';
import { Dropdown } from 'antd';
import type { MenuProps } from 'antd/es/menu';
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  DragOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import type { ElementType } from '@/types/api';

interface Props {
  x: number;
  y: number;
  nodeDn: string;
  nodeName: string;
  nodeType: ElementType;
  isMergeGroup: boolean;
  onClose: () => void;
}

export default function NodeContextMenu({
  x,
  y,
  nodeDn,
  nodeName,
  nodeType,
  onClose,
}: Props) {
  const [visible, setVisible] = useState(true);

  const handleAction = useCallback(
    (action: string) => {
      setVisible(false);
      onClose();

      const event = new CustomEvent('nodeContextAction', {
        detail: { action, nodeDn, nodeName, nodeType },
      });
      window.dispatchEvent(event);
    },
    [nodeDn, nodeName, nodeType, onClose],
  );

  useEffect(() => {
    const handleClick = () => {
      setVisible(false);
      onClose();
    };
    document.addEventListener('click', handleClick);
    return () => document.removeEventListener('click', handleClick);
  }, [onClose]);

  const menuItems: MenuProps['items'] = [
    {
      key: 'detail',
      label: '查看详情',
      icon: <InfoCircleOutlined />,
      onClick: () => handleAction('detail'),
    },
  ];

  if (nodeType === 'SUBNET') {
    menuItems.push(
      {
        key: 'edit',
        label: '编辑子网',
        icon: <EditOutlined />,
        onClick: () => handleAction('edit'),
      },
      {
        key: 'createChild',
        label: '新增子网',
        icon: <PlusOutlined />,
        onClick: () => handleAction('createChild'),
      },
      {
        key: 'createNe',
        label: '新增网元',
        icon: <PlusOutlined />,
        onClick: () => handleAction('createNe'),
      },
      { type: 'divider' },
      {
        key: 'delete',
        label: '删除子网',
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => handleAction('delete'),
      },
    );
  }

  if (nodeType === 'NE') {
    menuItems.push(
      {
        key: 'edit',
        label: '编辑网元',
        icon: <EditOutlined />,
        onClick: () => handleAction('edit'),
      },
      {
        key: 'moveTo',
        label: '移入子网',
        icon: <DragOutlined />,
        onClick: () => handleAction('moveTo'),
      },
      { type: 'divider' },
      {
        key: 'delete',
        label: '删除网元',
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => handleAction('delete'),
      },
    );
  }

  return (
    <Dropdown
      open={visible}
      menu={{ items: menuItems }}
      trigger={['contextmenu']}
    >
      <div
        style={{
          position: 'fixed',
          left: x,
          top: y,
          width: 0,
          height: 0,
        }}
      />
    </Dropdown>
  );
}
