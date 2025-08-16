create table public.agent_widgets (
                                      id character varying(32) primary key not null,
                                      agent_id character varying(32) not null, -- Agent ID
                                      user_id character varying(32) not null, -- 创建者用户ID
                                      public_id character varying(32) not null, -- 嵌入访问的唯一ID
                                      name character varying(100) not null, -- 名称
                                      description text, -- 描述
                                      model_id character varying(32) not null, -- 指定使用的模型ID
                                      provider_id character varying(32), -- 可选：指定服务商ID
                                      allowed_domains text, -- JSON数组：允许的域名列表
                                      daily_limit integer default '-1'::integer, -- 每日调用限制（-1为无限制）
                                      enabled boolean default true, -- 是否启用
                                      created_at timestamp without time zone default CURRENT_TIMESTAMP, -- 创建时间
                                      updated_at timestamp without time zone default CURRENT_TIMESTAMP, -- 更新时间
                                      deleted_at timestamp without time zone, -- 删除时间（软删除）
                                      widget_type character varying(20) not null default 'AGENT', -- Widget类型：AGENT（Agent类型）/RAG（RAG类型）
                                      knowledge_base_ids jsonb -- RAG类型Widget专用：知识库ID列表（JSON数组格式）
);
create unique index agent_embeds_public_id_key on agent_widgets using btree (public_id);
create index idx_agent_embeds_agent_id on agent_widgets using btree (agent_id);
create index idx_agent_embeds_user_id on agent_widgets using btree (user_id);
create index idx_agent_embeds_public_id on agent_widgets using btree (public_id);
create index idx_agent_embeds_enabled on agent_widgets using btree (enabled);
comment on table public.agent_widgets is 'Agent小组件配置表，用于配置Agent的网站嵌入功能';
comment on column public.agent_widgets.agent_id is 'Agent ID';
comment on column public.agent_widgets.user_id is '创建者用户ID';
comment on column public.agent_widgets.public_id is '嵌入访问的唯一ID';
comment on column public.agent_widgets.name is '名称';
comment on column public.agent_widgets.description is '描述';
comment on column public.agent_widgets.model_id is '指定使用的模型ID';
comment on column public.agent_widgets.provider_id is '可选：指定服务商ID';
comment on column public.agent_widgets.allowed_domains is 'JSON数组：允许的域名列表';
comment on column public.agent_widgets.daily_limit is '每日调用限制（-1为无限制）';
comment on column public.agent_widgets.enabled is '是否启用';
comment on column public.agent_widgets.created_at is '创建时间';
comment on column public.agent_widgets.updated_at is '更新时间';
comment on column public.agent_widgets.deleted_at is '删除时间（软删除）';
comment on column public.agent_widgets.widget_type is 'Widget类型：AGENT（Agent类型）/RAG（RAG类型）';
comment on column public.agent_widgets.knowledge_base_ids is 'RAG类型Widget专用：知识库ID列表（JSON数组格式）';

