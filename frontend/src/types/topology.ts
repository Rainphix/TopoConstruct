import type { ElementType, NeType, Status } from './api';

/** 树节点 */
export interface TreeNodeVO {
  dn: string;
  name: string;
  displayName: string;
  type: ElementType;
  neType?: NeType;
  icon?: string;
  isMergeGroup: boolean;
  layer: number;
  status?: Status;
  childCount: number;
  alarmCount?: number;
  children?: TreeNodeVO[];
}

/** 子网详情 */
export interface SubnetDetailVO {
  dn: string;
  name: string;
  displayName: string;
  layer: number;
  isMergeGroup: boolean;
  mergeType?: string;
  neCount: number;
  subnetCount: number;
  offlineCount: number;
  onlineCount: number;
  criticalCount: number;
  majorCount: number;
  minorCount: number;
  warningCount: number;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
}

/** 网元详情 */
export interface NeDetailVO {
  id: number;
  dn: string;
  name: string;
  displayName: string;
  neType: NeType;
  icon?: string;
  parentDn: string;
  parentType: string;
  rootSubnetDn: string;
  medNode?: string;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
  status: Status;
  alarmStatus?: string;
  criticalCount: number;
  majorCount: number;
  minorCount: number;
  warningCount: number;
  sequenceNo: number;
  createdTime: string;
  updatedTime: string;
  syncTime?: string;
  statusDesc?: string;
  version: number;
}

/** 拓扑元素 */
export interface TopoElement {
  dn: string;
  name: string;
  type: ElementType;
  neType?: NeType;
  icon?: string;
  x: number;
  y: number;
  width: number;
  height: number;
  status?: Status;
  isMergeGroup: boolean;
  alarmStatus?: string;
  criticalCount?: number;
  majorCount?: number;
  minorCount?: number;
  warningCount?: number;
  childCount: number;
}

/** 拓扑视图 */
export interface TopologyViewVO {
  subnetDn: string;
  subnetName: string;
  criticalCount?: number;
  majorCount?: number;
  minorCount?: number;
  warningCount?: number;
  neCount?: number;
  onlineCount?: number;
  offlineCount?: number;
  subnetCount?: number;
  elements: TopoElement[];
}
