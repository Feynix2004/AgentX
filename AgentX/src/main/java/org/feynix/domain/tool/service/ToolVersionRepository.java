package org.feynix.domain.tool.service;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.tool.model.ToolVersionEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

@Mapper
public interface ToolVersionRepository extends MyBatisPlusExtRepository<ToolVersionEntity> {

}
