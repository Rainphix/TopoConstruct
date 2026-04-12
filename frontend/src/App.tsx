import { useCallback, useEffect, useState } from 'react';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import AppLayout from '@/components/layout/AppLayout';
import SubnetCreateDialog from '@/components/dialogs/SubnetCreateDialog';
import SubnetEditDialog from '@/components/dialogs/SubnetEditDialog';
import NeCreateDialog from '@/components/dialogs/NeCreateDialog';
import NeEditDialog from '@/components/dialogs/NeEditDialog';
import DeleteConfirmDialog from '@/components/dialogs/DeleteConfirmDialog';
import MergeConfigDialog from '@/components/dialogs/MergeConfigDialog';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import type { ElementType } from '@/types/api';

interface DialogState {
  createSubnet: { open: boolean; parentDn: string };
  createNe: { open: boolean; parentDn: string };
  editSubnet: { open: boolean; dn: string };
  editNe: { open: boolean; dn: string };
  delete: { open: boolean; dn: string; name: string; nodeType: ElementType };
  mergeConfig: { open: boolean };
}

const initialDialogState: DialogState = {
  createSubnet: { open: false, parentDn: '' },
  createNe: { open: false, parentDn: '' },
  editSubnet: { open: false, dn: '' },
  editNe: { open: false, dn: '' },
  delete: { open: false, dn: '', name: '', nodeType: 'SUBNET' },
  mergeConfig: { open: false },
};

export default function App() {
  const [dialogs, setDialogs] = useState(initialDialogState);
  const selectedKey = useTreeStore((s) => s.selectedKey);

  const openDialog = useCallback(
    <K extends keyof DialogState>(key: K, state: DialogState[K]) => {
      setDialogs((prev) => ({ ...prev, [key]: state }));
    },
    [],
  );

  const closeDialog = useCallback(
    <K extends keyof DialogState>(key: K) => {
      setDialogs((prev) => ({
        ...prev,
        [key]: { ...prev[key], open: false },
      }));
    },
    [],
  );

  // Handle context menu actions from both tree and canvas
  useEffect(() => {
    const handler = (e: Event) => {
      const { action, nodeDn, nodeName, nodeType } = (e as CustomEvent).detail as {
        action: string;
        nodeDn: string;
        nodeName: string;
        nodeType: ElementType;
      };

      switch (action) {
        case 'createChild':
          openDialog('createSubnet', { open: true, parentDn: nodeDn });
          break;
        case 'createNe':
          openDialog('createNe', { open: true, parentDn: nodeDn });
          break;
        case 'edit':
          if (nodeType === 'NE') {
            openDialog('editNe', { open: true, dn: nodeDn });
          } else {
            openDialog('editSubnet', { open: true, dn: nodeDn });
          }
          break;
        case 'delete':
          openDialog('delete', { open: true, dn: nodeDn, name: nodeName, nodeType });
          break;
        case 'detail':
          useTopologyStore.getState().setSelectedNodeId(nodeDn);
          break;
      }
    };
    window.addEventListener('nodeContextAction', handler);
    return () => window.removeEventListener('nodeContextAction', handler);
  }, [openDialog]);

  // Handle merge config dialog open
  useEffect(() => {
    const handler = () => openDialog('mergeConfig', { open: true });
    window.addEventListener('openMergeConfig', handler);
    return () => window.removeEventListener('openMergeConfig', handler);
  }, [openDialog]);

  return (
    <ConfigProvider locale={zhCN}>
      <AppLayout />
      <SubnetCreateDialog
        open={dialogs.createSubnet.open}
        parentDn={dialogs.createSubnet.parentDn}
        onClose={() => closeDialog('createSubnet')}
      />
      <NeCreateDialog
        open={dialogs.createNe.open}
        parentDn={dialogs.createNe.parentDn}
        onClose={() => closeDialog('createNe')}
      />
      <SubnetEditDialog
        open={dialogs.editSubnet.open}
        dn={dialogs.editSubnet.dn}
        onClose={() => closeDialog('editSubnet')}
      />
      <NeEditDialog
        open={dialogs.editNe.open}
        dn={dialogs.editNe.dn}
        onClose={() => closeDialog('editNe')}
      />
      <DeleteConfirmDialog
        open={dialogs.delete.open}
        dn={dialogs.delete.dn}
        name={dialogs.delete.name}
        nodeType={dialogs.delete.nodeType}
        onClose={() => closeDialog('delete')}
      />
      <MergeConfigDialog
        open={dialogs.mergeConfig.open}
        onClose={() => closeDialog('mergeConfig')}
      />
    </ConfigProvider>
  );
}
