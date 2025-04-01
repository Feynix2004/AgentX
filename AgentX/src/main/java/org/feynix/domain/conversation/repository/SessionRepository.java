package org.feynix.domain.conversation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.feynix.domain.conversation.model.SessionEntity;
import org.feynix.infrastructure.repository.MyBatisPlusExtRepository;

/**
 * 会话仓库接口
 */
@Mapper
public interface SessionRepository extends MyBatisPlusExtRepository<SessionEntity> {
}