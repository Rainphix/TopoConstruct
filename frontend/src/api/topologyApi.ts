import client from './client';
import type { Result } from '@/types/api';
import type { TopologyViewVO } from '@/types/topology';
import type { PositionSaveRequest } from '@/types/request';

export const topologyApi = {
  /** 获取拓扑视图 */
  getView(subnetDn: string) {
    return client.get<Result<TopologyViewVO>>('/topo/view', {
      params: { subnetDn },
    });
  },

  /** 保存坐标 */
  savePosition(data: PositionSaveRequest) {
    return client.post<Result<string>>('/topo/position', data);
  },

  /** 自动布局 */
  autoLayout(subnetDn: string) {
    return client.post<Result<TopologyViewVO>>('/topo/auto-layout', null, {
      params: { subnetDn },
    });
  },
};
