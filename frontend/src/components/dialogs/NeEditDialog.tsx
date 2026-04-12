import { useEffect, useState } from 'react';
import { Modal, Form, Input, Spin, message } from 'antd';
import { neApi } from '@/api/neApi';
import { useTreeStore } from '@/stores/useTreeStore';
import { useTopologyStore } from '@/stores/useTopologyStore';
import type { NeUpdateRequest } from '@/types/request';

interface Props {
  open: boolean;
  dn: string;
  onClose: () => void;
}

export default function NeEditDialog({ open, dn, onClose }: Props) {
  const [form] = Form.useForm<NeUpdateRequest>();
  const [loading, setLoading] = useState(false);
  const fetchTree = useTreeStore((s) => s.fetchTree);
  const refreshView = useTopologyStore((s) => s.refreshView);

  useEffect(() => {
    if (open && dn) {
      setLoading(true);
      form.resetFields();
      neApi
        .getDetail(dn)
        .then((res) => {
          const detail = res.data.data;
          form.setFieldsValue({
            name: detail.name,
            displayName: detail.displayName,
            address: detail.address,
            location: detail.location,
            maintainer: detail.maintainer,
            contact: detail.contact,
          });
        })
        .finally(() => setLoading(false));
    }
  }, [open, dn, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await neApi.update(dn, values);
      message.success('网元更新成功');
      onClose();
      fetchTree();
      refreshView();
    } catch {
      // error handled by interceptor
    }
  };

  return (
    <Modal
      title="编辑网元"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      destroyOnClose
      width={520}
    >
      {loading ? (
        <Spin style={{ display: 'block', margin: '40px auto' }} />
      ) : (
        <Form form={form} layout="vertical" autoComplete="off">
          <Form.Item label="DN">
            <Input value={dn} disabled />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ max: 100, message: '名称最多100个字符' }]}>
            <Input placeholder="请输入网元名称" />
          </Form.Item>
          <Form.Item name="displayName" label="显示名称">
            <Input placeholder="请输入显示名称" />
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
      )}
    </Modal>
  );
}
