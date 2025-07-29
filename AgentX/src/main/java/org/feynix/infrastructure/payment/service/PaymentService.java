package org.feynix.infrastructure.payment.service;

import org.feynix.infrastructure.payment.constant.PaymentMethod;
import org.feynix.infrastructure.payment.model.PaymentCallback;
import org.feynix.infrastructure.payment.model.PaymentRequest;
import org.feynix.infrastructure.payment.model.PaymentResult;

import java.util.List;
import java.util.Map;

/** 支付服务统一接口 */
public interface PaymentService {
    
    /** 创建支付
     * 
     * @param paymentMethod 支付方式
     * @param request 支付请求
     * @return 支付结果 */
    PaymentResult createPayment(PaymentMethod paymentMethod, PaymentRequest request);
    
    /** 查询支付状态
     * 
     * @param paymentMethod 支付方式
     * @param providerOrderId 第三方订单ID
     * @return 支付结果 */
    PaymentResult queryPayment(PaymentMethod paymentMethod, String providerOrderId);
    
    /** 处理支付回调
     * 
     * @param paymentMethod 支付方式
     * @param callbackData 回调数据
     * @return 支付回调对象 */
    PaymentCallback handleCallback(PaymentMethod paymentMethod, Map<String, Object> callbackData);
    
    /** 取消支付
     * 
     * @param paymentMethod 支付方式
     * @param providerOrderId 第三方订单ID
     * @return 支付结果 */
    PaymentResult cancelPayment(PaymentMethod paymentMethod, String providerOrderId);
    
    /** 申请退款
     * 
     * @param paymentMethod 支付方式
     * @param providerOrderId 第三方订单ID
     * @param refundAmount 退款金额（元）
     * @param refundReason 退款原因
     * @return 支付结果 */
    PaymentResult refundPayment(PaymentMethod paymentMethod, String providerOrderId, String refundAmount, String refundReason);
    
    /** 获取支持的支付方式列表
     * 
     * @return 支付方式列表 */
    List<PaymentMethod> getSupportedPaymentMethods();
    
    /** 检查支付方式是否支持
     * 
     * @param paymentMethod 支付方式
     * @return 是否支持 */
    boolean isPaymentMethodSupported(PaymentMethod paymentMethod);
    
    /** 检查支付方式是否支持特定功能
     * 
     * @param paymentMethod 支付方式
     * @param feature 功能名称（query、cancel、refund等）
     * @return 是否支持 */
    boolean supportsFeature(PaymentMethod paymentMethod, String feature);
    
    /** 获取回调响应内容
     * 
     * @param paymentMethod 支付方式
     * @param success 处理是否成功
     * @return 响应内容 */
    String getCallbackResponse(PaymentMethod paymentMethod, boolean success);
}