import { useEffect, useState } from 'react';
import { Table, Tag, Button, Space, Tabs, message } from 'antd';
import { orderApi } from '../services/order';

const STATUS_COLORS: Record<string, string> = {
  PENDING_PAYMENT: 'orange',
  PAID: 'blue',
  SHIPPED: 'cyan',
  DELIVERED: 'geekblue',
  COMPLETED: 'green',
  CANCELLED: 'red',
};

export default function OrderListPage() {
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<string>('');

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res: any = await orderApi.list({ status: status || undefined, page: 1, size: 20 });
      setOrders(res.data?.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchOrders(); }, [status]);

  const cancel = async (orderNo: string) => {
    await orderApi.cancel(orderNo);
    message.success('Order cancelled');
    fetchOrders();
  };

  const pay = async (orderNo: string) => {
    await orderApi.pay(orderNo, 'CREDIT_CARD');
    message.success('Payment successful');
    fetchOrders();
  };

  const confirm = async (orderNo: string) => {
    await orderApi.confirm(orderNo);
    message.success('Receipt confirmed');
    fetchOrders();
  };

  const columns = [
    { title: 'Order No', dataIndex: 'orderNo' },
    { title: 'Amount', dataIndex: 'payAmount', render: (v: number) => `€${v.toFixed(2)}` },
    {
      title: 'Status',
      dataIndex: 'status',
      render: (s: string) => <Tag color={STATUS_COLORS[s] || 'default'}>{s.replace(/_/g, ' ')}</Tag>,
    },
    { title: 'Created', dataIndex: 'createdAt' },
    {
      title: 'Actions',
      render: (_: any, r: any) => (
        <Space>
          {r.status === 'PENDING_PAYMENT' && (
            <>
              <Button type="primary" size="small" onClick={() => pay(r.orderNo)}>Pay</Button>
              <Button size="small" danger onClick={() => cancel(r.orderNo)}>Cancel</Button>
            </>
          )}
          {r.status === 'DELIVERED' && (
            <Button size="small" onClick={() => confirm(r.orderNo)}>Confirm Receipt</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h2>My Orders</h2>
      <Tabs
        onChange={(key) => setStatus(key)}
        items={[
          { key: '', label: 'All' },
          { key: 'PENDING_PAYMENT', label: 'Pending Payment' },
          { key: 'PAID', label: 'Paid' },
          { key: 'SHIPPED', label: 'Shipped' },
          { key: 'COMPLETED', label: 'Completed' },
        ]}
      />
      <Table dataSource={orders} columns={columns} rowKey="orderNo" loading={loading} />
    </div>
  );
}