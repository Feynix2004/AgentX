-- 订单和支付系统数据库表创建脚本 (PostgreSQL)
-- 作者: Claude Code
-- 创建时间: 2025-07-29
-- 描述: 为AgentX创建订单管理和支付系统相关表

-- 1. 创建订单表 (orders)
-- 存储订单基本信息，支持多种订单类型和支付方式
CREATE TABLE orders (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    order_no VARCHAR(100) NOT NULL UNIQUE,
    order_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount DECIMAL(20,8) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    status INTEGER NOT NULL DEFAULT 1,
    expired_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    refunded_at TIMESTAMP,
    refund_amount DECIMAL(20,8) DEFAULT 0.00000000,
    payment_platform VARCHAR(50),
    payment_type VARCHAR(50),
    provider_order_id VARCHAR(200),
    metadata JSONB,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 创建支付记录表 (payments)
-- 存储支付渠道信息和第三方支付记录
CREATE TABLE payments (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,
    provider_order_id VARCHAR(200),
    provider_payment_id VARCHAR(200),
    amount DECIMAL(20,8) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    status INTEGER NOT NULL DEFAULT 1,
    payment_url TEXT,
    payment_data JSONB,
    callback_data JSONB,
    error_message TEXT,
    completed_at TIMESTAMP,
    expired_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 创建支付日志表 (payment_logs)
-- 记录支付流程中的关键步骤和异常信息
CREATE TABLE payment_logs (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    payment_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    log_type VARCHAR(50) NOT NULL,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    request_data JSONB,
    response_data JSONB,
    error_trace TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加索引
-- 订单表索引
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_no ON orders(order_no);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_payment_platform ON orders(payment_platform);
CREATE INDEX idx_orders_payment_type ON orders(payment_type);
CREATE INDEX idx_orders_provider_order_id ON orders(provider_order_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_expired_at ON orders(expired_at);

-- 支付记录表索引
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_payment_provider ON payments(payment_provider);
CREATE INDEX idx_payments_provider_order_id ON payments(provider_order_id);
CREATE INDEX idx_payments_provider_payment_id ON payments(provider_payment_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- 支付日志表索引
CREATE INDEX idx_payment_logs_payment_id ON payment_logs(payment_id);
CREATE INDEX idx_payment_logs_order_id ON payment_logs(order_id);
CREATE INDEX idx_payment_logs_log_type ON payment_logs(log_type);
CREATE INDEX idx_payment_logs_log_level ON payment_logs(log_level);
CREATE INDEX idx_payment_logs_created_at ON payment_logs(created_at);

-- 添加表注释
COMMENT ON TABLE orders IS '订单表，存储各种类型的订单信息和支付方式';
COMMENT ON TABLE payments IS '支付记录表，存储第三方支付渠道的支付信息';
COMMENT ON TABLE payment_logs IS '支付日志表，记录支付流程的详细日志';

-- 添加列注释
-- 订单表字段注释
COMMENT ON COLUMN orders.id IS '订单唯一ID';
COMMENT ON COLUMN orders.user_id IS '用户ID';
COMMENT ON COLUMN orders.order_no IS '订单号（唯一）';
COMMENT ON COLUMN orders.order_type IS '订单类型：RECHARGE(充值)、PURCHASE(购买)、SUBSCRIPTION(订阅)';
COMMENT ON COLUMN orders.title IS '订单标题';
COMMENT ON COLUMN orders.description IS '订单描述';
COMMENT ON COLUMN orders.amount IS '订单金额';
COMMENT ON COLUMN orders.currency IS '货币代码，默认CNY';
COMMENT ON COLUMN orders.status IS '订单状态：1-待支付，2-已支付，3-已取消，4-已退款，5-已过期';
COMMENT ON COLUMN orders.expired_at IS '订单过期时间';
COMMENT ON COLUMN orders.paid_at IS '支付完成时间';
COMMENT ON COLUMN orders.cancelled_at IS '取消时间';
COMMENT ON COLUMN orders.refunded_at IS '退款时间';
COMMENT ON COLUMN orders.refund_amount IS '退款金额';
COMMENT ON COLUMN orders.payment_platform IS '支付平台：alipay(支付宝)、wechat(微信支付)、stripe(Stripe)';
COMMENT ON COLUMN orders.payment_type IS '支付类型：web(网页支付)、qr_code(二维码支付)、mobile(移动端支付)、h5(H5支付)、mini_program(小程序支付)';
COMMENT ON COLUMN orders.provider_order_id IS '第三方支付平台的订单ID，用于查询支付状态和对账';
COMMENT ON COLUMN orders.metadata IS '订单扩展信息（JSONB格式）';

-- 支付记录表字段注释
COMMENT ON COLUMN payments.id IS '支付记录唯一ID';
COMMENT ON COLUMN payments.order_id IS '关联的订单ID';
COMMENT ON COLUMN payments.user_id IS '用户ID';
COMMENT ON COLUMN payments.payment_method IS '支付方式：ALIPAY(支付宝)、STRIPE(Stripe)、WECHAT(微信)';
COMMENT ON COLUMN payments.payment_provider IS '支付提供商：alipay、stripe、wechat_pay';
COMMENT ON COLUMN payments.provider_order_id IS '第三方支付平台的订单ID';
COMMENT ON COLUMN payments.provider_payment_id IS '第三方支付平台的支付ID';
COMMENT ON COLUMN payments.amount IS '支付金额';
COMMENT ON COLUMN payments.currency IS '货币代码';
COMMENT ON COLUMN payments.status IS '支付状态：1-创建，2-等待支付，3-支付成功，4-支付失败，5-已取消';
COMMENT ON COLUMN payments.payment_url IS '支付跳转URL';
COMMENT ON COLUMN payments.payment_data IS '支付请求数据（JSONB格式）';
COMMENT ON COLUMN payments.callback_data IS '支付回调数据（JSONB格式）';
COMMENT ON COLUMN payments.error_message IS '错误信息';
COMMENT ON COLUMN payments.completed_at IS '支付完成时间';
COMMENT ON COLUMN payments.expired_at IS '支付过期时间';

-- 支付日志表字段注释
COMMENT ON COLUMN payment_logs.id IS '日志记录唯一ID';
COMMENT ON COLUMN payment_logs.payment_id IS '关联的支付记录ID';
COMMENT ON COLUMN payment_logs.order_id IS '关联的订单ID';
COMMENT ON COLUMN payment_logs.log_type IS '日志类型：CREATE(创建)、CALLBACK(回调)、QUERY(查询)、ERROR(错误)';
COMMENT ON COLUMN payment_logs.log_level IS '日志级别：DEBUG、INFO、WARN、ERROR';
COMMENT ON COLUMN payment_logs.message IS '日志消息';
COMMENT ON COLUMN payment_logs.request_data IS '请求数据（JSONB格式）';
COMMENT ON COLUMN payment_logs.response_data IS '响应数据（JSONB格式）';
COMMENT ON COLUMN payment_logs.error_trace IS '错误堆栈信息';
COMMENT ON COLUMN payment_logs.ip_address IS '客户端IP地址';
COMMENT ON COLUMN payment_logs.user_agent IS '用户代理信息';

-- 插入初始测试数据（可选，用于开发测试）
-- 注意：生产环境部署时可以移除这些测试数据
INSERT INTO orders (id, user_id, order_no, order_type, title, description, amount, status, payment_platform, payment_type) VALUES 
('test-order-001', 'admin-user-uuid-001', 'ORD20250729001', 'RECHARGE', '账户充值', '充值100元到账户余额', 100.00000000, 1, 'alipay', 'web'),
('test-order-002', 'admin-user-uuid-001', 'ORD20250729002', 'PURCHASE', '购买服务', '购买模型调用服务包', 50.00000000, 1, 'wechat', 'qr_code');

-- 注释说明：
-- 1. 订单状态枚举：1-待支付，2-已支付，3-已取消，4-已退款，5-已过期
-- 2. 支付状态枚举：1-创建，2-等待支付，3-支付成功，4-支付失败，5-已取消
-- 3. 订单类型支持：RECHARGE(充值)、PURCHASE(购买)、SUBSCRIPTION(订阅)等
-- 4. 支付平台支持：alipay(支付宝)、wechat(微信支付)、stripe(Stripe)等
-- 5. 支付类型支持：web(网页支付)、qr_code(二维码支付)、mobile(移动端支付)、h5(H5支付)、mini_program(小程序支付)等
-- 6. 所有金额字段使用DECIMAL(20,8)确保精度
-- 7. 使用JSONB存储扩展信息，便于灵活扩展和查询
-- 8. 建立了完善的索引以支持高效查询
-- 9. 支持软删除机制（deleted_at字段）
-- 10. provider_order_id用于与第三方支付平台进行状态同步和对账