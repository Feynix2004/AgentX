package org.feynix.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.tool.model.ToolEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 工具仓储接口 */
@Mapper
public interface ToolRepository extends MyBatisPlusExtRepository<ToolEntity> {
}