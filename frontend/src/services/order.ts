import api from './request';

export interface CreateOrderParams {
    addressId: number;
    items: { skuId: number; quantity: number }[];
    remark?: string;
}

export const orderApi = {
    create: (data: CreateOrderParams) => api.post('/orders', data),
    list: (params?: { status?: string; page?: number; size?: number }) =>
        api.get('/orders', { params }),
    detail: (orderNo: string) => api.get(`/orders/${orderNo}`),
    cancel: (orderNo: string) => api.put(`/orders/${orderNo}/cancel`),
    pay: (orderNo: string, paymentType: string) =>
        api.post(`/orders/${orderNo}/pay`, { paymentType }),
    confirm: (orderNo: string) => api.put(`/orders/${orderNo}/confirm`),
};