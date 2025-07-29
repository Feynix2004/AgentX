package org.feynix.infrastructure.payment.provider;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
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

/** 支付宝支付提供商 */
@Component
public class AlipayProvider extends PaymentProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(AlipayProvider.class);
    
    /** 支付宝网关地址 */
    @Value("${payment.alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;
    
    /** 应用ID */
    @Value("${payment.alipay.app-id:}")
    private String appId;
    
    /** 应用私钥 */
    @Value("${payment.alipay.private-key:}")
    private String privateKey;
    
    /** 支付宝公钥 */
    @Value("${payment.alipay.public-key:}")
    private String alipayPublicKey;
    
    /** 签名类型 */
    @Value("${payment.alipay.sign-type:RSA2}")
    private String signType;
    
    /** 字符编码 */
    @Value("${payment.alipay.charset:UTF-8}")
    private String charset;
    
    /** 数据格式 */
    @Value("${payment.alipay.format:json}")
    private String format;
    
    /** 是否沙箱环境 */
    @Value("${payment.alipay.sandbox:true}")
    private boolean sandbox;
    
    /** 超时时间（分钟） */
    @Value("${payment.alipay.timeout:30}")
    private int timeoutMinutes;
    
    private AlipayClient alipayClient;
    
    /** 获取支付宝客户端 */
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            synchronized (this) {
                if (alipayClient == null) {
                    String actualGatewayUrl = sandbox ? 
                        "https://openapi.alipaydev.com/gateway.do" : gatewayUrl;
                    
                    alipayClient = new DefaultAlipayClient(
                        actualGatewayUrl, appId, privateKey, format, charset, alipayPublicKey, signType
                    );
                }
            }
        }
        return alipayClient;
    }
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ALIPAY;
    }
    
    @Override
    public String getProviderCode() {
        return "alipay";
    }
    
    @Override
    public String getProviderName() {
        return "支付宝";
    }
    
    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        logger.info("创建支付宝支付: orderId={}, amount={}", request.getOrderId(), request.getAmount());
        
        try {
            // 创建支付请求
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            
            // 设置回调地址
            alipayRequest.setReturnUrl(request.getSuccessUrl());
            alipayRequest.setNotifyUrl(request.getNotifyUrl());
            
            // 设置请求参数
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(request.getOrderNo());
            model.setTotalAmount(request.getAmount().toString());
            model.setSubject(request.getTitle());
            model.setBody(request.getDescription());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setTimeoutExpress(timeoutMinutes + "m");
            
            alipayRequest.setBizModel(model);
            
            // 调用支付宝API
            AlipayTradePagePayResponse response = getAlipayClient().pageExecute(alipayRequest);
            
            if (response.isSuccess()) {
                PaymentResult result = PaymentResult.success();
                result.setProviderOrderId(request.getOrderNo());
                result.setPaymentUrl(response.getBody());
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("body", response.getBody());
                rawResponse.put("code", response.getCode());
                rawResponse.put("msg", response.getMsg());
                rawResponse.put("subCode", response.getSubCode());
                rawResponse.put("subMsg", response.getSubMsg());
                result.setRawResponse(rawResponse);
                
                logger.info("支付宝支付创建成功: orderId={}, orderNo={}", 
                    request.getOrderId(), request.getOrderNo());
                return result;
            } else {
                String errorMsg = String.format("支付宝支付创建失败: %s - %s", 
                    response.getSubCode(), response.getSubMsg());
                logger.warn(errorMsg);
                return PaymentResult.failure(response.getSubCode(), errorMsg);
            }
            
        } catch (AlipayApiException e) {
            logger.error("支付宝支付创建异常: orderId={}", request.getOrderId(), e);
            return PaymentResult.failure("API_ERROR", "支付宝接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("支付宝支付创建系统异常: orderId={}", request.getOrderId(), e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResult queryPayment(String providerOrderId) {
        logger.info("查询支付宝支付状态: orderNo={}", providerOrderId);
        
        try {
            // 创建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(providerOrderId);
            request.setBizModel(model);
            
            // 调用支付宝API
            AlipayTradeQueryResponse response = getAlipayClient().execute(request);
            
            if (response.isSuccess()) {
                PaymentResult result = PaymentResult.success();
                result.setProviderOrderId(providerOrderId);
                result.setProviderPaymentId(response.getTradeNo());
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("tradeNo", response.getTradeNo());
                rawResponse.put("outTradeNo", response.getOutTradeNo());
                rawResponse.put("tradeStatus", response.getTradeStatus());
                rawResponse.put("totalAmount", response.getTotalAmount());
                rawResponse.put("buyerLogonId", response.getBuyerLogonId());
                rawResponse.put("buyerUserId", response.getBuyerUserId());
                result.setRawResponse(rawResponse);
                
                // 根据交易状态判断支付是否成功
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setErrorMessage("支付未完成，状态: " + tradeStatus);
                }
                
                logger.info("支付宝支付状态查询成功: orderNo={}, status={}", 
                    providerOrderId, tradeStatus);
                return result;
            } else {
                String errorMsg = String.format("支付宝支付查询失败: %s - %s", 
                    response.getSubCode(), response.getSubMsg());
                logger.warn(errorMsg);
                return PaymentResult.failure(response.getSubCode(), errorMsg);
            }
            
        } catch (AlipayApiException e) {
            logger.error("支付宝支付查询异常: orderNo={}", providerOrderId, e);
            return PaymentResult.failure("API_ERROR", "支付宝接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("支付宝支付查询系统异常: orderNo={}", providerOrderId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentCallback handleCallback(Map<String, Object> callbackData) {
        logger.info("处理支付宝支付回调: dataSize={}", callbackData.size());
        
        PaymentCallback callback = new PaymentCallback();
        callback.setRawData(callbackData);
        
        try {
            // 验证签名
            boolean signatureValid = verifyCallback(callbackData);
            callback.setSignatureValid(signatureValid);
            
            if (!signatureValid) {
                logger.warn("支付宝回调验签失败");
                callback.setErrorMessage("签名验证失败");
                return callback;
            }
            
            // 解析回调数据
            String orderNo = getStringValue(callbackData, "out_trade_no");
            String tradeNo = getStringValue(callbackData, "trade_no");
            String tradeStatus = getStringValue(callbackData, "trade_status");
            String totalAmount = getStringValue(callbackData, "total_amount");
            String buyerLogonId = getStringValue(callbackData, "buyer_logon_id");
            String buyerId = getStringValue(callbackData, "buyer_id");
            String gmtPayment = getStringValue(callbackData, "gmt_payment");
            
            callback.setOrderNo(orderNo);
            callback.setProviderOrderId(orderNo);
            callback.setProviderPaymentId(tradeNo);
            callback.setPaymentStatus(tradeStatus);
            callback.setPaymentMethod("支付宝");
            callback.setPaymentTime(gmtPayment);
            callback.setBuyerInfo(buyerLogonId);
            
            // 解析金额
            if (StringUtils.hasText(totalAmount)) {
                try {
                    callback.setAmount(new BigDecimal(totalAmount));
                } catch (NumberFormatException e) {
                    logger.warn("支付宝回调金额格式错误: {}", totalAmount);
                }
            }
            
            // 设置扩展数据
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("buyerId", buyerId);
            extraData.put("tradeNo", tradeNo);
            extraData.put("tradeStatus", tradeStatus);
            callback.setExtraData(extraData);
            
            // 判断支付是否成功
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                callback.setPaymentSuccess(true);
                logger.info("支付宝支付成功回调: orderNo={}, tradeNo={}", orderNo, tradeNo);
            } else {
                callback.setPaymentSuccess(false);
                logger.info("支付宝支付非成功回调: orderNo={}, status={}", orderNo, tradeStatus);
            }
            
        } catch (Exception e) {
            logger.error("处理支付宝回调异常", e);
            callback.setSignatureValid(false);
            callback.setErrorMessage("回调处理异常: " + e.getMessage());
        }
        
        return callback;
    }
    
    @Override
    public PaymentResult cancelPayment(String providerOrderId) {
        // 支付宝不支持主动取消支付
        return PaymentResult.failure("NOT_SUPPORTED", "支付宝不支持主动取消支付");
    }
    
    @Override
    public PaymentResult refundPayment(String providerOrderId, String refundAmount, String refundReason) {
        logger.info("申请支付宝退款: orderNo={}, amount={}", providerOrderId, refundAmount);
        
        try {
            // 创建退款请求
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(providerOrderId);
            model.setRefundAmount(refundAmount);
            model.setRefundReason(refundReason);
            request.setBizModel(model);
            
            // 调用支付宝API
            AlipayTradeRefundResponse response = getAlipayClient().execute(request);
            
            if (response.isSuccess()) {
                PaymentResult result = PaymentResult.success();
                result.setProviderOrderId(providerOrderId);
                
                // 设置原始响应数据
                Map<String, Object> rawResponse = new HashMap<>();
                rawResponse.put("tradeNo", response.getTradeNo());
                rawResponse.put("outTradeNo", response.getOutTradeNo());
                rawResponse.put("refundFee", response.getRefundFee());
                rawResponse.put("gmtRefundPay", response.getGmtRefundPay());
                result.setRawResponse(rawResponse);
                
                logger.info("支付宝退款申请成功: orderNo={}, refundFee={}", 
                    providerOrderId, response.getRefundFee());
                return result;
            } else {
                String errorMsg = String.format("支付宝退款申请失败: %s - %s", 
                    response.getSubCode(), response.getSubMsg());
                logger.warn(errorMsg);
                return PaymentResult.failure(response.getSubCode(), errorMsg);
            }
            
        } catch (AlipayApiException e) {
            logger.error("支付宝退款申请异常: orderNo={}", providerOrderId, e);
            return PaymentResult.failure("API_ERROR", "支付宝接口调用异常: " + e.getMessage());
        } catch (Exception e) {
            logger.error("支付宝退款申请系统异常: orderNo={}", providerOrderId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    @Override
    protected boolean verifyCallback(Map<String, Object> callbackData) {
        try {
            // 转换为支付宝SDK需要的格式
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : callbackData.entrySet()) {
                if (entry.getValue() != null) {
                    params.put(entry.getKey(), entry.getValue().toString());
                }
            }
            
            // 验证签名
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验签异常", e);
            return false;
        }
    }
    
    @Override
    protected boolean supportsRefund() {
        return true;
    }
    
    @Override
    public String getCallbackResponse(boolean success) {
        return success ? "success" : "failure";
    }
    
    @Override
    protected String formatAmount(String amount) {
        // 支付宝金额格式：元，保留两位小数
        try {
            BigDecimal amountDecimal = new BigDecimal(amount);
            return amountDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } catch (NumberFormatException e) {
            logger.warn("金额格式化失败: {}", amount);
            return amount;
        }
    }
    
    @Override
    protected String parseAmount(String amount) {
        // 支付宝金额已经是元格式，直接返回
        return amount;
    }
    
    @Override
    protected String getConfig(String key) {
        switch (key) {
            case "appId":
                return appId;
            case "privateKey":
                return privateKey;
            case "publicKey":
                return alipayPublicKey;
            case "gatewayUrl":
                return gatewayUrl;
            case "sandbox":
                return String.valueOf(sandbox);
            default:
                return null;
        }
    }
    
    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(appId) && 
               StringUtils.hasText(privateKey) && 
               StringUtils.hasText(alipayPublicKey);
    }
    
    /** 获取字符串值 */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}