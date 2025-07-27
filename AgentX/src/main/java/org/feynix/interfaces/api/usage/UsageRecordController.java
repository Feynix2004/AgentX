package org.feynix.interfaces.api.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.feynix.application.usage.dto.UsageRecordDTO;
import org.feynix.application.usage.service.UsageRecordAppService;
import org.feynix.infrastructure.auth.UserContext;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.usage.request.QueryUsageRecordRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用记录控制层
 * 提供使用记录查询的API接口
 */
@RestController
@RequestMapping("/usage-records")
public class UsageRecordController {
    
    private final UsageRecordAppService usageRecordAppService;
    
    public UsageRecordController(UsageRecordAppService usageRecordAppService) {
        this.usageRecordAppService = usageRecordAppService;
    }
    
    /** 根据ID获取使用记录
     * 
     * @param recordId 记录ID
     * @return 使用记录信息 */
    @GetMapping("/{recordId}")
    public Result<UsageRecordDTO> getUsageRecordById(@PathVariable String recordId) {
        UsageRecordDTO record = usageRecordAppService.getUsageRecordById(recordId);
        return Result.success(record);
    }
    
    /** 获取当前用户的使用记录（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页列表 */
    @GetMapping("/current")
    public Result<Page<UsageRecordDTO>> getCurrentUserUsageRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        String userId = UserContext.getCurrentUserId();
        Page<UsageRecordDTO> records = usageRecordAppService.getUserUsageRecords(userId, page, size);
        return Result.success(records);
    }
    
    /** 获取指定用户的使用记录（分页）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页列表 */
    @GetMapping("/user/{userId}")
    public Result<Page<UsageRecordDTO>> getUserUsageRecords(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<UsageRecordDTO> records = usageRecordAppService.getUserUsageRecords(userId, page, size);
        return Result.success(records);
    }
    
    /** 按条件查询当前用户使用记录
     * 
     * @param request 查询参数
     * @return 使用记录分页列表 */
    @GetMapping
    public Result<Page<UsageRecordDTO>> queryUsageRecords(QueryUsageRecordRequest request) {
        // 前台API只能查询当前用户的记录，防止越权
        String userId = UserContext.getCurrentUserId();
        request.setUserId(userId);
        Page<UsageRecordDTO> records = usageRecordAppService.queryUsageRecords(request);
        return Result.success(records);
    }
    
    /** 获取用户某个商品的使用记录
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页列表 */
    @GetMapping("/user/{userId}/product/{productId}")
    public Result<Page<UsageRecordDTO>> getUserProductUsageRecords(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<UsageRecordDTO> records = usageRecordAppService.getUserProductUsageRecords(userId, productId, page, size);
        return Result.success(records);
    }
    
    /** 获取当前用户某个商品的使用记录
     * 
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页列表 */
    @GetMapping("/current/product/{productId}")
    public Result<Page<UsageRecordDTO>> getCurrentUserProductUsageRecords(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        String userId = UserContext.getCurrentUserId();
        Page<UsageRecordDTO> records = usageRecordAppService.getUserProductUsageRecords(userId, productId, page, size);
        return Result.success(records);
    }
    
    /** 获取用户在指定时间范围内的使用记录
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表 */
    @GetMapping("/user/{userId}/time-range")
    public Result<List<UsageRecordDTO>> getUserUsageByTimeRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<UsageRecordDTO> records = usageRecordAppService.getUserUsageByTimeRange(userId, startTime, endTime);
        return Result.success(records);
    }
    
    /** 获取当前用户在指定时间范围内的使用记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表 */
    @GetMapping("/current/time-range")
    public Result<List<UsageRecordDTO>> getCurrentUserUsageByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        String userId = UserContext.getCurrentUserId();
        List<UsageRecordDTO> records = usageRecordAppService.getUserUsageByTimeRange(userId, startTime, endTime);
        return Result.success(records);
    }
    
    /** 获取商品的使用记录（分页）
     * 
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录分页列表 */
    @GetMapping("/product/{productId}")
    public Result<Page<UsageRecordDTO>> getProductUsageRecords(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<UsageRecordDTO> records = usageRecordAppService.getProductUsageRecords(productId, page, size);
        return Result.success(records);
    }
    
    /** 检查请求ID是否已存在
     * 
     * @param requestId 请求ID
     * @return 是否存在 */
    @GetMapping("/request/{requestId}/exists")
    public Result<Boolean> existsByRequestId(@PathVariable String requestId) {
        boolean exists = usageRecordAppService.existsByRequestId(requestId);
        return Result.success(exists);
    }
    
    /** 获取用户的总消费金额
     * 
     * @param userId 用户ID
     * @return 总消费金额 */
    @GetMapping("/user/{userId}/total-cost")
    public Result<BigDecimal> getUserTotalCost(@PathVariable String userId) {
        BigDecimal totalCost = usageRecordAppService.getUserTotalCost(userId);
        return Result.success(totalCost);
    }
    
    /** 获取当前用户的总消费金额
     * 
     * @return 总消费金额 */
    @GetMapping("/current/total-cost")
    public Result<BigDecimal> getCurrentUserTotalCost() {
        String userId = UserContext.getCurrentUserId();
        BigDecimal totalCost = usageRecordAppService.getUserTotalCost(userId);
        return Result.success(totalCost);
    }
    
    /** 获取用户在指定时间范围内的消费金额
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费金额 */
    @GetMapping("/user/{userId}/cost-by-time")
    public Result<BigDecimal> getUserCostByTimeRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        BigDecimal cost = usageRecordAppService.getUserCostByTimeRange(userId, startTime, endTime);
        return Result.success(cost);
    }
    
    /** 获取当前用户在指定时间范围内的消费金额
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费金额 */
    @GetMapping("/current/cost-by-time")
    public Result<BigDecimal> getCurrentUserCostByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        String userId = UserContext.getCurrentUserId();
        BigDecimal cost = usageRecordAppService.getUserCostByTimeRange(userId, startTime, endTime);
        return Result.success(cost);
    }
}