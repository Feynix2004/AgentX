package org.feynix.domain.rag.straegy.context;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.feynix.domain.rag.model.enums.RagDocSyncOcrEnum;
import org.feynix.domain.rag.straegy.RagDocSyncOcrStrategy;
import org.feynix.infrastructure.enums.IBaseEnum;

import jakarta.annotation.Resource;

/**
 * @author shilong.zang
 * @date 09:39 <br/>
 */
@Service
public class RagDocSyncOcrContext {


    @Resource
    private Map<String, RagDocSyncOcrStrategy> taskExportStrategyMap;

    public RagDocSyncOcrStrategy getTaskExportStrategy(String strategy) {
        return taskExportStrategyMap.get(IBaseEnum.getLabelByValue(strategy, RagDocSyncOcrEnum.class));
    }
}
