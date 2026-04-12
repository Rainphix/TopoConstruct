import { useEffect } from 'react';
import { Modal, Form, Input, message } from 'antd';
import { subnetApi } from '@/api/subnetApi';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import type { SubnetCreateRequest } from '@/types/request';

interface Props {
  open: boolean;
  parentDn: string;
  onClose: () => void;
}

export default function SubnetCreateDialog({ open, parentDn, onClose }: Props) {
  const [form] = Form.useForm<SubnetCreateRequest>();
  const fetchTree = useTreeStore((s) => s.fetchTree);
  const setExpandedKeys = useTreeStore((s) => s.setExpandedKeys);
  const expandedKeys = useTreeStore((s) => s.expandedKeys);
  const setSelectedKey = useTreeStore((s) => s.setSelectedKey);
  const fetchView = useTopologyStore((s) => s.fetchView);

  useEffect(() => {
    if (open) {
      form.resetFields();
      form.setFieldsValue({ parentDn });
    }
  }, [open, parentDn, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      const res = await subnetApi.create(values);
      message.success('子网创建成功');
      onClose();
      // 刷新树
      await fetchTree();
      // 展开父节点，选中父节点并刷新拓扑视图
      const newExpanded = expandedKeys.includes(values.parentDn)
        ? expandedKeys
        : [...expandedKeys, values.parentDn];
      setExpandedKeys(newExpanded);
      setSelectedKey(values.parentDn);
      fetchView(values.parentDn);
    } catch {
      // error handled by interceptor
    }
  };

  return (
    <Modal
      title="新增子网"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      destroyOnClose
      width={520}
    >
      <Form form={form} layout="vertical" autoComplete="off">
        <Form.Item name="parentDn" label="父子网" rules={[{ required: true, message: '请输入父子网DN' }]}>
          <Input placeholder="请输入父子网DN" />
        </Form.Item>
        <Form.Item
          name="name"
          label="名称"
          rules={[
            { required: true, message: '请输入名称' },
            { max: 100, message: '名称最多100个字符' },
          ]}
        >
          <Input placeholder="请输入子网名称" />
        </Form.Item>
        <Form.Item
          name="displayName"
          label="显示名称"
          rules={[{ max: 200, message: '显示名称最多200个字符' }]}
        >
          <Input placeholder="请输入显示名称" />
        </Form.Item>
        <Form.Item name="address" label="地址">
          <Input placeholder="请输入地址" />
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
