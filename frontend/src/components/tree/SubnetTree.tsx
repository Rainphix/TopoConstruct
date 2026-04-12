import { useEffect, useCallback } from 'react';
import { Tree, Spin, Button, Space, Alert, Tooltip, Popconfirm, message } from 'antd';
import {
  FolderOutlined,
  DownOutlined,
  ExpandOutlined,
  ShrinkOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import type { TreeDataNode, TreeProps } from 'antd';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import { getNeIcon } from '@/utils/iconUtils';
import { subnetApi } from '@/api/subnetApi';
import type { TreeNodeVO } from '@/types/topology';
import TreeNodeTitle from './TreeNodeTitle';

/** 扩展 TreeDataNode，携带节点类型 */
interface PhyTreeDataNode extends TreeDataNode {
  nodeType?: string;
}

/** 将后端树数据转换为 Ant Design TreeDataNode */
function toTreeData(nodes: TreeNodeVO[]): PhyTreeDataNode[] {
  return nodes.map((node) => {
    const isSubnet = node.type === 'SUBNET';
    const Icon = isSubnet ? undefined : getNeIcon(node.neType);
    return {
      key: node.dn,
      title: <TreeNodeTitle node={node} />,
      icon: isSubnet ? <FolderOutlined /> : Icon ? <Icon /> : undefined,
      children: node.children ? toTreeData(node.children) : undefined,
      isLeaf: !isSubnet,
      nodeType: node.type,
    };
  });
}

function collectAllKeys(nodes: TreeNodeVO[]): string[] {
  const keys: string[] = [];
  for (const node of nodes) {
    if (node.children && node.children.length > 0) {
      keys.push(node.dn);
      keys.push(...collectAllKeys(node.children));
    }
  }
  return keys;
}

/** 查找节点在树中的信息 */
function findNode(nodes: TreeNodeVO[], dn: string): TreeNodeVO | null {
  for (const n of nodes) {
    if (n.dn === dn) return n;
    if (n.children) {
      const found = findNode(n.children, dn);
      if (found) return found;
    }
  }
  return null;
}

/** 查找节点的父节点 DN */
function findParentDn(nodes: TreeNodeVO[], dn: string): string | null {
  for (const n of nodes) {
    if (n.children) {
      for (const child of n.children) {
        if (child.dn === dn) return n.dn;
      }
      const found = findParentDn(n.children, dn);
      if (found) return found;
    }
  }
  return null;
}

export default function SubnetTree() {
  const { treeData, loading, expandedKeys, isDemo, selectedKey, fetchTree, setSelectedKey, setExpandedKeys } =
    useTreeStore();
  const fetchView = useTopologyStore((s) => s.fetchView);
  const fetchNeDetail = useTopologyStore((s) => s.fetchNeDetail);
  const clearNeDetail = useTopologyStore((s) => s.clearNeDetail);

  useEffect(() => {
    fetchTree().then(() => {
      const data = useTreeStore.getState().treeData;
      if (data.length > 0 && !useTreeStore.getState().selectedKey) {
        const rootDn = data[0].dn;
        setSelectedKey(rootDn);
        fetchView(rootDn);
      }
    });
  }, [fetchTree, setSelectedKey, fetchView]);

  const onSelect: TreeProps['onSelect'] = useCallback(
    (keys) => {
      const key = keys[0] as string | undefined;
      setSelectedKey(key ?? null);
      if (key) {
        const node = findNode(treeData, key);
        if (node?.type === 'NE') {
          clearNeDetail();
          fetchNeDetail(key);
        } else {
          clearNeDetail();
          fetchView(key);
        }
      }
    },
    [setSelectedKey, fetchView, fetchNeDetail, clearNeDetail, treeData],
  );

  const handleExpandAll = () => {
    const allKeys = collectAllKeys(treeData);
    setExpandedKeys(allKeys);
  };

  const handleCollapseAll = () => {
    setExpandedKeys([]);
  };

  // CRUD 按钮通过事件触发 App.tsx 中的 dialog
  const handleCreateSubnet = () => {
    const parentDn = selectedKey || (treeData.length > 0 ? treeData[0].dn : '');
    window.dispatchEvent(
      new CustomEvent('nodeContextAction', {
        detail: { action: 'createChild', nodeDn: parentDn },
      }),
    );
  };

  const handleCreateNe = () => {
    const parentDn = selectedKey || (treeData.length > 0 ? treeData[0].dn : '');
    window.dispatchEvent(
      new CustomEvent('nodeContextAction', {
        detail: { action: 'createNe', nodeDn: parentDn },
      }),
    );
  };

  const handleEdit = () => {
    if (!selectedKey) return;
    const node = findNode(treeData, selectedKey);
    window.dispatchEvent(
      new CustomEvent('nodeContextAction', {
        detail: {
          action: 'edit',
          nodeDn: selectedKey,
          nodeName: node?.displayName || node?.name,
          nodeType: node?.type || 'SUBNET',
        },
      }),
    );
  };

  const handleDelete = () => {
    if (!selectedKey) return;
    const node = findNode(treeData, selectedKey);
    window.dispatchEvent(
      new CustomEvent('nodeContextAction', {
        detail: {
          action: 'delete',
          nodeDn: selectedKey,
          nodeName: node?.displayName || node?.name,
          nodeType: node?.type || 'SUBNET',
        },
      }),
    );
  };

  const selectedNode = selectedKey ? findNode(treeData, selectedKey) : null;

  const onDrop: TreeProps['onDrop'] = useCallback(
    async (info) => {
      const dragKey = info.dragNode.key as string;
      const dropKey = info.node.key as string;

      // 从 treeData 查找节点信息，不依赖 TreeDataNode 自定义属性
      const dragNodeVo = findNode(treeData, dragKey);
      const dropNodeVo = findNode(treeData, dropKey);
      if (!dragNodeVo || !dropNodeVo) {
        message.error('无法确定拖拽节点');
        return;
      }

      const dragType = dragNodeVo.type;

      // 解析目标父节点 DN
      let targetDn: string;
      if (info.dropToGap) {
        // 间隙放置：如果 dropNode 是 SUBNET，用户意图是放入该子网
        if (dropNodeVo.type === 'SUBNET') {
          targetDn = dropKey;
        } else {
          // dropNode 是 NE，取其父节点
          const parentDn = findParentDn(treeData, dropKey);
          if (!parentDn) {
            message.error('不能移动到根层级');
            return;
          }
          targetDn = parentDn;
        }
      } else {
        // 直接放置到节点上
        if (dropNodeVo.type !== 'SUBNET') {
          message.warning('网元不能作为父节点');
          return;
        }
        targetDn = dropKey;
      }

      // 不能拖到自己
      if (dragKey === targetDn) {
        message.warning('不能移动到自身');
        return;
      }

      try {
        await subnetApi.move(dragKey, dragType, targetDn);
        message.success('移动成功');
        // 展开目标节点并刷新
        if (!expandedKeys.includes(targetDn)) {
          setExpandedKeys([...expandedKeys, targetDn]);
        }
        fetchTree();
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : '移动失败';
        message.error(msg);
      }
    },
    [treeData, fetchTree, expandedKeys, setExpandedKeys],
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {isDemo && (
        <Alert
          message="演示模式"
          description="后端未连接，显示示例数据"
          type="info"
          showIcon
          style={{ margin: '8px 12px', borderRadius: 6 }}
          closable={false}
        />
      )}
      {/* 标题栏 */}
      <div
        style={{
          padding: '8px 12px',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ fontWeight: 600, fontSize: 14 }}>子网导航</span>
        <Space size={4}>
          <Button
            type="text"
            size="small"
            icon={<ExpandOutlined />}
            onClick={handleExpandAll}
            title="展开全部"
          />
          <Button
            type="text"
            size="small"
            icon={<ShrinkOutlined />}
            onClick={handleCollapseAll}
            title="折叠全部"
          />
        </Space>
      </div>

      {/* 操作按钮栏 */}
      <div
        style={{
          padding: '6px 12px',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          gap: 6,
          alignItems: 'center',
          flexWrap: 'wrap',
        }}
      >
        <Button
          type="primary"
          size="small"
          icon={<PlusOutlined />}
          onClick={handleCreateSubnet}
        >
          子网
        </Button>
        <Button
          size="small"
          icon={<PlusOutlined />}
          onClick={handleCreateNe}
        >
          网元
        </Button>
        <Tooltip title={selectedKey ? '编辑选中节点' : '请先选中一个节点'}>
          <Button
            size="small"
            icon={<EditOutlined />}
            disabled={!selectedKey}
            onClick={handleEdit}
          >
            编辑
          </Button>
        </Tooltip>
        <Popconfirm
          title="确认删除"
          description={`确定删除 ${selectedNode?.displayName || selectedNode?.name || '选中节点'} 吗？`}
          onConfirm={handleDelete}
          okText="删除"
          cancelText="取消"
          okButtonProps={{ danger: true }}
          disabled={!selectedKey}
        >
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            disabled={!selectedKey}
          >
            删除
          </Button>
        </Popconfirm>
      </div>

      {/* 树 */}
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 0' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : (
          <Tree
            showIcon
            blockNode
            draggable
            allowDrop={({ dropNode, dropPosition }) => {
              const node = dropNode as PhyTreeDataNode;
              // dropPosition 0 = 放入节点内部，只允许 SUBNET
              // dropPosition -1/1 = 放在节点前后，始终允许
              if (dropPosition === 0) {
                return node.nodeType === 'SUBNET';
              }
              return true;
            }}
            switcherIcon={<DownOutlined />}
            treeData={toTreeData(treeData)}
            expandedKeys={expandedKeys}
            onExpand={(keys) => setExpandedKeys(keys as string[])}
            onSelect={onSelect}
            selectedKeys={selectedKey ? [selectedKey] : []}
            onDrop={onDrop}
          />
        )}
      </div>
    </div>
  );
}
