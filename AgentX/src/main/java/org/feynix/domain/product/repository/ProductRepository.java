package org.feynix.domain.product.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.product.model.ProductEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 商品仓储接口
 */
@Mapper
public interface ProductRepository extends MyBatisPlusExtRepository<ProductEntity> {
}