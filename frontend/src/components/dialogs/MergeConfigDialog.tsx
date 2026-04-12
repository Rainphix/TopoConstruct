import { useEffect, useState } from 'react';
import { Modal, Form, InputNumber, Switch, Select, Spin, message } from 'antd';
import { mergeApi } from '@/api/mergeApi';
import type { MergeConfigRequest } from '@/types/request';
import type { MergeMode } from '@/types/api';

interface Props {
  open: boolean;
  onClose: () => void;
}

export default function MergeConfigDialog({ open, onClose }: Props) {
  const [form] = Form.useForm<MergeConfigRequest>();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      setLoading(true);
      mergeApi
        .getConfig()
        .then((res) => {
          form.setFieldsValue(res.data.data);
        })
        .finally(() => setLoading(false));
    }
  }, [open, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    try {
      await mergeApi.updateConfig(values);
      message.success('配置已更新');
      onClose();
    } catch {
      // error handled by interceptor
    }
  };

  return (
    <Modal
      title="合并配置"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      destroyOnClose
      width={480}
    >
      {loading ? (
        <Spin style={{ display: 'block', margin: '40px auto' }} />
      ) : (
        <Form form={form} layout="vertical" autoComplete="off">
          <Form.Item name="enabled" label="启用合并" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item
            name="threshold"
            label="合并阈值"
            rules={[{ required: true, message: '请输入阈值' }]}
          >
            <InputNumber min={2} max={1000} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="mode"
            label="合并模式"
            rules={[{ required: true, message: '请选择模式' }]}
          >
            <Select<MergeMode>
              options={[
                { label: '相同类型', value: 'SAME_TYPE' },
                { label: '相同状态', value: 'SAME_STATUS' },
                { label: '自定义', value: 'CUSTOM' },
              ]}
            />
          </Form.Item>
        </Form>
      )}
    </Modal>
  );
}
