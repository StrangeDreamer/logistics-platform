package cn.tycoding.resource;

import cn.tycoding.domain.BankAccount;
import cn.tycoding.repository.BankAccountRepository;
import cn.tycoding.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 11:15
 * 4
 */

@RestController
@RequestMapping("/bankAccounts")
@CrossOrigin(origins = "*")


public class BankAccountResource {
    /**
     * 1 由于只做模拟，不提供银行账户的注册注销接口；不提供设置账户初始资金的接口（使用默认值）
     *
     * 2 在进行增减操作前，先判断有没有该账户，没有则自动注册
     *
     * 3 实现的接口包含：查询所有账户；查看某账户资金流水和开始和结束的金额
     *
     * 4 实现的功能：增减某一账户的可用金额（对应账户的冻结解冻资金）；
     *   增减某账户金额；查询某账户当前可用金额
     */
    private final Logger logger= LoggerFactory.getLogger(BankAccountResource.class);
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;

    public BankAccountResource(BankAccountRepository bankAccountRepository, BankAccountService bankAccountService) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountService = bankAccountService;
    }

    /**
     * 查询所有银行注册账户
     * @return
     */
    @GetMapping("/findAll")
    public List<BankAccount> getAll(){
        logger.info("REST 查询所有账户");
        return bankAccountRepository.findAll();
    }

    /**
     * 查询查询账户流水
     * @return
     */
    @GetMapping("/findMoneyLog/{id}/{type}")
    public String findTruckById(@PathVariable("id") int id,@PathVariable("type") String type){
        logger.info("REST 查询账户流水");
//        return  bankAccountService.check(id,type);
        return bankAccountService.findMoneyLog(id,type);
    }



}
