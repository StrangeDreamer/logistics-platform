package cn.tycoding.service;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 15:15
 * 4
 */

import cn.tycoding.domain.InsuranceAccount;
import cn.tycoding.repository.InsuranceAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 担保方目前只提供给承运方账户，所有的type均为Truck；
 */

@Service
public class InsuranceAccountService {

    private final Logger logger = LoggerFactory.getLogger(InsuranceAccountService.class);
    @Autowired
    private InsuranceAccountRepository insuranceAccountRepository;




    // 查询所有的注册担保方账户
    public List<InsuranceAccount> findAll(){
        return insuranceAccountRepository.findAll();
    }

    // 检查 该承运方方是否存在，没有则自动创建,如果已经存在则直接返回该账户
    public synchronized InsuranceAccount check (int id, String type) {
        type = "承运方";
        InsuranceAccount insuranceAccount = insuranceAccountRepository.findInsuranceAccountByIdAndType(id, type);
        if (insuranceAccount == null) {
            insuranceAccount = new InsuranceAccount();
            insuranceAccount.setId(id);
            logger.info("该承运方账户不存在，自动为其创建承运方账户！");
            insuranceAccount.setMoney((int)(100000 + 500000 * Math.random()));
            insuranceAccount.setAvailableMoney(insuranceAccount.getMoney());
            insuranceAccount.setInsuranceAccountLog(insuranceAccount.getInsuranceAccountLog() + "参与方初始担保额为" + insuranceAccount.getMoney());
            insuranceAccountRepository.save(insuranceAccount);
        }
        return insuranceAccount;
    }

    // 查询指定注册承运方账户担保额的流水
    public String findMoneyLog(int id, String type) {
        InsuranceAccount insuranceAccount = check(id, type);
        return insuranceAccount.getInsuranceAccountLog() + "\n参与方当前可用担保额为" + insuranceAccount.getMoney();
    }

    // 冻结资金，如果money为正则为解冻，money为负数则为冻结
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    public boolean changeAvailableMoney(int id, String type, double money) {
        InsuranceAccount insuranceAccount = check(id,type);
        return changeAvailableMoney(insuranceAccount, money);
    }

    @Transactional
    public boolean changeAvailableMoney(InsuranceAccount insuranceAccount, double money) {
        if (insuranceAccount.getAvailableMoney() < money) {
            logger.info("冻结失败，担保额不足！");
            return false;
        }
        insuranceAccount.setAvailableMoney(insuranceAccount.getAvailableMoney() + money);
        if (money > 0) {
            insuranceAccount.setInsuranceAccountLog(insuranceAccount.getInsuranceAccountLog() +
                    ", " + insuranceAccount.getType() + insuranceAccount.getId() + "解冻担保额" + String.format("%.2f",money));
        } else {
            insuranceAccount.setInsuranceAccountLog(insuranceAccount.getInsuranceAccountLog() +
                    ", " + insuranceAccount.getType() + insuranceAccount.getId() +"冻结担保额" + String.format("%.2f",(-money)));
        }
        insuranceAccountRepository.save(insuranceAccount);
        return true;
    }

    // 查看当前可用担保额
    public double getAvailableMoney(int id, String type) {
        InsuranceAccount insuranceAccount = check(id, type);
        return  insuranceAccount.getAvailableMoney();
    }

    // 添加流水记录
    public String addMoneyLog(InsuranceAccount insuranceAccount, String log) {
        insuranceAccount.setInsuranceAccountLog(insuranceAccount.getInsuranceAccountLog() + "\n" + log);
        insuranceAccountRepository.save(insuranceAccount);
        return insuranceAccount.getInsuranceAccountLog();
    }

}