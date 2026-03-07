import api from './request';

export const cartApi = {
    get: () => api.get('/cart'),
    add: (skuId: number, quantity: number) => api.post('/cart', { skuId, quantity }),
    update: (skuId: number, quantity: number) => api.put(`/cart/${skuId}`, { quantity }),
    remove: (skuId: number) => api.delete(`/cart/${skuId}`),
    clear: () => api.delete('/cart'),
};