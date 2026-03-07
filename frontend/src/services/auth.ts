import api from './request';

export interface LoginParams {
    email: string;
    password: string;
}

export interface RegisterParams {
    username: string;
    email: string;
    password: string;
}

export const authApi = {
    login: (data: LoginParams) => api.post('/auth/login', data),
    register: (data: RegisterParams) => api.post('/auth/register', data),
    logout: () => api.post('/auth/logout'),
};