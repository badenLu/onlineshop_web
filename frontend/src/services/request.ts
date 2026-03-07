import axios from 'axios';
import { message } from 'antd';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '/api',
    timeout: 10000,
});

// Request: inject JWT token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Response: unwrap data, handle 401
api.interceptors.response.use(
    (res) => res.data,
    async (err) => {
        if (err.response?.status === 401) {
            const refreshToken = localStorage.getItem('refresh_token');
            if (refreshToken) {
                try {
                    const res = await axios.post('/api/auth/refresh', null, {
                        headers: { 'X-Refresh-Token': refreshToken },
                    });
                    const { accessToken, refreshToken: newRefresh } = res.data.data;
                    localStorage.setItem('access_token', accessToken);
                    localStorage.setItem('refresh_token', newRefresh);
                    err.config.headers.Authorization = `Bearer ${accessToken}`;
                    return api(err.config);
                } catch {
                    localStorage.clear();
                    window.location.href = '/login';
                }
            } else {
                localStorage.clear();
                window.location.href = '/login';
            }
        }
        message.error(err.response?.data?.message || 'Request failed');
        return Promise.reject(err);
    }
);

export default api;