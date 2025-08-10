package org.feynix.domain.agent.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.feynix.domain.agent.model.AgentEmbedEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

import java.util.List;

/** Agent嵌入配置仓储接口 */
@Mapper
public interface AgentEmbedRepository extends MyBatisPlusExtRepository<AgentEmbedEntity> {


}