import { create } from 'zustand';
import type { TreeNodeVO } from '@/types/topology';
import { subnetApi } from '@/api/subnetApi';
import { mockTreeData } from '@/utils/mockData';

interface TreeState {
  treeData: TreeNodeVO[];
  selectedKey: string | null;
  expandedKeys: string[];
  loading: boolean;
  isDemo: boolean;

  fetchTree: () => Promise<void>;
  setSelectedKey: (key: string | null) => void;
  setExpandedKeys: (keys: string[]) => void;
  toggleExpand: (key: string) => void;
}

function collectExpandKeys(nodes: TreeNodeVO[]): string[] {
  const keys: string[] = [];
  for (const n of nodes) {
    if (n.childCount > 0) {
      keys.push(n.dn);
    }
    if (n.children) {
      keys.push(...collectExpandKeys(n.children));
    }
  }
  return keys;
}

export const useTreeStore = create<TreeState>((set, get) => ({
  treeData: [],
  selectedKey: null,
  expandedKeys: [],
  loading: false,
  isDemo: false,

  fetchTree: async () => {
    set({ loading: true });
    try {
      const res = await subnetApi.getTree();
      const data = res.data.data;
      const expandedKeys = collectExpandKeys(data);
      set({ treeData: data, expandedKeys, loading: false, isDemo: false });
    } catch {
      // API 不可用时使用 mock 数据
      const expandedKeys = collectExpandKeys(mockTreeData);
      set({ treeData: mockTreeData, expandedKeys, loading: false, isDemo: true });
    }
  },

  setSelectedKey: (key) => set({ selectedKey: key }),

  setExpandedKeys: (keys) => set({ expandedKeys: keys }),

  toggleExpand: (key) => {
    const { expandedKeys } = get();
    if (expandedKeys.includes(key)) {
      set({ expandedKeys: expandedKeys.filter((k) => k !== key) });
    } else {
      set({ expandedKeys: [...expandedKeys, key] });
    }
  },
}));
