-- 重命名agent_embeds表为agent_widgets
ALTER TABLE agent_embeds RENAME TO agent_widgets;

-- 重命名相关字段名（如果需要的话）
-- 注意：字段名embed_name, embed_description等保持不变，因为在业务上下文中仍然合理
-- embed在这里表示"嵌入配置"的含义，而widget表示整个功能模块

-- 更新注释
COMMENT ON TABLE agent_widgets IS 'Agent小组件配置表，用于配置Agent的网站嵌入功能';