import { BrowserRouter, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Button } from 'antd';
import { ShoppingCartOutlined, LogoutOutlined } from '@ant-design/icons';
import LoginPage from './pages/LoginPage';
import ProductListPage from './pages/ProductListPage';
import CartPage from './pages/CartPage';
import OrderListPage from './pages/OrderListPage';
import CheckoutPage from './pages/CheckoutPage';

function AppLayout() {
    const navigate = useNavigate();
    const token = localStorage.getItem('access_token');

    const logout = () => {
        localStorage.clear();
        navigate('/login');
    };

    return (
        <div style={{ minHeight: '100vh' }}>
            <nav style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                background: '#001529', padding: '0 24px', height: 56,
                width: '100%'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 32 }}>
                    <Link to="/" style={{ color: '#fff', fontSize: 20, fontWeight: 'bold', textDecoration: 'none' }}>
                        🛒 OnlineShop
                    </Link>
                    <Link to="/" style={{ color: '#ffffffa6', textDecoration: 'none' }}>Products</Link>
                    <Link to="/cart" style={{ color: '#ffffffa6', textDecoration: 'none' }}>
                        <ShoppingCartOutlined /> Cart
                    </Link>
                    <Link to="/orders" style={{ color: '#ffffffa6', textDecoration: 'none' }}>Orders</Link>
                </div>
                {token ? (
                    <Button icon={<LogoutOutlined />} onClick={logout} type="text" style={{ color: '#fff' }}>Logout</Button>
                ) : (
                    <Button type="primary" onClick={() => navigate('/login')}>Login</Button>
                )}
            </nav>
            <div>
                <Routes>
                    <Route path="/" element={<ProductListPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/orders" element={<OrderListPage />} />
                    <Route path="/checkout" element={<CheckoutPage />} />
                </Routes>
            </div>
        </div>
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