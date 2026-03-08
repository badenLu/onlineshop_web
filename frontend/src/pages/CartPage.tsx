import { useEffect, useState } from 'react';
import { Table, Button, InputNumber, Space, message, Empty } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { cartApi } from '../services/cart';
import { useNavigate } from 'react-router-dom';

export default function CartPage() {
    const [items, setItems] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const fetchCart = async () => {
        setLoading(true);
        try {
            const res: any = await cartApi.get();
            setItems(res.data || []);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchCart(); }, []);

    const updateQty = async (skuId: number, qty: number) => {
        await cartApi.update(skuId, qty);
        fetchCart();
    };

    const remove = async (skuId: number) => {
        await cartApi.remove(skuId);
        message.success('Removed');
        fetchCart();
    };

    const total = items.reduce((sum, item) => sum + item.price * item.quantity, 0);

    const columns = [
        {
            title: 'Product',
            render: (_: any, r: any) => (
                <Space>
                    <img src={r.productImage} alt="" style={{ width: 60, height: 60, objectFit: 'cover' }} />
                    <div>
                        <div>{r.productName}</div>
                        <div style={{ color: '#999', fontSize: 12 }}>{r.skuCode}</div>
                    </div>
                </Space>
            ),
        },
        { title: 'Price', dataIndex: 'price', render: (v: number) => `€${v.toFixed(2)}` },
        {
            title: 'Quantity',
            render: (_: any, r: any) => (
                <InputNumber min={1} max={r.stock} value={r.quantity} onChange={(v) => v && updateQty(r.skuId, v)} />
            ),
        },
        { title: 'Subtotal', render: (_: any, r: any) => `€${(r.price * r.quantity).toFixed(2)}` },
        {
            title: '',
            render: (_: any, r: any) => <Button danger icon={<DeleteOutlined />} onClick={() => remove(r.skuId)} />,
        },
    ];

    if (!loading && items.length === 0) {
        return <div style={{ padding: 48, width: '100%', textAlign: 'center' }}><Empty description="Your cart is empty" /></div>;
    }
    return (
        <div style={{ padding: 24 }}>
            <h2>Shopping Cart</h2>
            <Table dataSource={items} columns={columns} rowKey="skuId" loading={loading} pagination={false} />
            <div style={{ textAlign: 'right', marginTop: 24 }}>
                <span style={{ fontSize: 20, marginRight: 24 }}>Total: <b>€{total.toFixed(2)}</b></span>
                <Button type="primary" size="large" onClick={() => navigate('/checkout')}>
                    Checkout ({items.length} items)
                </Button>
            </div>
        </div>
    );
}