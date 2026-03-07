package com.ecommerce.product.service.impl;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductSku;
import com.ecommerce.product.enums.ProductStatus;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("Category"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .mainImage(request.getMainImage())
                .images(request.getImages())
                .stock(0)
                .salesCount(0)
                .status(ProductStatus.DRAFT)
                .build();

        // Create SKUs
        List<ProductSku> skus = request.getSkus().stream()
                .map(skuReq -> ProductSku.builder()
                        .product(product)
                        .skuCode(skuReq.getSkuCode())
                        .attributes(skuReq.getAttributes())
                        .price(skuReq.getPrice())
                        .stock(skuReq.getStock())
                        .status("ACTIVE")
                        .build())
                .collect(Collectors.toList());

        product.setSkus(skus);
        product.setStock(skus.stream().mapToInt(ProductSku::getStock).sum());

        productRepository.save(product);
        return toResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Product"));
        return toResponse(product);
    }

    @Override
    public Page<ProductResponse> listProducts(int page, int size, Long categoryId, String sort) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page - 1, size, sorting);

        Page<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable);
        } else {
            products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }

        return products.map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return productRepository.searchByKeyword(keyword, ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Product"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("Category"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());

        productRepository.save(product);
        return toResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Product"));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    private Sort parseSort(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "sales" -> Sort.by(Sort.Direction.DESC, "salesCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private ProductResponse toResponse(Product product) {
        List<ProductResponse.SkuResponse> skuResponses = null;
        if (product.getSkus() != null) {
            skuResponses = product.getSkus().stream()
                    .map(sku -> ProductResponse.SkuResponse.builder()
                            .id(sku.getId())
                            .skuCode(sku.getSkuCode())
                            .attributes(sku.getAttributes())
                            .price(sku.getPrice())
                            .stock(sku.getStock())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stock(product.getStock())
                .salesCount(product.getSalesCount())
                .mainImage(product.getMainImage())
                .images(product.getImages())
                .status(product.getStatus().name())
                .skus(skuResponses)
                .createdAt(product.getCreatedAt())
                .build();
    }
}