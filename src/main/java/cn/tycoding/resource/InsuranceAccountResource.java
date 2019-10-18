package cn.tycoding.resource;

import cn.tycoding.domain.InsuranceAccount;
import cn.tycoding.repository.InsuranceAccountRepository;
import cn.tycoding.service.InsuranceAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 14:03
 * 4
 */

@RestController
@RequestMapping("/insurance-accounts")

public class InsuranceAccountResource {
    /**
     * 1 由于只做模拟，不提供银行账户的注册注销接口；不提供设置账户初始资金的接口（使用默认值）
     * <p>
     * 2 在进行增减操作前，先判断有没有该账户，没有则自动注册
     * <p>
     * 3 实现的接口包含：查询所有账户；查看某账户资金流水和开始和结束的金额
     * <p>
     * 4 实现的功能：增减某一账户的可用金额（对应账户的冻结解冻资金）；
     * 增减某账户金额；查询某账户当前可用金额
     */
    private final Logger logger = LoggerFactory.getLogger(InsuranceAccountResource.class);
    private final InsuranceAccountService insuranceAccountService;
    private final InsuranceAccountRepository insuranceAccountRepository;

    public InsuranceAccountResource(InsuranceAccountRepository insuranceAccountRepository, InsuranceAccountService insuranceAccountService) {
        this.insuranceAccountRepository = insuranceAccountRepository;
        this.insuranceAccountService = insuranceAccountService;
    }

    /**
     * 查询所有保险账户
     *
     * @return
     */
    @GetMapping()
    public List<InsuranceAccount> getAll() {
        logger.info("REST 查询所有账户");
        return insuranceAccountRepository.findAll();
    }

    /**TODO
     * 查询查询账户流水
     *
     * @return
     */
    @GetMapping("/money-log/{id}/{type}")
    public String findTruckById(@PathVariable("id") int id, @PathVariable("type") String type) {
        logger.info("REST 查询账户流水");
        return insuranceAccountService.findMoneyLog(id, type);
    }

}



