import { useEffect } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import { neApi } from '@/api/neApi';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import type { NeCreateRequest } from '@/types/request';

const NE_TYPE_OPTIONS = [
  { value: 'FIREWALL', label: '防火墙' },
  { value: 'SWITCH', label: '交换机' },
  { value: 'SERVER', label: '服务器' },
  { value: 'STORAGE', label: '存储设备' },
  { value: 'GATEWAY', label: '网关' },
  { value: 'CHASSIS', label: '机框' },
  { value: 'RACK', label: '机架' },
  { value: 'DEFAULT', label: '通用设备' },
];

interface Props {
  open: boolean;
  parentDn: string;
  onClose: () => void;
}

export default function NeCreateDialog({ open, parentDn, onClose }: Props) {
  const [form] = Form.useForm<NeCreateRequest>();
  const fetchTree = useTreeStore((s) => s.fetchTree);
  const refreshView = useTopologyStore((s) => s.refreshView);

  useEffect(() => {
    if (open) {
      form.resetFields();
      form.setFieldsValue({ parentDn });
    }
  }, [open, parentDn, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await neApi.create(values);
      message.success('网元创建成功');
      onClose();
      await fetchTree();
      refreshView();
    } catch {
      // error handled by interceptor
    }
  };

  return (
    <Modal
      title="新增网元"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      destroyOnClose
      width={520}
    >
      <Form form={form} layout="vertical" autoComplete="off">
        <Form.Item name="parentDn" label="所属子网" rules={[{ required: true, message: '请输入所属子网' }]}>
          <Input placeholder="所属子网DN" />
        </Form.Item>
        <Form.Item
          name="name"
          label="名称"
          rules={[
            { required: true, message: '请输入网元名称' },
            { max: 100, message: '名称最多100个字符' },
          ]}
        >
          <Input placeholder="请输入网元名称" />
        </Form.Item>
        <Form.Item name="displayName" label="显示名称">
          <Input placeholder="请输入显示名称" />
        </Form.Item>
        <Form.Item
          name="neType"
          label="网元类型"
          rules={[{ required: true, message: '请选择网元类型' }]}
        >
          <Select placeholder="请选择网元类型" options={NE_TYPE_OPTIONS} />
        </Form.Item>
        <Form.Item name="address" label="IP地址">
          <Input placeholder="请输入IP地址" />
        </Form.Item>
        <Form.Item name="location" label="位置">
          <Input placeholder="请输入位置" />
        </Form.Item>
        <Form.Item name="maintainer" label="维护人">
          <Input placeholder="请输入维护人" />
        </Form.Item>
        <Form.Item name="contact" label="联系方式">
          <Input placeholder="请输入联系方式" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
