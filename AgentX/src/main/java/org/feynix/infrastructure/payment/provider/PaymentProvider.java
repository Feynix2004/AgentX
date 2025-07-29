package org.feynix.infrastructure.payment.provider;

import org.feynix.infrastructure.payment.constant.PaymentMethod;
import org.feynix.infrastructure.payment.model.PaymentCallback;
import org.feynix.infrastructure.payment.model.PaymentRequest;
import org.feynix.infrastructure.payment.model.PaymentResult;

import java.util.Map;

/** 支付提供商抽象类 */
public abstract class PaymentProvider {
    
    /** 获取支付方式 */
    public abstract PaymentMethod getPaymentMethod();
    
    /** 获取提供商代码 */
    public abstract String getProviderCode();
    
    /** 获取提供商名称 */
    public abstract String getProviderName();
    
    /** 创建支付
     * 
     * @param request 支付请求
     * @return 支付结果 */
    public abstract PaymentResult createPayment(PaymentRequest request);
    
    /** 查询支付状态
     * 
     * @param providerOrderId 第三方订单ID
     * @return 支付结果 */
    public abstract PaymentResult queryPayment(String providerOrderId);
    
    /** 处理支付回调
     * 
     * @param callbackData 回调数据
     * @return 支付回调对象 */
    public abstract PaymentCallback handleCallback(Map<String, Object> callbackData);
    
    /** 取消支付
     * 
     * @param providerOrderId 第三方订单ID
     * @return 支付结果 */
    public abstract PaymentResult cancelPayment(String providerOrderId);
    
    /** 申请退款
     * 
     * @param providerOrderId 第三方订单ID
     * @param refundAmount 退款金额（元）
     * @param refundReason 退款原因
     * @return 支付结果 */
    public abstract PaymentResult refundPayment(String providerOrderId, String refundAmount, String refundReason);
    
    /** 验证回调签名
     * 
     * @param callbackData 回调数据
     * @return 是否验证通过 */
    protected abstract boolean verifyCallback(Map<String, Object> callbackData);
    
    /** 检查是否支持该功能
     * 
     * @param feature 功能名称
     * @return 是否支持 */
    public boolean supportsFeature(String feature) {
        switch (feature) {
            case "query":
                return true; // 所有提供商都支持查询
            case "cancel":
                return supportsCancellation();
            case "refund":
                return supportsRefund();
            default:
                return false;
        }
    }
    
    /** 是否支持取消支付 */
    protected boolean supportsCancellation() {
        return false; // 默认不支持，子类可重写
    }
    
    /** 是否支持退款 */
    protected boolean supportsRefund() {
        return false; // 默认不支持，子类可重写
    }
    
    /** 获取回调响应内容
     * 
     * @param success 处理是否成功
     * @return 响应内容 */
    public abstract String getCallbackResponse(boolean success);
    
    /** 格式化金额（转换为提供商要求的格式）
     * 
     * @param amount 金额（元）
     * @return 格式化后的金额 */
    protected abstract String formatAmount(String amount);
    
    /** 解析金额（从提供商格式转换为元）
     * 
     * @param amount 提供商格式的金额
     * @return 金额（元） */
    protected abstract String parseAmount(String amount);
    
    /** 获取配置参数
     * 
     * @param key 配置键
     * @return 配置值 */
    protected abstract String getConfig(String key);
    
    /** 检查配置是否完整 */
    public abstract boolean isConfigured();
    
    /** 获取支付提供商的显示信息 */
    public String getDisplayInfo() {
        return getProviderName() + " (" + getPaymentMethod() + ")";
    }
    
    @Override
    public String toString() {
        return "PaymentProvider{" +
                "method=" + getPaymentMethod() +
                ", code='" + getProviderCode() + '\'' +
                ", name='" + getProviderName() + '\'' +
                ", configured=" + isConfigured() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaymentProvider that = (PaymentProvider) obj;
        return getPaymentMethod() == that.getPaymentMethod() && 
               getProviderCode().equals(that.getProviderCode());
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(getPaymentMethod(), getProviderCode());
    }
}