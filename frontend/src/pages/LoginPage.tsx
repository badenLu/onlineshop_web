import { useState } from 'react';
import { Form, Input, Button, Card, Tabs, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { authApi } from '../services/auth';
import { useNavigate } from 'react-router-dom';

export default function LoginPage() {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onLogin = async (values: { email: string; password: string }) => {
        setLoading(true);
        try {
            const res: any = await authApi.login(values);
            localStorage.setItem('access_token', res.data.accessToken);
            localStorage.setItem('refresh_token', res.data.refreshToken);
            localStorage.setItem('role', res.data.role);
            message.success('Login successful');
            navigate('/');
        } catch {
            // error handled by interceptor
        } finally {
            setLoading(false);
        }
    };

    const onRegister = async (values: { username: string; email: string; password: string }) => {
        setLoading(true);
        try {
            const res: any = await authApi.register(values);
            localStorage.setItem('access_token', res.data.accessToken);
            localStorage.setItem('refresh_token', res.data.refreshToken);
            message.success('Registration successful');
            navigate('/');
        } catch {
            // error handled by interceptor
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
            <Card style={{ width: 420 }}>
                <h2 style={{ textAlign: 'center', marginBottom: 24 }}>🛒 OnlineShop</h2>
                <Tabs centered items={[
                    {
                        key: 'login',
                        label: 'Login',
                        children: (
                            <Form onFinish={onLogin}>
                                <Form.Item name="email" rules={[{ required: true, type: 'email' }]}>
                                    <Input prefix={<MailOutlined />} placeholder="Email" />
                                </Form.Item>
                                <Form.Item name="password" rules={[{ required: true }]}>
                                    <Input.Password prefix={<LockOutlined />} placeholder="Password" />
                                </Form.Item>
                                <Button type="primary" htmlType="submit" loading={loading} block>Login</Button>
                            </Form>
                        ),
                    },
                    {
                        key: 'register',
                        label: 'Register',
                        children: (
                            <Form onFinish={onRegister}>
                                <Form.Item name="username" rules={[{ required: true }]}>
                                    <Input prefix={<UserOutlined />} placeholder="Username" />
                                </Form.Item>
                                <Form.Item name="email" rules={[{ required: true, type: 'email' }]}>
                                    <Input prefix={<MailOutlined />} placeholder="Email" />
                                </Form.Item>
                                <Form.Item name="password" rules={[{ required: true, min: 6 }]}>
                                    <Input.Password prefix={<LockOutlined />} placeholder="Password (min 6 chars)" />
                                </Form.Item>
                                <Button type="primary" htmlType="submit" loading={loading} block>Register</Button>
                            </Form>
                        ),
                    },
                ]} />
            </Card>
        </div>
    );
}