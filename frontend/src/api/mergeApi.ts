import client from './client';
import type { Result } from '@/types/api';
import type { MergeConfigRequest } from '@/types/request';

export const mergeApi = {
  /** 获取合并配置 */
  getConfig() {
    return client.get<Result<MergeConfigRequest>>('/merge/config');
  },

  /** 更新合并配置 */
  updateConfig(data: MergeConfigRequest) {
    return client.put<Result<string>>('/merge/config', data);
  },

  /** 执行合并 */
  execute() {
    return client.post<Result<string>>('/merge/execute');
  },

  /** 取消合并 */
  disable() {
    return client.post<Result<string>>('/merge/disable');
  },
};
