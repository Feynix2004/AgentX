package org.feynix.interfaces.api.account;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.feynix.application.account.dto.AccountDTO;
import org.feynix.application.account.service.AccountAppService;
import org.feynix.infrastructure.auth.UserContext;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.account.request.AddCreditRequest;
import org.feynix.interfaces.dto.account.request.RechargeRequest;

import java.math.BigDecimal;

/**
 * 账户管理控制层
 * 提供用户账户管理的API接口
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private final AccountAppService accountAppService;
    
    public AccountController(AccountAppService accountAppService) {
        this.accountAppService = accountAppService;
    }
    
    /** 获取当前用户账户信息
     * 
     * @return 账户信息 */
    @GetMapping("/current")
    public Result<AccountDTO> getCurrentUserAccount() {
        String userId = UserContext.getCurrentUserId();
        AccountDTO account = accountAppService.getUserAccount(userId);
        return Result.success(account);
    }
    
    /** 根据用户ID获取账户信息
     * 
     * @param userId 用户ID
     * @return 账户信息 */
    @GetMapping("/user/{userId}")
    public Result<AccountDTO> getUserAccount(@PathVariable String userId) {
        AccountDTO account = accountAppService.getUserAccount(userId);
        return Result.success(account);
    }
    
    /** 根据账户ID获取账户信息
     * 
     * @param accountId 账户ID
     * @return 账户信息 */
    @GetMapping("/{accountId}")
    public Result<AccountDTO> getAccountById(@PathVariable String accountId) {
        AccountDTO account = accountAppService.getAccountById(accountId);
        return Result.success(account);
    }
    
    /** 当前用户账户充值
     * 
     * @param request 充值请求
     * @return 充值后的账户信息 */
    @PostMapping("/current/recharge")
    public Result<AccountDTO> rechargeCurrentUser(@RequestBody @Validated RechargeRequest request) {
        String userId = UserContext.getCurrentUserId();
        AccountDTO account = accountAppService.recharge(userId, request);
        return Result.success(account);
    }
    
    /** 指定用户账户充值（管理员接口）
     * 
     * @param userId 用户ID
     * @param request 充值请求
     * @return 充值后的账户信息 */
    @PostMapping("/user/{userId}/recharge")
    public Result<AccountDTO> rechargeUser(@PathVariable String userId, 
                                          @RequestBody @Validated RechargeRequest request) {
        AccountDTO account = accountAppService.recharge(userId, request);
        return Result.success(account);
    }
    
    /** 为当前用户增加信用额度
     * 
     * @param request 增加信用额度请求
     * @return 更新后的账户信息 */
    @PostMapping("/current/credit")
    public Result<AccountDTO> addCreditToCurrentUser(@RequestBody @Validated AddCreditRequest request) {
        String userId = UserContext.getCurrentUserId();
        AccountDTO account = accountAppService.addCredit(userId, request);
        return Result.success(account);
    }
    
    /** 为指定用户增加信用额度（管理员接口）
     * 
     * @param userId 用户ID
     * @param request 增加信用额度请求
     * @return 更新后的账户信息 */
    @PostMapping("/user/{userId}/credit")
    public Result<AccountDTO> addCreditToUser(@PathVariable String userId, 
                                             @RequestBody @Validated AddCreditRequest request) {
        AccountDTO account = accountAppService.addCredit(userId, request);
        return Result.success(account);
    }
    
    /** 检查当前用户余额是否充足
     * 
     * @param amount 需要检查的金额
     * @return 是否充足 */
    @GetMapping("/current/balance/check")
    public Result<Boolean> checkCurrentUserBalance(@RequestParam BigDecimal amount) {
        String userId = UserContext.getCurrentUserId();
        boolean sufficient = accountAppService.checkSufficientBalance(userId, amount);
        return Result.success(sufficient);
    }
    
    /** 检查指定用户余额是否充足
     * 
     * @param userId 用户ID
     * @param amount 需要检查的金额
     * @return 是否充足 */
    @GetMapping("/user/{userId}/balance/check")
    public Result<Boolean> checkUserBalance(@PathVariable String userId, 
                                           @RequestParam BigDecimal amount) {
        boolean sufficient = accountAppService.checkSufficientBalance(userId, amount);
        return Result.success(sufficient);
    }
    
    /** 获取当前用户可用余额
     * 
     * @return 可用余额 */
    @GetMapping("/current/balance/available")
    public Result<BigDecimal> getCurrentUserAvailableBalance() {
        String userId = UserContext.getCurrentUserId();
        BigDecimal balance = accountAppService.getAvailableBalance(userId);
        return Result.success(balance);
    }
    
    /** 获取指定用户可用余额
     * 
     * @param userId 用户ID
     * @return 可用余额 */
    @GetMapping("/user/{userId}/balance/available")
    public Result<BigDecimal> getUserAvailableBalance(@PathVariable String userId) {
        BigDecimal balance = accountAppService.getAvailableBalance(userId);
        return Result.success(balance);
    }
    
    /** 检查当前用户账户是否存在
     * 
     * @return 是否存在 */
    @GetMapping("/current/exists")
    public Result<Boolean> checkCurrentUserAccountExists() {
        String userId = UserContext.getCurrentUserId();
        boolean exists = accountAppService.existsAccount(userId);
        return Result.success(exists);
    }
    
    /** 检查指定用户账户是否存在
     * 
     * @param userId 用户ID
     * @return 是否存在 */
    @GetMapping("/user/{userId}/exists")
    public Result<Boolean> checkUserAccountExists(@PathVariable String userId) {
        boolean exists = accountAppService.existsAccount(userId);
        return Result.success(exists);
    }
}