package org.feynix.domain.tool.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.tool.model.ToolVersionEntity;
import org.feynix.domain.tool.model.UserToolEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

@Mapper
public interface UserToolRepository extends MyBatisPlusExtRepository<UserToolEntity> {
}
