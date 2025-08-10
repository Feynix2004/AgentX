-- Agent嵌入配置表
CREATE TABLE agent_embeds (
                              id VARCHAR(32) PRIMARY KEY,
                              agent_id VARCHAR(32) NOT NULL,
                              user_id VARCHAR(32) NOT NULL,
                              public_id VARCHAR(32) UNIQUE NOT NULL,

    -- 嵌入配置
                              embed_name VARCHAR(100) NOT NULL,
                              embed_description TEXT,

    -- 模型配置
                              model_id VARCHAR(32) NOT NULL,
                              provider_id VARCHAR(32),

    -- 访问控制
                              allowed_domains TEXT,
                              daily_limit_limit INTEGER DEFAULT -1,
                              enabled BOOLEAN DEFAULT TRUE,

    -- 元数据
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              deleted_at TIMESTAMP NULL
);

-- 添加字段注释
COMMENT ON COLUMN agent_embeds.agent_id IS 'Agent ID';
COMMENT ON COLUMN agent_embeds.user_id IS '创建者用户ID';
COMMENT ON COLUMN agent_embeds.public_id IS '嵌入访问的唯一ID';
COMMENT ON COLUMN agent_embeds.embed_name IS '嵌入名称';
COMMENT ON COLUMN agent_embeds.embed_description IS '嵌入描述';
COMMENT ON COLUMN agent_embeds.model_id IS '指定使用的模型ID';
COMMENT ON COLUMN agent_embeds.provider_id IS '可选：指定服务商ID';
COMMENT ON COLUMN agent_embeds.allowed_domains IS 'JSON数组：允许的域名列表';
COMMENT ON COLUMN agent_embeds.daily_limit IS '每日调用限制（-1为无限制）';
COMMENT ON COLUMN agent_embeds.enabled IS '是否启用';
COMMENT ON COLUMN agent_embeds.created_at IS '创建时间';
COMMENT ON COLUMN agent_embeds.updated_at IS '更新时间';
COMMENT ON COLUMN agent_embeds.deleted_at IS '删除时间（软删除）';

-- 添加表注释
COMMENT ON TABLE agent_embeds IS 'Agent嵌入配置表';

-- 创建索引
CREATE INDEX idx_agent_embeds_agent_id ON agent_embeds (agent_id);
CREATE INDEX idx_agent_embeds_user_id ON agent_embeds (user_id);
CREATE INDEX idx_agent_embeds_public_id ON agent_embeds (public_id);
CREATE INDEX idx_agent_embeds_enabled ON agent_embeds (enabled);