package org.feynix.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rag.model.RagVersionEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** RAG版本仓储接口
 * @author feynix
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface RagVersionRepository extends MyBatisPlusExtRepository<RagVersionEntity> {

}