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
import org.feynix.interfaces.dto.account.response.OrderStatusResponseDTO;
import org.feynix.interfaces.dto.account.response.PaymentMethodDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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
    
    /** 构建支付请求（仅用于查询） */
    private PaymentRequest buildPaymentRequest(OrderEntity order) {
        return buildPaymentRequest(order, null);
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
    
    /** 查询订单状态（根据订单号）
     * 
     * @param orderNo 订单号
     * @return 订单状态响应 */
    public OrderStatusResponseDTO queryOrderStatus(String orderNo) {
        logger.info("查询订单状态: orderNo={}", orderNo);
        
        try {
            // 1. 查询本地订单
            OrderEntity order = orderDomainService.findOrderByOrderNo(orderNo);
            if (order == null) {
                throw new BusinessException("订单不存在: " + orderNo);
            }
            
            // 2. 查询支付平台订单状态（只有未完成的订单才需要查询）
            if (order.getStatus() == OrderStatus.PENDING) {
                try {
                    PaymentProvider provider = paymentProviderFactory.getProvider(order.getPaymentPlatform());
                    PaymentResult platformResult = provider.queryPayment(order.getOrderNo());
                    
                    if (platformResult.isSuccess() || platformResult.getStatus() != null) {
                        // 3. 使用Provider的状态转换逻辑
                        OrderStatus platformStatus = provider.convertToOrderStatus(platformResult.getStatus());
                        if (platformStatus != order.getStatus()) {
                            logger.info("订单状态不一致，更新本地状态: orderNo={}, localStatus={}, platformStatus={}, rawStatus={}", 
                                orderNo, order.getStatus(), platformStatus, platformResult.getStatus());
                            orderDomainService.updateOrderStatus(order.getId(), platformStatus);
                            order.setStatus(platformStatus);
                        }
                    } else {
                        logger.warn("查询支付平台订单状态失败: orderNo={}, error={}", orderNo, platformResult.getErrorMessage());
                        // 支付平台查询失败时，不更新本地状态，使用本地状态
                    }
                } catch (Exception e) {
                    logger.warn("查询支付平台订单状态异常: orderNo={}", orderNo, e);
                    // 平台查询异常时，不影响返回本地订单状态
                }
            }
            
            // 4. 构建响应
            return buildOrderStatusResponse(order);
            
        } catch (Exception e) {
            logger.error("查询订单状态失败: orderNo={}", orderNo, e);
            throw new BusinessException("查询订单状态失败: " + e.getMessage());
        }
    }
    
    
    /** 构建订单状态响应 */
    private OrderStatusResponseDTO buildOrderStatusResponse(OrderEntity order) {
        OrderStatusResponseDTO response = new OrderStatusResponseDTO();
        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus().name());
        response.setPaymentPlatform(order.getPaymentPlatform().getCode());
        response.setPaymentType(order.getPaymentType().getCode());
        response.setAmount(order.getAmount());
        response.setTitle(order.getTitle());
        
        // 如果是二维码支付，需要重新生成支付URL
        if ("QR_CODE".equals(order.getPaymentType().getCode()) && order.getStatus() == OrderStatus.PENDING) {
            try {
                PaymentProvider provider = paymentProviderFactory.getProvider(order.getPaymentPlatform());
                PaymentRequest paymentRequest = buildPaymentRequest(order);
                PaymentResult paymentResult = provider.createPayment(paymentRequest);
                if (paymentResult.isSuccess()) {
                    response.setPaymentUrl(paymentResult.getPaymentUrl());
                }
            } catch (Exception e) {
                logger.warn("重新生成支付URL失败: orderNo={}", order.getOrderNo(), e);
            }
        }
        
        response.setCreatedAt(order.getCreatedAt().toString());
        response.setUpdatedAt(order.getUpdatedAt().toString());
        if (order.getExpiredAt() != null) {
            response.setExpiredAt(order.getExpiredAt().toString());
        }
        
        return response;
    }
    
    /** 获取可用的支付方法列表
     * 
     * @return 支付方法列表 */
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getAvailablePaymentMethods() {
        logger.info("获取可用的支付方法列表");
        
        List<PaymentMethodDTO> methods = new ArrayList<>();
        
        try {
            // 获取所有可用的支付平台
            List<PaymentPlatform> availablePlatforms = paymentProviderFactory.getAvailablePaymentPlatforms();
            
            for (PaymentPlatform platform : availablePlatforms) {
                PaymentMethodDTO methodDTO = new PaymentMethodDTO();
                methodDTO.setPlatformCode(platform.getCode());
                methodDTO.setPlatformName(platform.getName());
                methodDTO.setAvailable(true);
                methodDTO.setDescription(getPaymentPlatformDescription(platform));
                
                // 获取该平台支持的支付类型
                List<PaymentMethodDTO.PaymentTypeDTO> paymentTypes = getSupportedPaymentTypes(platform);
                methodDTO.setPaymentTypes(paymentTypes);
                
                methods.add(methodDTO);
            }
            
            logger.info("获取支付方法列表成功: 共{}个平台", methods.size());
            return methods;
            
        } catch (Exception e) {
            logger.error("获取支付方法列表失败", e);
            // 返回空列表而不是抛出异常，避免影响前端页面
            return new ArrayList<>();
        }
    }
    
    /** 获取支付平台描述 */
    private String getPaymentPlatformDescription(PaymentPlatform platform) {
        switch (platform) {
            case ALIPAY:
                return "支付宝支付，支持网页支付和扫码支付";
            case STRIPE:
                return "Stripe国际支付，支持信用卡支付";
            case WECHAT:
                return "微信支付，支持扫码支付和小程序支付";
            default:
                return platform.getName() + "支付";
        }
    }
    
    /** 获取平台支持的支付类型 */
    private List<PaymentMethodDTO.PaymentTypeDTO> getSupportedPaymentTypes(PaymentPlatform platform) {
        List<PaymentMethodDTO.PaymentTypeDTO> types = new ArrayList<>();
        
        // 只支持二维码支付
        switch (platform) {
            case ALIPAY:
                types.add(new PaymentMethodDTO.PaymentTypeDTO("QR_CODE", "扫码支付", false));
                break;
            case WECHAT:
                types.add(new PaymentMethodDTO.PaymentTypeDTO("QR_CODE", "扫码支付", false));
                break;
            case STRIPE:
                // Stripe暂不支持二维码支付，跳过
                break;
            default:
                // 其他平台暂不支持，跳过
                break;
        }
        
        // 为每个支付类型设置描述
        for (PaymentMethodDTO.PaymentTypeDTO type : types) {
            type.setDescription(getPaymentTypeDescription(type.getTypeCode()));
        }
        
        return types;
    }
    
    /** 获取支付类型描述 */
    private String getPaymentTypeDescription(String typeCode) {
        switch (typeCode) {
            case "WEB":
                return "跳转到支付平台网页完成支付";
            case "QR_CODE":
                return "扫描二维码完成支付";
            case "MOBILE":
                return "移动端应用内支付";
            case "H5":
                return "移动端网页支付";
            case "MINI_PROGRAM":
                return "小程序内支付";
            default:
                return "在线支付";
        }
    }
    
}