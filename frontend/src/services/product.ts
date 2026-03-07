import api from './request';

export const productApi = {
    list: (params?: { page?: number; size?: number; categoryId?: number; sort?: string }) =>
        api.get('/products', { params }),
    detail: (id: number) => api.get(`/products/${id}`),
    search: (q: string, page = 1) => api.get('/products/search', { params: { q, page } }),
    categories: () => api.get('/categories'),
};