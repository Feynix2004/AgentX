package org.feynix.interfaces.api.portal.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.feynix.application.payment.service.PaymentAppService;
import org.feynix.domain.order.constant.PaymentPlatform;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.account.request.RechargeRequest;
import org.feynix.interfaces.dto.account.response.PaymentResponseDTO;
import org.feynix.interfaces.dto.account.response.OrderStatusResponseDTO;
import org.feynix.interfaces.dto.account.response.PaymentMethodDTO;
import org.feynix.infrastructure.exception.RateLimitException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/** 支付控制器 */
@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentAppService paymentAppService;

    public PaymentController(PaymentAppService paymentAppService) {
        this.paymentAppService = paymentAppService;
    }

    /** 创建充值支付
     * 
     * @param request 充值请求
     * @return 支付响应 */
    @PostMapping("/recharge")
    public Result<PaymentResponseDTO> createRechargePayment(@RequestBody @Validated RechargeRequest request) {

        logger.info("接收充值支付请求: amount={}, platform={}, type={}", request.getAmount(), request.getPaymentPlatform(),
                request.getPaymentType());

        try {
            PaymentResponseDTO response = paymentAppService.createRechargePayment(request);
            logger.info("充值支付创建成功: orderId={}, orderNo={}", response.getOrderId(), response.getOrderNo());
            return Result.success(response);

        } catch (RateLimitException e) {
            logger.warn("充值支付请求触发限流: amount={}, platform={}, type={}, error={}", 
                    request.getAmount(), request.getPaymentPlatform(), request.getPaymentType(), e.getMessage());
            return Result.error(429, "请求过于频繁，请稍后再试");
            
        } catch (Exception e) {
            logger.error("充值支付创建失败: amount={}, platform={}, type={}", request.getAmount(), request.getPaymentPlatform(),
                    request.getPaymentType(), e);
            return Result.error(500, e.getMessage());
        }
    }

    /** 查询订单状态
     * 
     * @param orderNo 订单号
     * @return 订单状态响应 */
    @GetMapping("/orders/{orderNo}/status")
    public Result<OrderStatusResponseDTO> queryOrderStatus(@PathVariable String orderNo) {

        logger.info("查询订单状态: orderNo={}", orderNo);

        try {
            OrderStatusResponseDTO response = paymentAppService.queryOrderStatus(orderNo);
            logger.info("订单状态查询成功: orderNo={}, status={}", orderNo, response.getStatus());
            return Result.success(response);

        } catch (Exception e) {
            logger.error("订单状态查询失败: orderNo={}", orderNo, e);
            return Result.error(500, e.getMessage());
        }
    }

    /** 获取可用的支付方法列表
     * 
     * @return 支付方法列表 */
    @GetMapping("/methods")
    public Result<List<PaymentMethodDTO>> getAvailablePaymentMethods() {

        logger.info("获取可用的支付方法列表");

        try {
            List<PaymentMethodDTO> methods = paymentAppService.getAvailablePaymentMethods();
            logger.info("支付方法列表获取成功: 共{}个平台", methods.size());
            return Result.success(methods);

        } catch (Exception e) {
            logger.error("获取支付方法列表失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /** 处理支付平台回调
     * 
     * @param platform 支付平台代码
     * @param request HTTP请求对象
     * @return 回调响应 */
    @PostMapping("/callback/{platform}")
    public ResponseEntity<String> handlePaymentCallback(@PathVariable String platform, HttpServletRequest request) {

        logger.info("接收支付回调: platform={}", platform);

        try {
            // 提取回调数据
            Map<String, Object> callbackData = extractCallbackData(request);
            logger.info("回调数据: platform={}, data={}", platform, callbackData);

            PaymentPlatform paymentPlatform = PaymentPlatform.fromCode(platform);

            // 处理回调
            String response = paymentAppService.handlePaymentCallback(paymentPlatform, callbackData);

            logger.info("支付回调处理成功: platform={}", platform);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("支付回调处理失败: platform={}", platform, e);
            // 返回失败响应，避免支付平台重复回调
            return ResponseEntity.ok("failure");
        }
    }

    /** 提取回调数据
     * 
     * @param request HTTP请求对象
     * @return 回调数据Map */
    private Map<String, Object> extractCallbackData(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();

        // 提取所有请求参数（支持form-data和query参数）
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                // 如果只有一个值，直接存储，否则存储数组
                if (values.length == 1) {
                    data.put(key, values[0]);
                } else {
                    data.put(key, values);
                }
            }
        });

        return data;
    }
}