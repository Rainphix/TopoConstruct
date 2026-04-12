import type { TreeNodeVO } from '@/types/topology';
import type { TopologyViewVO } from '@/types/topology';

/** Mock 树数据 */
export const mockTreeData: TreeNodeVO[] = [
  {
    dn: 'DC-001',
    name: 'Default',
    displayName: '默认数据中心',
    type: 'SUBNET',
    isMergeGroup: false,
    layer: 0,
    childCount: 5,
    children: [
      {
        dn: 'SUBNET-CORE',
        name: 'Core',
        displayName: '核心网',
        type: 'SUBNET',
        isMergeGroup: false,
        layer: 1,
        childCount: 3,
        children: [
          { dn: 'NE-FW-01', name: 'FW-01', displayName: '核心防火墙', type: 'NE', neType: 'FIREWALL', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
          { dn: 'NE-SW-01', name: 'SW-01', displayName: '核心交换机', type: 'NE', neType: 'SWITCH', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
          { dn: 'NE-RT-01', name: 'RT-01', displayName: '核心路由器', type: 'NE', neType: 'ROUTER', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
        ],
      },
      {
        dn: 'SUBNET-SERVER',
        name: 'Server',
        displayName: '服务器区',
        type: 'SUBNET',
        isMergeGroup: false,
        layer: 1,
        childCount: 2,
        children: [
          { dn: 'NE-SRV-01', name: 'SRV-01', displayName: '应用服务器1', type: 'NE', neType: 'SERVER', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
          { dn: 'NE-SRV-02', name: 'SRV-02', displayName: '应用服务器2', type: 'NE', neType: 'SERVER', isMergeGroup: false, layer: 2, childCount: 0, status: 'OFFLINE' },
        ],
      },
      {
        dn: 'SUBNET-DMZ',
        name: 'DMZ',
        displayName: 'DMZ区域',
        type: 'SUBNET',
        isMergeGroup: false,
        layer: 1,
        childCount: 2,
        children: [
          { dn: 'NE-LB-01', name: 'LB-01', displayName: '负载均衡', type: 'NE', neType: 'LOAD_BALANCER', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
          { dn: 'NE-FW-02', name: 'FW-02', displayName: '边界防火墙', type: 'NE', neType: 'FIREWALL', isMergeGroup: false, layer: 2, childCount: 0, status: 'ONLINE' },
        ],
      },
    ],
  },
];

/** Mock 拓扑视图 */
export function getMockTopologyView(subnetDn: string): TopologyViewVO | null {
  const views: Record<string, TopologyViewVO> = {
    'DC-001': {
      subnetDn: 'DC-001',
      subnetName: '默认数据中心',
      elements: [
        { dn: 'SUBNET-CORE', name: '核心网', type: 'SUBNET', x: 300, y: 50, width: 120, height: 70, isMergeGroup: false, childCount: 3, status: 'ONLINE' },
        { dn: 'SUBNET-SERVER', name: '服务器区', type: 'SUBNET', x: 100, y: 250, width: 120, height: 70, isMergeGroup: false, childCount: 2, status: 'ONLINE' },
        { dn: 'SUBNET-DMZ', name: 'DMZ区域', type: 'SUBNET', x: 500, y: 250, width: 120, height: 70, isMergeGroup: false, childCount: 2, status: 'ONLINE' },
      ],
    },
    'SUBNET-CORE': {
      subnetDn: 'SUBNET-CORE',
      subnetName: '核心网',
      elements: [
        { dn: 'NE-FW-01', name: '核心防火墙', type: 'NE', neType: 'FIREWALL', x: 200, y: 50, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
        { dn: 'NE-SW-01', name: '核心交换机', type: 'NE', neType: 'SWITCH', x: 400, y: 50, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
        { dn: 'NE-RT-01', name: '核心路由器', type: 'NE', neType: 'ROUTER', x: 300, y: 200, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
      ],
    },
    'SUBNET-SERVER': {
      subnetDn: 'SUBNET-SERVER',
      subnetName: '服务器区',
      elements: [
        { dn: 'NE-SRV-01', name: '应用服务器1', type: 'NE', neType: 'SERVER', x: 150, y: 80, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
        { dn: 'NE-SRV-02', name: '应用服务器2', type: 'NE', neType: 'SERVER', x: 350, y: 80, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'OFFLINE' },
      ],
    },
    'SUBNET-DMZ': {
      subnetDn: 'SUBNET-DMZ',
      subnetName: 'DMZ区域',
      elements: [
        { dn: 'NE-LB-01', name: '负载均衡', type: 'NE', neType: 'LOAD_BALANCER', x: 150, y: 80, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
        { dn: 'NE-FW-02', name: '边界防火墙', type: 'NE', neType: 'FIREWALL', x: 350, y: 80, width: 90, height: 65, isMergeGroup: false, childCount: 0, status: 'ONLINE' },
      ],
    },
  };
  return views[subnetDn] ?? null;
}
