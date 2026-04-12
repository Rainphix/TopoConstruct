import axios from 'axios';
import { message } from 'antd';
import type { Result } from '@/types/api';

const client = axios.create({
  baseURL: '/api/topo',
  timeout: 15000,
});

client.interceptors.response.use(
  (res) => {
    const data = res.data as Result<unknown>;
    if (data.code !== 200) {
      message.error(data.message || '请求失败');
      return Promise.reject(new Error(data.message));
    }
    return res;
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络错误';
    message.error(msg);
    return Promise.reject(error);
  },
);

export default client;
