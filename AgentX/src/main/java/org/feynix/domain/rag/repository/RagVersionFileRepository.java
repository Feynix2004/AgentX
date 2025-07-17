package org.feynix.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rag.model.RagVersionFileEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** RAG版本文件仓储接口
 * @author feynix
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface RagVersionFileRepository extends MyBatisPlusExtRepository<RagVersionFileEntity> {

}