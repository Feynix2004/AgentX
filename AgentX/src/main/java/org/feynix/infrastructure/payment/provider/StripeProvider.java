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
import org.feynix.domain.order.constant.PaymentPlatform;
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


    @Override
    public PaymentPlatform getPaymentPlatform() {
        return PaymentPlatform.STRIPE;
    }

    @Override
    public String getProviderCode() {
        return "";
    }

    @Override
    public String getProviderName() {
        return "";
    }

    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        return null;
    }

    @Override
    public PaymentResult queryPayment(String providerOrderId) {
        return null;
    }

    @Override
    public PaymentCallback handleCallback(Map<String, Object> callbackData) {
        return null;
    }

    @Override
    public PaymentResult cancelPayment(String providerOrderId) {
        return null;
    }

    @Override
    public PaymentResult refundPayment(String providerOrderId, String refundAmount, String refundReason) {
        return null;
    }

    @Override
    protected boolean verifyCallback(Map<String, Object> callbackData) {
        return false;
    }

    @Override
    public String getCallbackResponse(boolean success) {
        return "";
    }

    @Override
    protected String formatAmount(String amount) {
        return "";
    }

    @Override
    protected String parseAmount(String amount) {
        return "";
    }

    @Override
    protected String getConfig(String key) {
        return "";
    }

    @Override
    public boolean isConfigured() {
        return false;
    }
}