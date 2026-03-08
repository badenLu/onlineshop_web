import { useEffect, useState } from 'react';
import { Button, Card, List, message, Result } from 'antd';
import { cartApi } from '../services/cart';
import { orderApi } from '../services/order';
import { useNavigate } from 'react-router-dom';

export default function CheckoutPage() {
    const [items, setItems] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [orderNo, setOrderNo] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        cartApi.get().then((res: any) => setItems(res.data || []));
    }, []);

    const total = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const freight = total >= 50 ? 0 : 4.99;

    const placeOrder = async () => {
        setLoading(true);
        try {
            const res: any = await orderApi.create({
                addressId: 1,  // Use default address from seed data
                items: items.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
                remark: '',
            });
            setOrderNo(res.data.orderNo);
            setSuccess(true);
            message.success('Order placed successfully!');
        } catch {
            // handled by interceptor
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div style={{ padding: 48 }}>
                <Result
                    status="success"
                    title="Order Placed Successfully!"
                    subTitle={`Order No: ${orderNo}`}
                    extra={[
                        <Button type="primary" key="orders" onClick={() => navigate('/orders')}>
                            View My Orders
                        </Button>,
                        <Button key="shop" onClick={() => navigate('/')}>
                            Continue Shopping
                        </Button>,
                    ]}
                />
            </div>
        );
    }

    return (
        <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
            <h2>Checkout</h2>

            <Card title="Shipping Address" style={{ marginBottom: 16 }}>
                <p><b>Admin User</b></p>
                <p>+49 170 1234567</p>
                <p>Baden-Württemberg, Stuttgart, Mitte, Königstraße 1, 70173</p>
            </Card>

            <Card title="Order Items" style={{ marginBottom: 16 }}>
                <List
                    dataSource={items}
                    renderItem={(item: any) => (
                        <List.Item extra={<b>€{(item.price * item.quantity).toFixed(2)}</b>}>
                            <List.Item.Meta
                                title={item.productName}
                                description={`${item.skuCode} × ${item.quantity}`}
                            />
                        </List.Item>
                    )}
                />
            </Card>

            <Card>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                    <span>Subtotal:</span><span>€{total.toFixed(2)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                    <span>Shipping:</span><span>{freight === 0 ? 'Free' : `€${freight.toFixed(2)}`}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 20, fontWeight: 'bold', borderTop: '1px solid #eee', paddingTop: 12 }}>
                    <span>Total:</span><span>€{(total + freight).toFixed(2)}</span>
                </div>
                <Button type="primary" size="large" block style={{ marginTop: 16 }} loading={loading} onClick={placeOrder}>
                    Place Order
                </Button>
            </Card>
        </div>
    );
}