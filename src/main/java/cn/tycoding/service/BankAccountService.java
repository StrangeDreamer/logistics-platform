package cn.tycoding.service;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 11:15
 * 4
 */

import cn.tycoding.domain.BankAccount;
import cn.tycoding.exception.BankAccountException;
import cn.tycoding.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 1 由于只做模拟，不提供银行账户的注册注销接口；不提供设置账户初始资金的接口
 * 2 在进行所有增删前，先判断有没有该账户，没有则自动注册
 * 3 要实现的接口包含：查询所有账户；查看某一账户资金流水和开始和结束的金额；
 * 4 要实现的功能：增减某一账户的可用金额（对应账户的冻结解冻资金）；
 *   增减某一账户某一金额；查询某一账户当前可用金额
 */

@Repository
public class BankAccountService {

    private final Logger logger = LoggerFactory.getLogger(BankAccountService.class);
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired

    // 查询所有的注册账户
    public List<BankAccount> findAll(){
        return bankAccountRepository.findAll();
    }

    // 检查 该参与方是否存在，没有则自动创建,如果已经存在则直接返回该账户
    public BankAccount check (int id, String type) {
        BankAccount bankAccount = bankAccountRepository.findBankAccountByIdAndType(id, type);

        if (bankAccount == null) {
            bankAccount = new BankAccount();
            bankAccount.setId(id);
            bankAccount.setType(type);
            logger.info("该银行账户不存在，自动为其创建银行账户！");

            bankAccountRepository.save(bankAccount);
        } else {
        }

        return bankAccount;
    }

    // 查询指定注册账户的流水
    public String findMoneyLog(int id, String type) {
        BankAccount bankAccount = check(id, type);
        return bankAccount.getBankAccountLog() + "\n参与方当前资金为" + bankAccount.getMoney();
    }

    // 冻结资金，如果money为正则为解冻，money为负数则为冻结
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    public boolean changeAvailableMoney(int id, String type, double money) {
        BankAccount bankAccount = check(id,type);
        return changeAvailableMoney(bankAccount, money);
    }

    // 冻结资金，如果money为正则为解冻，money为负数则为冻结
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    public boolean changeAvailableMoney(BankAccount bankAccount, double money) {
        if (bankAccount.getAvailableMoney() < money) {
            logger.info("冻结失败，资金不足！");
            return false;
        }
        bankAccount.setAvailableMoney(bankAccount.getAvailableMoney() + money);
        if (money > 0) {
            bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() +
                    "\n" + bankAccount.getType() + bankAccount.getId() + "解冻担保额" + String.format("%.2f",money));
        } else {
            bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() +
                    "\n" + bankAccount.getType() + bankAccount.getId() +"冻结担保额" + String.format("%.2f",(0-money)));
        }
        bankAccountRepository.save(bankAccount);
        return true;
    }

    // 资金变动，A向B支付money数值的钱
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    public boolean transferMoney(int A_id, String A_type, int B_id, String B_type, double money) {
        BankAccount bankAccountA = check(A_id, A_type);
        BankAccount bankAccountB = check(B_id, B_type);
        return transferMoney(bankAccountA, bankAccountB, money);
    }

    // 资金变动，A向B支付money数值的钱
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    public boolean transferMoney(BankAccount bankAccountA, BankAccount bankAccountB, double money) {
        if (bankAccountA.getMoney() < money) {
            logger.info("转账失败，转账人的资金不足！");
            return false;
        }
        if (bankAccountA.getAvailableMoney() < money) {
            logger.info("转账失败，转账人的可用资金不足！");
            return false;
        }
        bankAccountA.setMoney(bankAccountA.getMoney() - money);
        bankAccountB.setMoney(bankAccountB.getMoney() + money);
        bankAccountA.setAvailableMoney(bankAccountA.getAvailableMoney() - money);
        bankAccountB.setAvailableMoney(bankAccountB.getAvailableMoney() + money);
        bankAccountA.setBankAccountLog(bankAccountA.getBankAccountLog()
                + "\n" + bankAccountA.getType() + bankAccountA.getId() +"减少资金" + String.format("%.2f",money));
        bankAccountB.setBankAccountLog(bankAccountB.getBankAccountLog()
                + "\n" + bankAccountB.getType() + bankAccountB.getId() +"增加资金" + String.format("%.2f",money));
        bankAccountRepository.save(bankAccountA);
        bankAccountRepository.save(bankAccountB);
        return true;
    }

    // 查看当前可用资金
    public double getAvailableMoney(int id, String type) {
        BankAccount bankAccount = check(id, type);
        return  bankAccount.getAvailableMoney();
    }

    // 添加流水记录
    public String addMoneyLog(BankAccount bankAccount, String log) {
        bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() + "\n" + log);
        bankAccountRepository.save(bankAccount);
        return bankAccount.getBankAccountLog();
    }

}


