package org.feynix.domain.order.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.order.model.OrderEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 订单仓储接口 */
@Mapper
public interface OrderRepository extends MyBatisPlusExtRepository<OrderEntity> {
}