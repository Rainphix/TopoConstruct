/** 后端统一响应结构 */
export interface Result<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

/** 元素类型 */
export type ElementType = 'SUBNET' | 'NE' | 'MERGE_GROUP';

/** 网元类型 */
export type NeType =
  | 'FIREWALL'
  | 'SWITCH'
  | 'SERVER'
  | 'STORAGE'
  | 'GATEWAY'
  | 'CHASSIS'
  | 'RACK'
  | 'DEFAULT';

/** 合并模式 */
export type MergeMode = 'SAME_TYPE' | 'SAME_STATUS' | 'CUSTOM';

/** 在线状态 */
export type Status = 'ONLINE' | 'OFFLINE' | 'UNKNOWN';
