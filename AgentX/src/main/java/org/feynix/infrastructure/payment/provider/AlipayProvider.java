package org.feynix.infrastructure.payment.provider;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.feynix.domain.order.constant.PaymentPlatform;
import org.feynix.infrastructure.payment.constant.AlipayPaymentType;
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
    
    @Override
    protected boolean supportsRefund() {
        return true;
    }
    
    @Override
    protected boolean supportsCancellation() {
        return false;
    }
    
    private void initializeConfig() throws Exception {
        if (!initialized && isConfigured()) {
            synchronized (this) {
                if (!initialized) {
                    Config config = new Config();
                    config.protocol = "https";
                    config.gatewayHost = this.gatewayHost;
                    config.signType = this.signType;
                    config.appId = this.appId;
                    config.merchantPrivateKey = this.privateKey;
                    config.alipayPublicKey = this.alipayPublicKey;
                    config.notifyUrl = "";
                    
                    Factory.setOptions(config);
                    initialized = true;
                    logger.info("支付宝 EasySDK 初始化成功");
                }
            }
        } else if (!isConfigured()) {
            throw new IllegalStateException("支付宝配置不完整，请检查 app-id, private-key, alipay-public-key 配置");
        }
    }


    @Value("${alipay.app-id:}")
    private String appId;
    
    @Value("${alipay.private-key:}")
    private String privateKey;
    
    @Value("${alipay.alipay-public-key:}")
    private String alipayPublicKey;
    
    @Value("${alipay.gateway-host:openapi.alipay.com}")
    private String gatewayHost;
    
    @Value("${alipay.sign-type:RSA2}")
    private String signType;
    
    private volatile boolean initialized = false;
    
    @Override
    public PaymentPlatform getPaymentPlatform() {
        return PaymentPlatform.ALIPAY;
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
        try {
            initializeConfig();
            request.validate();
            
            // 获取支付类型，默认为WEB支付
            String paymentType = request.getPaymentType();
            if (paymentType == null || !AlipayPaymentType.isValid(paymentType)) {
                paymentType = AlipayPaymentType.getDefault();
            }
            
            logger.info("创建支付宝支付请求: orderId={}, amount={}, paymentType={}", 
                request.getOrderId(), request.getAmount(), paymentType);
            
            PaymentResult result;
            switch (paymentType) {
                case AlipayPaymentType.WEB:
                    result = createWebPayment(request);
                    break;
                case AlipayPaymentType.QR_CODE:
                    result = createQrCodePayment(request);
                    break;
                default:
                    result = createWebPayment(request);
                    break;
            }
            
            // 设置支付方式和类型
            if (result.isSuccess()) {
                result.setPaymentMethod("ALIPAY");
                result.setPaymentType(paymentType);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("支付宝支付创建异常: orderId={}", request.getOrderId(), e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    /** 创建网页支付 */
    private PaymentResult createWebPayment(PaymentRequest request) throws Exception {
        AlipayTradePagePayResponse response = Factory.Payment.Page().pay(
            request.getTitle(),
            request.getOrderNo(),
            formatAmount(request.getAmount().toString()),
            request.getSuccessUrl()
        );
        
        if (ResponseChecker.success(response)) {
            PaymentResult result = PaymentResult.success();
            result.setPaymentUrl(response.body);
            result.setProviderOrderId(request.getOrderNo());
            logger.info("支付宝WEB支付创建成功: orderId={}", request.getOrderId());
            return result;
        } else {
            logger.error("支付宝WEB支付创建失败: orderId={}", request.getOrderId());
            return PaymentResult.failure("WEB_PAYMENT_CREATE_FAILED", "WEB支付创建失败");
        }
    }
    
    /** 创建二维码支付 */
    private PaymentResult createQrCodePayment(PaymentRequest request) throws Exception {
        AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace().preCreate(
            request.getTitle(),
            request.getOrderNo(),
            formatAmount(request.getAmount().toString())
        );
        
        if (ResponseChecker.success(response)) {
            PaymentResult result = PaymentResult.success();
            result.setPaymentUrl(response.qrCode); // 二维码内容字符串
            result.setProviderOrderId(request.getOrderNo());
            logger.info("支付宝二维码支付创建成功: orderId={}, qrCode={}", request.getOrderId(), response.qrCode);
            return result;
        } else {
            logger.error("支付宝二维码支付创建失败: orderId={}", request.getOrderId());
            return PaymentResult.failure("QR_CODE_PAYMENT_CREATE_FAILED", "二维码支付创建失败");
        }
    }

    @Override
    public PaymentResult queryPayment(String providerOrderId) {
        try {
            initializeConfig();
            
            logger.info("查询支付宝支付状态: providerOrderId={}", providerOrderId);
            
            AlipayTradeQueryResponse response = Factory.Payment.Common().query(providerOrderId);
            
            if (ResponseChecker.success(response)) {
                PaymentResult result = new PaymentResult();
                result.setProviderOrderId(response.outTradeNo);
                result.setProviderPaymentId(response.tradeNo);
                
                // 检查支付状态
                if ("TRADE_SUCCESS".equals(response.tradeStatus) || "TRADE_FINISHED".equals(response.tradeStatus)) {
                    result.setSuccess(true);
                    logger.info("支付宝支付成功: providerOrderId={}, tradeNo={}", providerOrderId, response.tradeNo);
                } else {
                    result.setSuccess(false);
                    result.setErrorMessage("支付未成功, 状态: " + response.tradeStatus);
                    logger.info("支付宝支付未成功: providerOrderId={}, status={}", providerOrderId, response.tradeStatus);
                }
                
                return result;
            } else {
                logger.error("支付宝支付查询失败: providerOrderId={}", providerOrderId);
                return PaymentResult.failure("PAYMENT_QUERY_FAILED", "支付查询失败");
            }
            
        } catch (Exception e) {
            logger.error("支付宝支付查询异常: providerOrderId={}", providerOrderId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    @Override
    public PaymentCallback handleCallback(Map<String, Object> callbackData) {
        PaymentCallback callback = new PaymentCallback();
        callback.setRawData(callbackData);
        
        try {
            logger.info("处理支付宝支付回调: data={}", callbackData);
            
            // 验证签名
            boolean isValid = verifyCallback(callbackData);
            callback.setSignatureValid(isValid);
            
            if (!isValid) {
                logger.warn("支付宝回调验签失败");
                return callback;
            }
            
            // 解析回调数据
            String tradeStatus = (String) callbackData.get("trade_status");
            String outTradeNo = (String) callbackData.get("out_trade_no");
            String tradeNo = (String) callbackData.get("trade_no");
            String totalAmount = (String) callbackData.get("total_amount");
            String gmtPayment = (String) callbackData.get("gmt_payment");
            String buyerLogonId = (String) callbackData.get("buyer_logon_id");
            
            callback.setOrderNo(outTradeNo);
            callback.setProviderOrderId(outTradeNo);
            callback.setProviderPaymentId(tradeNo);
            callback.setPaymentTime(gmtPayment);
            callback.setBuyerInfo(buyerLogonId);
            callback.setCurrency("CNY");
            
            if (StringUtils.hasText(totalAmount)) {
                callback.setAmount(new BigDecimal(totalAmount));
            }
            
            // 判断支付状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                callback.setPaymentSuccess(true);
                callback.setPaymentStatus("SUCCESS");
                logger.info("支付宝支付成功回调: outTradeNo={}, tradeNo={}, amount={}", 
                    outTradeNo, tradeNo, totalAmount);
            } else {
                callback.setPaymentSuccess(false);
                callback.setPaymentStatus(tradeStatus);
                logger.info("支付宝支付未成功回调: outTradeNo={}, status={}", outTradeNo, tradeStatus);
            }
            
        } catch (Exception e) {
            logger.error("支付宝回调处理异常", e);
            callback.setSignatureValid(false);
            callback.setPaymentSuccess(false);
            callback.setErrorMessage("回调处理异常: " + e.getMessage());
        }
        
        return callback;
    }

    @Override
    public PaymentResult cancelPayment(String providerOrderId) {
        logger.warn("支付宝不支持取消支付操作: providerOrderId={}", providerOrderId);
        return PaymentResult.failure("NOT_SUPPORTED", "支付宝不支持取消支付操作");
    }

    @Override
    public PaymentResult refundPayment(String providerOrderId, String refundAmount, String refundReason) {
        try {
            initializeConfig();
            
            logger.info("申请支付宝退款: providerOrderId={}, amount={}, reason={}", 
                providerOrderId, refundAmount, refundReason);
            
            // 生成退款请求号
            String refundNo = "RF" + System.currentTimeMillis();
            
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(
                providerOrderId, 
                formatAmount(refundAmount)
            );
            
            if (ResponseChecker.success(response)) {
                PaymentResult result = PaymentResult.success();
                result.setProviderOrderId(response.outTradeNo);
                result.setProviderPaymentId(response.tradeNo);
                
                // 设置退款信息
                Map<String, Object> extraData = new HashMap<>();
                extraData.put("refundAmount", response.refundFee);
                extraData.put("refundDate", response.gmtRefundPay);
                extraData.put("refundReason", refundReason);
                result.setExtraData(extraData);
                
                logger.info("支付宝退款成功: providerOrderId={}, refundAmount={}, tradeNo={}", 
                    providerOrderId, response.refundFee, response.tradeNo);
                return result;
            } else {
                logger.error("支付宝退款失败: providerOrderId={}", providerOrderId);
                return PaymentResult.failure("REFUND_FAILED", "退款失败");
            }
            
        } catch (Exception e) {
            logger.error("支付宝退款异常: providerOrderId={}", providerOrderId, e);
            return PaymentResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    @Override
    protected boolean verifyCallback(Map<String, Object> callbackData) {
        try {
            initializeConfig();
            
            // 将Map转换为适合的格式
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : callbackData.entrySet()) {
                if (entry.getValue() != null) {
                    params.put(entry.getKey(), entry.getValue().toString());
                }
            }
            
            // 使用EasySDK验证回调
            return Factory.Payment.Common().verifyNotify(params);
            
        } catch (Exception e) {
            logger.error("支付宝回调验签异常", e);
            return false;
        }
    }

    @Override
    public String getCallbackResponse(boolean success) {
        return success ? "success" : "failure";
    }

    @Override
    protected String formatAmount(String amount) {
        try {
            BigDecimal amt = new BigDecimal(amount);
            return amt.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } catch (Exception e) {
            logger.error("金额格式化失败: amount={}", amount, e);
            return "0.00";
        }
    }

    @Override
    protected String parseAmount(String amount) {
        try {
            return new BigDecimal(amount).toString();
        } catch (Exception e) {
            logger.error("金额解析失败: amount={}", amount, e);
            return "0";
        }
    }

    @Override
    protected String getConfig(String key) {
        switch (key) {
            case "app-id":
                return appId;
            case "private-key":
                return privateKey;
            case "alipay-public-key":
                return alipayPublicKey;
            case "gateway-host":
                return gatewayHost;
            case "sign-type":
                return signType;
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
}