package org.feynix.infrastructure.payment.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.feynix.infrastructure.payment.constant.PaymentMethod;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.infrastructure.payment.factory.PaymentProviderFactory;
import org.feynix.infrastructure.payment.model.PaymentCallback;
import org.feynix.infrastructure.payment.model.PaymentRequest;
import org.feynix.infrastructure.payment.model.PaymentResult;
import org.feynix.infrastructure.payment.provider.PaymentProvider;
import org.feynix.infrastructure.payment.service.PaymentService;

import java.util.List;
import java.util.Map;

/** 支付服务默认实现 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    private final PaymentProviderFactory providerFactory;
    
    public PaymentServiceImpl(PaymentProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }
    
    @Override
    public PaymentResult createPayment(PaymentMethod paymentMethod, PaymentRequest request) {
        logger.info("创建支付: method={}, orderId={}, amount={}", 
                paymentMethod, request.getOrderId(), request.getAmount());
        
        try {
            // 验证请求参数
            request.validate();
            
            // 获取支付提供商
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            
            // 创建支付
            PaymentResult result = provider.createPayment(request);
            
            if (result.isSuccess()) {
                logger.info("支付创建成功: method={}, orderId={}, providerOrderId={}", 
                        paymentMethod, request.getOrderId(), result.getProviderOrderId());
            } else {
                logger.warn("支付创建失败: method={}, orderId={}, error={}", 
                        paymentMethod, request.getOrderId(), result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("创建支付异常: method={}, orderId={}", paymentMethod, request.getOrderId(), e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }
    
    @Override
    public PaymentResult queryPayment(PaymentMethod paymentMethod, String providerOrderId) {
        logger.info("查询支付状态: method={}, providerOrderId={}", paymentMethod, providerOrderId);
        
        try {
            if (providerOrderId == null || providerOrderId.trim().isEmpty()) {
                throw new BusinessException("第三方订单ID不能为空");
            }
            
            // 获取支付提供商
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            
            // 查询支付状态
            PaymentResult result = provider.queryPayment(providerOrderId);
            
            logger.info("支付状态查询完成: method={}, providerOrderId={}, success={}", 
                    paymentMethod, providerOrderId, result.isSuccess());
            
            return result;
            
        } catch (Exception e) {
            logger.error("查询支付状态异常: method={}, providerOrderId={}", paymentMethod, providerOrderId, e);
            return PaymentResult.failure("QUERY_ERROR", "查询支付状态失败");
        }
    }
    
    @Override
    public PaymentCallback handleCallback(PaymentMethod paymentMethod, Map<String, Object> callbackData) {
        logger.info("处理支付回调: method={}, dataSize={}", paymentMethod, 
                callbackData != null ? callbackData.size() : 0);
        
        try {
            if (callbackData == null || callbackData.isEmpty()) {
                throw new BusinessException("回调数据不能为空");
            }
            
            // 获取支付提供商
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            
            // 处理支付回调
            PaymentCallback callback = provider.handleCallback(callbackData);
            
            if (callback.isValid()) {
                logger.info("支付回调处理成功: method={}, orderNo={}, success={}", 
                        paymentMethod, callback.getOrderNo(), callback.isPaymentSuccess());
            } else if (callback.isInvalid()) {
                logger.warn("支付回调验签失败: method={}, orderNo={}", 
                        paymentMethod, callback.getOrderNo());
            } else {
                logger.info("支付回调处理完成: method={}, orderNo={}, success={}", 
                        paymentMethod, callback.getOrderNo(), callback.isPaymentSuccess());
            }
            
            return callback;
            
        } catch (Exception e) {
            logger.error("处理支付回调异常: method={}", paymentMethod, e);
            
            // 返回无效回调对象
            PaymentCallback errorCallback = new PaymentCallback();
            errorCallback.setSignatureValid(false);
            errorCallback.setErrorMessage("回调处理异常: " + e.getMessage());
            return errorCallback;
        }
    }
    
    @Override
    public PaymentResult cancelPayment(PaymentMethod paymentMethod, String providerOrderId) {
        logger.info("取消支付: method={}, providerOrderId={}", paymentMethod, providerOrderId);
        
        try {
            if (providerOrderId == null || providerOrderId.trim().isEmpty()) {
                throw new BusinessException("第三方订单ID不能为空");
            }
            
            // 获取支付提供商
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            
            // 检查是否支持取消支付
            if (!provider.supportsFeature("cancel")) {
                return PaymentResult.failure("NOT_SUPPORTED", "该支付方式不支持取消支付");
            }
            
            // 取消支付
            PaymentResult result = provider.cancelPayment(providerOrderId);
            
            if (result.isSuccess()) {
                logger.info("支付取消成功: method={}, providerOrderId={}", paymentMethod, providerOrderId);
            } else {
                logger.warn("支付取消失败: method={}, providerOrderId={}, error={}", 
                        paymentMethod, providerOrderId, result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("取消支付异常: method={}, providerOrderId={}", paymentMethod, providerOrderId, e);
            return PaymentResult.failure("CANCEL_ERROR", "取消支付失败");
        }
    }
    
    @Override
    public PaymentResult refundPayment(PaymentMethod paymentMethod, String providerOrderId, String refundAmount, String refundReason) {
        logger.info("申请退款: method={}, providerOrderId={}, amount={}", 
                paymentMethod, providerOrderId, refundAmount);
        
        try {
            if (providerOrderId == null || providerOrderId.trim().isEmpty()) {
                throw new BusinessException("第三方订单ID不能为空");
            }
            if (refundAmount == null || refundAmount.trim().isEmpty()) {
                throw new BusinessException("退款金额不能为空");
            }
            
            // 获取支付提供商
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            
            // 检查是否支持退款
            if (!provider.supportsFeature("refund")) {
                return PaymentResult.failure("NOT_SUPPORTED", "该支付方式不支持退款");
            }
            
            // 申请退款
            PaymentResult result = provider.refundPayment(providerOrderId, refundAmount, refundReason);
            
            if (result.isSuccess()) {
                logger.info("退款申请成功: method={}, providerOrderId={}, amount={}", 
                        paymentMethod, providerOrderId, refundAmount);
            } else {
                logger.warn("退款申请失败: method={}, providerOrderId={}, amount={}, error={}", 
                        paymentMethod, providerOrderId, refundAmount, result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("申请退款异常: method={}, providerOrderId={}, amount={}", 
                    paymentMethod, providerOrderId, refundAmount, e);
            return PaymentResult.failure("REFUND_ERROR", "申请退款失败");
        }
    }
    
    @Override
    public List<PaymentMethod> getSupportedPaymentMethods() {
        return providerFactory.getAvailablePaymentMethods();
    }
    
    @Override
    public boolean isPaymentMethodSupported(PaymentMethod paymentMethod) {
        return providerFactory.isAvailable(paymentMethod);
    }
    
    @Override
    public boolean supportsFeature(PaymentMethod paymentMethod, String feature) {
        return providerFactory.supportsFeature(paymentMethod, feature);
    }
    
    @Override
    public String getCallbackResponse(PaymentMethod paymentMethod, boolean success) {
        try {
            PaymentProvider provider = providerFactory.getProvider(paymentMethod);
            return provider.getCallbackResponse(success);
        } catch (Exception e) {
            logger.error("获取回调响应异常: method={}, success={}", paymentMethod, success, e);
            return success ? "SUCCESS" : "FAILURE";
        }
    }
}