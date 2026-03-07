import { BrowserRouter, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Layout, Menu, Button, Space } from 'antd';
import { ShoppingCartOutlined, UserOutlined, LogoutOutlined, ShopOutlined } from '@ant-design/icons';
import LoginPage from './pages/LoginPage';
import ProductListPage from './pages/ProductListPage';
import CartPage from './pages/CartPage';
import OrderListPage from './pages/OrderListPage';

const { Header, Content } = Layout;

function AppLayout() {
    const navigate = useNavigate();
    const token = localStorage.getItem('access_token');

    const logout = () => {
        localStorage.clear();
        navigate('/login');
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Link to="/" style={{ color: '#fff', fontSize: 20, fontWeight: 'bold' }}>
                    🛒 OnlineShop
                </Link>
                <Space>
                    <Menu theme="dark" mode="horizontal" selectable={false} items={[
                        { key: 'products', icon: <ShopOutlined />, label: <Link to="/">Products</Link> },
                        { key: 'cart', icon: <ShoppingCartOutlined />, label: <Link to="/cart">Cart</Link> },
                        { key: 'orders', icon: <UserOutlined />, label: <Link to="/orders">Orders</Link> },
                    ]} />
                    {token ? (
                        <Button icon={<LogoutOutlined />} onClick={logout} type="text" style={{ color: '#fff' }}>
                            Logout
                        </Button>
                    ) : (
                        <Button type="primary" onClick={() => navigate('/login')}>Login</Button>
                    )}
                </Space>
            </Header>
            <Content>
                <Routes>
                    <Route path="/" element={<ProductListPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/orders" element={<OrderListPage />} />
                </Routes>
            </Content>
        </Layout>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/*" element={<AppLayout />} />
            </Routes>
        </BrowserRouter>
    );
}