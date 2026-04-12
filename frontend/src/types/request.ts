import type { ElementType, MergeMode } from './api';

/** 子网创建请求 */
export interface SubnetCreateRequest {
  name: string;
  displayName: string;
  parentDn: string;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
}

/** 子网更新请求 */
export interface SubnetUpdateRequest {
  dn: string;
  name?: string;
  displayName?: string;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
}

/** 网元创建请求 */
export interface NeCreateRequest {
  name: string;
  displayName?: string;
  neType: string;
  parentDn: string;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
}

/** 网元更新请求 */
export interface NeUpdateRequest {
  name?: string;
  displayName?: string;
  address?: string;
  location?: string;
  maintainer?: string;
  contact?: string;
}

/** 坐标项 */
export interface PositionItem {
  elementDn: string;
  elementType: ElementType;
  x: number;
  y: number;
  width: number;
  height: number;
}

/** 坐标保存请求 */
export interface PositionSaveRequest {
  subnetDn: string;
  positions: PositionItem[];
}

/** 批量移入请求 */
export interface BatchMoveRequest {
  targetSubnetDn: string;
  neDnList: string[];
}

/** 合并配置请求 */
export interface MergeConfigRequest {
  enabled: boolean;
  threshold: number;
  mode: MergeMode;
}
