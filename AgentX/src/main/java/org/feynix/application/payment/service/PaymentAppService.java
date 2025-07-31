package org.feynix.application.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.feynix.application.payment.assembler.PaymentAssembler;
import org.feynix.domain.order.constant.OrderStatus;
import org.feynix.domain.order.constant.OrderType;
import org.feynix.domain.order.constant.PaymentPlatform;
import org.feynix.domain.order.constant.PaymentType;
import org.feynix.domain.order.model.OrderEntity;
import org.feynix.domain.order.service.OrderDomainService;
import org.feynix.infrastructure.auth.UserContext;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.infrastructure.payment.constant.PaymentMethod;
import org.feynix.infrastructure.payment.factory.PaymentProviderFactory;
import org.feynix.infrastructure.payment.model.PaymentCallback;
import org.feynix.infrastructure.payment.model.PaymentRequest;
import org.feynix.infrastructure.payment.model.PaymentResult;
import org.feynix.infrastructure.payment.provider.PaymentProvider;
import org.feynix.interfaces.dto.account.request.RechargeRequest;
import org.feynix.interfaces.dto.account.response.PaymentResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** 支付应用服务 */
@Service
public class PaymentAppService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentAppService.class);
    
    private final OrderDomainService orderDomainService;
    private final PaymentProviderFactory paymentProviderFactory;
    
    public PaymentAppService(OrderDomainService orderDomainService, 
                            PaymentProviderFactory paymentProviderFactory) {
        this.orderDomainService = orderDomainService;
        this.paymentProviderFactory = paymentProviderFactory;
    }
    
    /** 创建充值订单并发起支付
     * 
     * @param request 充值请求
     * @return 支付响应 */
    @Transactional
    public PaymentResponseDTO createRechargePayment(RechargeRequest request) {
        String userId = UserContext.getCurrentUserId();
        
        // 转换支付平台和类型
        PaymentPlatform paymentPlatform = PaymentPlatform.fromCode(request.getPaymentPlatform());
        PaymentType paymentType = PaymentType.fromCode(request.getPaymentType());
        
        // 检查支付平台是否可用
        if (!paymentProviderFactory.isAvailable(paymentPlatform)) {
            throw new BusinessException("支付平台暂不可用: " + paymentPlatform.getName());
        }
        
        // 创建充值订单
        OrderEntity order = createRechargeOrder(userId, request, paymentPlatform, paymentType);
        
        try {
            // 获取支付提供商并发起支付
            PaymentProvider provider = paymentProviderFactory.getProvider(paymentPlatform);
            PaymentRequest paymentRequest = buildPaymentRequest(order, request);
            PaymentResult paymentResult = provider.createPayment(paymentRequest);
            
            if (paymentResult.isSuccess()) {
                // 使用Assembler构建响应
                PaymentResponseDTO response = PaymentAssembler.toPaymentResponseDTO(order, paymentResult);
                
                logger.info("充值支付创建成功: userId={}, orderId={}, amount={}, platform={}, type={}", 
                    userId, order.getId(), request.getAmount(), paymentPlatform, paymentType);
                
                return response;
            } else {
                logger.error("充值支付创建失败: userId={}, orderId={}, error={}", 
                    userId, order.getId(), paymentResult.getErrorMessage());
                throw new BusinessException("支付创建失败: " + paymentResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("充值支付处理异常: userId={}, orderId={}", userId, order.getId(), e);
            throw new BusinessException("支付处理失败: " + e.getMessage());
        }
    }
    
    
    /** 创建充值订单 */
    private OrderEntity createRechargeOrder(String userId, RechargeRequest request, 
                                          PaymentPlatform paymentPlatform, PaymentType paymentType) {
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setOrderType(OrderType.RECHARGE);
        order.setTitle("账户充值");
        order.setDescription("账户余额充值");
        order.setAmount(request.getAmount());
        order.setCurrency("CNY");
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentPlatform(paymentPlatform);
        order.setPaymentType(paymentType);
        // 订单创建时间由BaseEntity自动设置
        
        return orderDomainService.createOrder(order);
    }
    
    /** 构建支付请求 */
    private PaymentRequest buildPaymentRequest(OrderEntity order, RechargeRequest request) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setPaymentId(order.getId()); // 使用订单ID作为支付ID
        paymentRequest.setOrderNo(order.getOrderNo());
        paymentRequest.setTitle(order.getTitle());
        paymentRequest.setDescription(order.getDescription());
        paymentRequest.setAmount(order.getAmount());
        paymentRequest.setCurrency(order.getCurrency());
        paymentRequest.setUserId(order.getUserId());
        paymentRequest.setPaymentType(order.getPaymentType().getCode());
        
        // 设置回调URL，使用平台代码作为路径参数
        String platformCode = order.getPaymentPlatform().getCode();
        paymentRequest.setNotifyUrl("/api/payments/callback/" + platformCode);
        paymentRequest.setSuccessUrl("/api/payments/success");
        paymentRequest.setCancelUrl("/api/payments/cancel");
        
        return paymentRequest;
    }
    
    /** 生成订单号 */
    private String generateOrderNo() {
        return "RCH" + System.currentTimeMillis() + String.format("%04d", 
            (int)(Math.random() * 10000));
    }
    
    /** 处理支付回调
     * 
     * @param paymentPlatform 支付平台代码
     * @param callbackData 回调数据
     * @return 回调响应字符串 */
    @Transactional
    public String handlePaymentCallback(PaymentPlatform paymentPlatform, Map<String, Object> callbackData) {
        try {
            // 获取支付提供商
            PaymentProvider provider = paymentProviderFactory.getProvider(paymentPlatform);
            
            // 处理回调
            PaymentCallback callback = provider.handleCallback(callbackData);
            
            if (callback.isSignatureValid()) {
                // 更新订单状态
                updateOrderStatus(callback);
                
                logger.info("支付回调处理成功: platform={}, orderNo={}, success={}", 
                    paymentPlatform, callback.getOrderNo(), callback.isPaymentSuccess());
            } else {
                logger.warn("支付回调验签失败: platform={}, orderNo={}", 
                    paymentPlatform, callback.getOrderNo());
            }
            
            // 返回平台要求的响应格式
            return provider.getCallbackResponse(callback.isSignatureValid() && callback.isPaymentSuccess());
            
        } catch (Exception e) {
            logger.error("支付回调处理异常: platform={}", paymentPlatform, e);
            // 返回失败响应，避免重复回调
            return "failure";
        }
    }
    
    /** 更新订单状态
     * 
     * @param callback 支付回调对象 */
    private void updateOrderStatus(PaymentCallback callback) {
        try {
            String orderNo = callback.getOrderNo();
            if (orderNo == null || orderNo.trim().isEmpty()) {
                logger.warn("回调中没有订单号信息");
                return;
            }
            
            // 根据订单号获取订单
            OrderEntity order = orderDomainService.getOrderByOrderNo(orderNo);
            if (order == null) {
                logger.warn("订单不存在: orderNo={}", orderNo);
                return;
            }
            
            // 检查订单状态是否可以更新
            if (order.getStatus() != OrderStatus.PENDING) {
                logger.info("订单状态已更新，跳过处理: orderNo={}, currentStatus={}", 
                    orderNo, order.getStatus());
                return;
            }
            
            // 根据支付结果更新订单状态
            if (callback.isPaymentSuccess()) {
                order.setStatus(OrderStatus.PAID);
                logger.info("订单支付成功: orderNo={}, amount={}", orderNo, callback.getAmount());
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                logger.info("订单支付失败: orderNo={}", orderNo);
            }
            
            // 保存订单状态更新
            orderDomainService.updateOrderStatus(order.getId(), order.getStatus());
            
        } catch (Exception e) {
            logger.error("更新订单状态失败: orderNo={}", callback.getOrderNo(), e);
            throw new BusinessException("订单状态更新失败: " + e.getMessage());
        }
    }
    
}