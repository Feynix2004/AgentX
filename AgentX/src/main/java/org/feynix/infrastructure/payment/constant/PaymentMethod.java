package org.feynix.infrastructure.payment.constant;

/** 支付方式枚举 */
public enum PaymentMethod {
    
    /** 支付宝 */
    ALIPAY,
    
    /** Stripe */
    STRIPE,
    
    /** 微信支付 */
    WECHAT;
    

    /** 检查是否支持退款 */
    public boolean supportsRefund() {
        return this == ALIPAY || this == STRIPE;
    }
    
    /** 检查是否为第三方支付 */
    public boolean isThirdParty() {
        return true; // 目前所有支付方式都是第三方
    }
}