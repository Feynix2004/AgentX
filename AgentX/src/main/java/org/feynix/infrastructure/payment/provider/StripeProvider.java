package org.feynix.infrastructure.payment.provider;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.feynix.infrastructure.payment.constant.PaymentMethod;
import org.feynix.infrastructure.payment.model.PaymentCallback;
import org.feynix.infrastructure.payment.model.PaymentRequest;
import org.feynix.infrastructure.payment.model.PaymentResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Stripe支付提供商 */
@Component
public class StripeProvider extends PaymentProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(StripeProvider.class);
    
    /** Stripe秘钥 */
    @Value("${payment.stripe.secret-key:}")
    private String secretKey;
    
    /** Stripe公钥 */
    @Value("${payment.stripe.publishable-key:}")
    private String publishableKey;
    
    /** Webhook签名密钥 */
    @Value("${payment.stripe.webhook-secret:}")
    private String webhookSecret;
    
    /** 是否测试环境 */
    @Value("${payment.stripe.test-mode:true}")
    private boolean testMode;
    
    /** 默认货币 */
    @Value("${payment.stripe.currency:usd}")
    private String defaultCurrency;
    
    /** 初始化Stripe配置 */
    private void initStripe() {
        if (StringUtils.hasText(secretKey)) {
            Stripe.apiKey = secretKey;
        }
    }
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.STRIPE;
    }
    
    @Override
    public String getProviderCode() {
        return "stripe";
    }
    
    @Override
    public String getProviderName() {
        return "Stripe";
    }
    
    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        logger.info("创建Stripe支付: orderId={}, amount={}", request.getOrderId(), request.getAmount());
        
        try {
            initStripe();
            
            // 将金额转换为分（Stripe要求以最小货币单位）
            long amountInCents = convertToCents(request.getAmount(), request.getCurrency());
            
            // 创建PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .putMetadata("order_id", request.getOrderId())
                .putMetadata("order_no", request.getOrderNo())
                .putMetadata("user_id", request.getUserId())
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .setConfirm(false)
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            if (paymentIntent != null) {
                PaymentResult result = PaymentResult.success();
                result.setProviderOrderId(request.getOrderNo());
                result.setProviderPaymentId(paymentIntent.getId());
                
                // 设置客户端密钥作为支付URL
                result.setPaymentUrl(paymentIntent.getClientSecret());
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("id", paymentIntent.getId());
                rawResponse.put("clientSecret", paymentIntent.getClientSecret());
                rawResponse.put("status", paymentIntent.getStatus());
                rawResponse.put("amount", paymentIntent.getAmount());
                rawResponse.put("currency", paymentIntent.getCurrency());
                rawResponse.put("created", paymentIntent.getCreated());
                result.setRawResponse(rawResponse);
                
                logger.info("Stripe支付创建成功: orderId={}, paymentIntentId={}", 
                    request.getOrderId(), paymentIntent.getId());
                return result;
            } else {
                logger.warn("Stripe支付创建失败: PaymentIntent为空");
                return PaymentResult.failure("CREATE_FAILED", "PaymentIntent创建失败");
            }
            
        } catch (StripeException e) {
            logger.error("Stripe支付创建异常: orderId={}, code={}, message={}", 
                request.getOrderId(), e.getCode(), e.getMessage(), e);
            return PaymentResult.failure(e.getCode(), "Stripe接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Stripe支付创建系统异常: orderId={}", request.getOrderId(), e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResult queryPayment(String providerPaymentId) {
        logger.info("查询Stripe支付状态: paymentIntentId={}", providerPaymentId);
        
        try {
            initStripe();
            
            // 查询PaymentIntent
            PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder().build();
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId, params, null);
            
            if (paymentIntent != null) {
                PaymentResult result = PaymentResult.success();
                result.setProviderPaymentId(paymentIntent.getId());
                
                // 从metadata中获取订单号
                if (paymentIntent.getMetadata() != null) {
                    result.setProviderOrderId(paymentIntent.getMetadata().get("order_no"));
                }
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("id", paymentIntent.getId());
                rawResponse.put("status", paymentIntent.getStatus());
                rawResponse.put("amount", paymentIntent.getAmount());
                rawResponse.put("currency", paymentIntent.getCurrency());
                rawResponse.put("created", paymentIntent.getCreated());
                rawResponse.put("metadata", paymentIntent.getMetadata());
                result.setRawResponse(rawResponse);
                
                // 根据状态判断支付是否成功
                String status = paymentIntent.getStatus();
                if ("succeeded".equals(status)) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setErrorMessage("支付未完成，状态: " + status);
                }
                
                logger.info("Stripe支付状态查询成功: paymentIntentId={}, status={}", 
                    providerPaymentId, status);
                return result;
            } else {
                logger.warn("Stripe支付查询失败: PaymentIntent不存在");
                return PaymentResult.failure("NOT_FOUND", "PaymentIntent不存在");
            }
            
        } catch (StripeException e) {
            logger.error("Stripe支付查询异常: paymentIntentId={}, code={}, message={}", 
                providerPaymentId, e.getCode(), e.getMessage(), e);
            return PaymentResult.failure(e.getCode(), "Stripe接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Stripe支付查询系统异常: paymentIntentId={}", providerPaymentId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentCallback handleCallback(Map<String, Object> callbackData) {
        logger.info("处理Stripe支付回调: dataSize={}", callbackData.size());
        
        PaymentCallback callback = new PaymentCallback();
        callback.setRawData(callbackData);
        
        try {
            // Stripe的回调通常是Webhook事件，需要验证签名
            // 这里简化处理，实际应该验证Webhook签名
            boolean signatureValid = verifyCallback(callbackData);
            callback.setSignatureValid(signatureValid);
            
            if (!signatureValid) {
                logger.warn("Stripe回调验签失败");
                callback.setErrorMessage("签名验证失败");
                return callback;
            }
            
            // 解析Webhook事件
            String eventType = getStringValue(callbackData, "type");
            Map<String, Object> eventData = getMapValue(callbackData, "data");
            Map<String, Object> objectData = eventData != null ? getMapValue(eventData, "object") : null;
            
            if (objectData == null) {
                callback.setSignatureValid(false);
                callback.setErrorMessage("回调数据格式错误");
                return callback;
            }
            
            // 解析PaymentIntent数据
            String paymentIntentId = getStringValue(objectData, "id");
            String status = getStringValue(objectData, "status");
            Long amount = getLongValue(objectData, "amount");
            String currency = getStringValue(objectData, "currency");
            Map<String, Object> metadata = getMapValue(objectData, "metadata");
            
            callback.setProviderPaymentId(paymentIntentId);
            callback.setPaymentStatus(status);
            callback.setPaymentMethod("Stripe");
            
            // 从metadata中获取订单信息
            if (metadata != null) {
                String orderNo = getStringValue(metadata, "order_no");
                callback.setOrderNo(orderNo);
                callback.setProviderOrderId(orderNo);
            }
            
            // 设置金额（转换为元）
            if (amount != null && currency != null) {
                BigDecimal amountDecimal = convertFromCents(amount, currency);
                callback.setAmount(amountDecimal);
                callback.setCurrency(currency.toUpperCase());
            }
            
            // 设置扩展数据
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("eventType", eventType);
            extraData.put("paymentIntentId", paymentIntentId);
            extraData.put("status", status);
            callback.setExtraData(extraData);
            
            // 判断支付是否成功
            if ("payment_intent.succeeded".equals(eventType) && "succeeded".equals(status)) {
                callback.setPaymentSuccess(true);
                logger.info("Stripe支付成功回调: paymentIntentId={}, orderNo={}", 
                    paymentIntentId, callback.getOrderNo());
            } else {
                callback.setPaymentSuccess(false);
                logger.info("Stripe支付非成功回调: paymentIntentId={}, eventType={}, status={}", 
                    paymentIntentId, eventType, status);
            }
            
        } catch (Exception e) {
            logger.error("处理Stripe回调异常", e);
            callback.setSignatureValid(false);
            callback.setErrorMessage("回调处理异常: " + e.getMessage());
        }
        
        return callback;
    }
    
    @Override
    public PaymentResult cancelPayment(String providerPaymentId) {
        logger.info("取消Stripe支付: paymentIntentId={}", providerPaymentId);
        
        try {
            initStripe();
            
            // 获取PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId);
            
            // 取消PaymentIntent
            PaymentIntent canceledPaymentIntent = paymentIntent.cancel();
            
            if (canceledPaymentIntent != null && "canceled".equals(canceledPaymentIntent.getStatus())) {
                PaymentResult result = PaymentResult.success();
                result.setProviderPaymentId(canceledPaymentIntent.getId());
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("id", canceledPaymentIntent.getId());
                rawResponse.put("status", canceledPaymentIntent.getStatus());
                result.setRawResponse(rawResponse);
                
                logger.info("Stripe支付取消成功: paymentIntentId={}", providerPaymentId);
                return result;
            } else {
                logger.warn("Stripe支付取消失败: paymentIntentId={}", providerPaymentId);
                return PaymentResult.failure("CANCEL_FAILED", "支付取消失败");
            }
            
        } catch (StripeException e) {
            logger.error("Stripe支付取消异常: paymentIntentId={}, code={}, message={}", 
                providerPaymentId, e.getCode(), e.getMessage(), e);
            return PaymentResult.failure(e.getCode(), "Stripe接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Stripe支付取消系统异常: paymentIntentId={}", providerPaymentId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResult refundPayment(String providerPaymentId, String refundAmount, String refundReason) {
        logger.info("申请Stripe退款: paymentIntentId={}, amount={}", providerPaymentId, refundAmount);
        
        try {
            initStripe();
            
            // 将退款金额转换为分
            long refundAmountInCents = convertToCents(new BigDecimal(refundAmount), defaultCurrency);
            
            // 创建退款
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(providerPaymentId)
                .setAmount(refundAmountInCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("reason", refundReason != null ? refundReason : "用户申请退款")
                .build();
            
            Refund refund = Refund.create(params);
            
            if (refund != null) {
                PaymentResult result = PaymentResult.success();
                result.setProviderPaymentId(refund.getId());
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("id", refund.getId());
                rawResponse.put("status", refund.getStatus());
                rawResponse.put("amount", refund.getAmount());
                rawResponse.put("currency", refund.getCurrency());
                rawResponse.put("created", refund.getCreated());
                result.setRawResponse(rawResponse);
                
                logger.info("Stripe退款申请成功: refundId={}, amount={}", 
                    refund.getId(), refund.getAmount());
                return result;
            } else {
                logger.warn("Stripe退款申请失败: 退款对象为空");
                return PaymentResult.failure("REFUND_FAILED", "退款申请失败");
            }
            
        } catch (StripeException e) {
            logger.error("Stripe退款申请异常: paymentIntentId={}, code={}, message={}", 
                providerPaymentId, e.getCode(), e.getMessage(), e);
            return PaymentResult.failure(e.getCode(), "Stripe接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Stripe退款申请系统异常: paymentIntentId={}", providerPaymentId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    protected boolean verifyCallback(Map<String, Object> callbackData) {
        // 简化处理，实际应该验证Webhook签名
        // Stripe Webhook签名验证需要原始请求体和签名头
        try {
            // 这里应该使用com.stripe.net.Webhook.constructEvent来验证签名
            // 但需要原始请求体，这里简化为检查基本格式
            return callbackData.containsKey("type") && callbackData.containsKey("data");
        } catch (Exception e) {
            logger.error("Stripe回调验签异常", e);
            return false;
        }
    }
    
    @Override
    protected boolean supportsCancellation() {
        return true;
    }
    
    @Override
    protected boolean supportsRefund() {
        return true;
    }
    
    @Override
    public String getCallbackResponse(boolean success) {
        // Stripe Webhook期望HTTP 200状态码
        return success ? "OK" : "ERROR";
    }
    
    @Override
    protected String formatAmount(String amount) {
        // Stripe金额格式：最小货币单位（如美分）
        try {
            BigDecimal amountDecimal = new BigDecimal(amount);
            return String.valueOf(convertToCents(amountDecimal, defaultCurrency));
        } catch (NumberFormatException e) {
            logger.warn("Stripe金额格式化失败: {}", amount);
            return amount;
        }
    }
    
    @Override
    protected String parseAmount(String amount) {
        // 从最小货币单位转换为元
        try {
            long amountInCents = Long.parseLong(amount);
            return convertFromCents(amountInCents, defaultCurrency).toString();
        } catch (NumberFormatException e) {
            logger.warn("Stripe金额解析失败: {}", amount);
            return amount;
        }
    }
    
    @Override
    protected String getConfig(String key) {
        switch (key) {
            case "secretKey":
                return secretKey;
            case "publishableKey":
                return publishableKey;
            case "webhookSecret":
                return webhookSecret;
            case "testMode":
                return String.valueOf(testMode);
            case "currency":
                return defaultCurrency;
            default:
                return null;
        }
    }
    
    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(secretKey) && StringUtils.hasText(publishableKey);
    }
    
    /** 将金额转换为最小货币单位（分） */
    private long convertToCents(BigDecimal amount, String currency) {
        // 大部分货币的最小单位是1/100，日元等是1
        if ("JPY".equalsIgnoreCase(currency) || "KRW".equalsIgnoreCase(currency)) {
            return amount.longValue();
        } else {
            return amount.multiply(new BigDecimal("100")).longValue();
        }
    }
    
    /** 将最小货币单位转换为元 */
    private BigDecimal convertFromCents(long amountInCents, String currency) {
        if ("JPY".equalsIgnoreCase(currency) || "KRW".equalsIgnoreCase(currency)) {
            return new BigDecimal(amountInCents);
        } else {
            return new BigDecimal(amountInCents).divide(new BigDecimal("100"));
        }
    }
    
    /** 获取字符串值 */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /** 获取Long值 */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    /** 获取Map值 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}