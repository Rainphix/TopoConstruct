import { create } from 'zustand';
import type { Node, Edge } from '@xyflow/react';
import type { TopoElement, TopologyViewVO, NeDetailVO } from '@/types/topology';
import { topologyApi } from '@/api/topologyApi';
import { neApi } from '@/api/neApi';
import { topoElementToNode, buildEdges } from '@/utils/topoUtils';
import { getMockTopologyView } from '@/utils/mockData';

interface TopologyState {
  subnetDn: string | null;
  subnetName: string;
  elements: TopoElement[];
  nodes: Node[];
  edges: Edge[];
  loading: boolean;
  selectedNodeId: string | null;
  selectedNeDetail: NeDetailVO | null;
  isDemo: boolean;
  /** 当前子网聚合告警 */
  viewCriticalCount: number;
  viewMajorCount: number;
  viewMinorCount: number;
  viewWarningCount: number;
  viewNeCount: number;
  viewOnlineCount: number;
  viewOfflineCount: number;
  viewSubnetCount: number;

  fetchView: (subnetDn: string) => Promise<void>;
  fetchNeDetail: (dn: string) => Promise<void>;
  clearNeDetail: () => void;
  setNodes: (nodes: Node[]) => void;
  setSelectedNodeId: (id: string | null) => void;
  applyAutoLayout: () => Promise<void>;
  refreshView: () => Promise<void>;
}

export const useTopologyStore = create<TopologyState>((set, get) => ({
  subnetDn: null,
  subnetName: '',
  elements: [],
  nodes: [],
  edges: [],
  loading: false,
  selectedNodeId: null,
  selectedNeDetail: null,
  isDemo: false,
  viewCriticalCount: 0,
  viewMajorCount: 0,
  viewMinorCount: 0,
  viewWarningCount: 0,
  viewNeCount: 0,
  viewOnlineCount: 0,
  viewOfflineCount: 0,
  viewSubnetCount: 0,

  fetchView: async (subnetDn: string) => {
    set({ loading: true, subnetDn, selectedNeDetail: null });
    try {
      const res = await topologyApi.getView(subnetDn);
      const view: TopologyViewVO = res.data.data;
      const nodes = view.elements.map(topoElementToNode);
      const edges = buildEdges(view.elements);
      set({
        subnetName: view.subnetName,
        elements: view.elements,
        nodes,
        edges,
        loading: false,
        selectedNodeId: null,
        isDemo: false,
        viewCriticalCount: view.criticalCount ?? 0,
        viewMajorCount: view.majorCount ?? 0,
        viewMinorCount: view.minorCount ?? 0,
        viewWarningCount: view.warningCount ?? 0,
        viewNeCount: view.neCount ?? 0,
        viewOnlineCount: view.onlineCount ?? 0,
        viewOfflineCount: view.offlineCount ?? 0,
        viewSubnetCount: view.subnetCount ?? 0,
      });
    } catch {
      const mockView = getMockTopologyView(subnetDn);
      if (mockView) {
        const nodes = mockView.elements.map(topoElementToNode);
        const edges = buildEdges(mockView.elements);
        set({
          subnetName: mockView.subnetName,
          elements: mockView.elements,
          nodes,
          edges,
          loading: false,
          selectedNodeId: null,
          isDemo: true,
          viewCriticalCount: 0,
          viewMajorCount: 0,
          viewMinorCount: 0,
          viewWarningCount: 0,
          viewNeCount: 0,
          viewOnlineCount: 0,
          viewOfflineCount: 0,
          viewSubnetCount: 0,
        });
      } else {
        set({ loading: false, isDemo: true });
      }
    }
  },

  fetchNeDetail: async (dn: string) => {
    set({ loading: true, selectedNeDetail: null });
    try {
      const res = await neApi.getDetail(dn);
      set({ selectedNeDetail: res.data.data, loading: false });
    } catch {
      set({ loading: false });
    }
  },

  clearNeDetail: () => set({ selectedNeDetail: null }),

  setNodes: (nodes) => set({ nodes }),

  setSelectedNodeId: (id) => set({ selectedNodeId: id }),

  applyAutoLayout: async () => {
    const { subnetDn } = get();
    if (!subnetDn) return;
    set({ loading: true });
    try {
      const res = await topologyApi.autoLayout(subnetDn);
      const view: TopologyViewVO = res.data.data;
      const nodes = view.elements.map(topoElementToNode);
      const edges = buildEdges(view.elements);
      set({ elements: view.elements, nodes, edges, loading: false });
    } catch {
      set({ loading: false });
    }
  },

  refreshView: async () => {
    const { subnetDn } = get();
    if (subnetDn) {
      await get().fetchView(subnetDn);
    }
  },
}));
