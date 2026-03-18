import { useEffect, useState } from 'react';
import {Card, Row, Col, Tag, Spin, Input, Select, Pagination, message, Button} from 'antd';
import { ShoppingCartOutlined } from '@ant-design/icons';
import { productApi } from '../services/product';
import { cartApi } from '../services/cart';

export default function ProductListPage() {
  const [products, setProducts] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [sort, setSort] = useState<string>('');

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res: any = keyword
        ? await productApi.search(keyword, page)
        : await productApi.list({ page, size: 12, sort: sort || undefined });
      setProducts(res.data.content || []);
      setTotal(res.data.totalElements || 0);
    } catch {
      // handled by interceptor
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchProducts(); }, [page, sort]);



  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16, display: 'flex', gap: 12 }}>
        <Input.Search
          placeholder="Search products..."
          onSearch={(v) => { setKeyword(v); setPage(1); fetchProducts(); }}
          style={{ width: 300 }}
          allowClear
        />
        <Select
          placeholder="Sort by"
          style={{ width: 160 }}
          onChange={(v) => setSort(v)}
          allowClear
          options={[
            { label: 'Newest', value: '' },
            { label: 'Price: Low → High', value: 'price_asc' },
            { label: 'Price: High → Low', value: 'price_desc' },
            { label: 'Best Selling', value: 'sales' },
          ]}
        />
      </div>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          {products.map((p: any) => (
            <Col key={p.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                    hoverable
                    cover={<img alt={p.name} src={p.mainImage} style={{ height: 200, objectFit: 'cover' }} />}>
                    <Card.Meta
                        title={p.name}
                        description={
                            <div>
                                <span style={{ color: '#f5222d', fontSize: 18, fontWeight: 'bold' }}>€{p.price}</span>
                                {p.originalPrice && (
                                    <span style={{ textDecoration: 'line-through', color: '#999', marginLeft: 8 }}>€{p.originalPrice}</span>
                                )}
                                <div style={{ marginTop: 4 }}>
                                    <Tag color="blue">{p.categoryName}</Tag>
                                    <Tag color="green">Sales: {p.salesCount}</Tag>
                                </div>
                                <Button
                                    type="primary"
                                    icon={<ShoppingCartOutlined />}
                                    size="small"
                                    style={{ marginTop: 8 }}
                                    onClick={async (e) => {
                                        e.stopPropagation();
                                        try {
                                            // Get product detail to find first SKU
                                            const detail: any = await productApi.detail(p.id);
                                            const firstSku = detail.data?.skus?.[0];
                                            if (firstSku) {
                                                await cartApi.add(firstSku.id, 1);
                                                message.success('Added to cart');
                                            } else {
                                                message.warning('No SKU available');
                                            }
                                        } catch {
                                            // handled by interceptor
                                        }
                                    }}>
                                    Add to Cart
                                </Button>
                            </div>
                        }
                    />
                </Card>
            </Col>
          ))}
        </Row>
      </Spin>

      <div style={{ textAlign: 'center', marginTop: 24 }}>
        <Pagination current={page} total={total} pageSize={12} onChange={setPage} />
      </div>
    </div>
  );
}