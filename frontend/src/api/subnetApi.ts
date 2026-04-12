import client from './client';
import type { Result } from '@/types/api';
import type { TreeNodeVO, SubnetDetailVO } from '@/types/topology';
import type { SubnetCreateRequest, SubnetUpdateRequest, BatchMoveRequest } from '@/types/request';

export const subnetApi = {
  /** 获取子网树 */
  getTree(rootDn?: string) {
    return client.get<Result<TreeNodeVO[]>>('/subnet/tree', {
      params: rootDn ? { rootDn } : undefined,
    });
  },

  /** 获取子网详情 */
  getDetail(dn: string) {
    return client.get<Result<SubnetDetailVO>>('/subnet/detail', {
      params: { dn },
    });
  },

  /** 创建子网 */
  create(data: SubnetCreateRequest) {
    return client.post<Result<string>>('/subnet', data);
  },

  /** 更新子网 */
  update(dn: string, data: SubnetUpdateRequest) {
    return client.put<Result<string>>('/subnet/update', data, {
      params: { dn },
    });
  },

  /** 删除子网 */
  remove(dn: string) {
    return client.delete<Result<string>>('/subnet/delete', {
      params: { dn },
    });
  },

  /** 批量移入 */
  batchMove(data: BatchMoveRequest) {
    return client.post<Result<string>>('/subnet/batch-move', data);
  },

  /** 移动节点（网元或子网） */
  move(dn: string, type: string, targetDn: string) {
    return client.post<Result<string>>('/subnet/move', null, {
      params: { dn, type, targetDn },
    });
  },
};
