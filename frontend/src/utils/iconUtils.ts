import {
  DesktopOutlined,
  CloudServerOutlined,
  ApiOutlined,
  ClusterOutlined,
  GatewayOutlined,
  AppstoreOutlined,
  DatabaseOutlined,
  HddOutlined,
} from '@ant-design/icons';
import type { NeType } from '@/types/api';

const iconMap: Record<string, React.ComponentType> = {
  FIREWALL: GatewayOutlined,
  SWITCH: ClusterOutlined,
  SERVER: CloudServerOutlined,
  STORAGE: DatabaseOutlined,
  GATEWAY: ApiOutlined,
  CHASSIS: HddOutlined,
  RACK: DesktopOutlined,
  DEFAULT: AppstoreOutlined,
};

/** 获取网元类型对应的图标组件 */
export function getNeIcon(neType?: NeType): React.ComponentType {
  if (neType && iconMap[neType]) {
    return iconMap[neType];
  }
  return AppstoreOutlined;
}

/** 获取状态对应的颜色 */
export function getStatusColor(status?: string | number | null): string {
  if (status === 1 || status === 'ONLINE') return '#52c41a';
  if (status === 0 || status === 'OFFLINE') return '#ff4d4f';
  return '#faad14';
}
