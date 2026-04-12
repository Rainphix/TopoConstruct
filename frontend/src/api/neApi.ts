import client from './client';
import type { Result } from '@/types/api';
import type { NeDetailVO } from '@/types/topology';
import type { NeCreateRequest, NeUpdateRequest } from '@/types/request';

export const neApi = {
  /** 获取网元详情 */
  getDetail(dn: string) {
    return client.get<Result<NeDetailVO>>('/ne/detail', {
      params: { dn },
    });
  },

  /** 创建网元 */
  create(data: NeCreateRequest) {
    return client.post<Result<string>>('/ne', data);
  },

  /** 更新网元 */
  update(dn: string, data: NeUpdateRequest) {
    return client.put<Result<string>>('/ne/update', data, {
      params: { dn },
    });
  },

  /** 删除网元 */
  remove(dn: string) {
    return client.delete<Result<string>>('/ne/delete', {
      params: { dn },
    });
  },
};
