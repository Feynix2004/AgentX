-- 为agent_widgets表添加widget类型和知识库ID字段
-- 用于支持RAG类型的Widget功能

-- 添加Widget类型字段
ALTER TABLE agent_widgets 
ADD COLUMN widget_type VARCHAR(20) DEFAULT 'AGENT' NOT NULL;

-- 添加知识库ID列表字段（JSONB格式）
ALTER TABLE agent_widgets 
ADD COLUMN knowledge_base_ids JSONB;

-- 添加字段注释
COMMENT ON COLUMN agent_widgets.widget_type IS 'Widget类型：AGENT（Agent类型）/RAG（RAG类型）';
COMMENT ON COLUMN agent_widgets.knowledge_base_ids IS 'RAG类型Widget专用：知识库ID列表（JSON数组格式）';
