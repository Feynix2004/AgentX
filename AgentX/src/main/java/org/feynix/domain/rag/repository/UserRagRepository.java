package org.feynix.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.rag.model.UserRagEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/** 用户安装的RAG仓储接口
 * @author feynix
 * @date 2025-07-16 <br/>
 */
@Mapper
public interface UserRagRepository extends MyBatisPlusExtRepository<UserRagEntity> {

}