
package cn.tycoding.service;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-09-26 11:15
 * 4
 */

import cn.tycoding.domain.BankAccount;
import cn.tycoding.exception.BankAccountException;
import cn.tycoding.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * 红包结算相关的方法
 */

@Service
public class BonusService {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    DecimalFormat df = new DecimalFormat("#.00");


    /**
     * @param bankAccount  账户
     * @param money 原本需要支付的金额
     * @return 实际需要支付的金额
     *      * 先检查账户红包，如果红包额度大于指定数额，则扣除红包数值，返回实际要花费金额0，即红包完全抵扣花费；如果红包额度小于指定花费，则返回实际要花费的资金
     */
    @Transactional
    public double spendWithBonus(BankAccount bankAccount, double money) {
        // 红包额度充足
        if(bankAccount.getBonus() >= money && bankAccount.getBonus() >= 0 && money >= 0) {
            // 扣除相应红包额度
            bankAccount.setBonus(bankAccount.getBonus() - money);
            // 实际只需要支付0资金
            return 0;
        } else if (bankAccount.getBonus() < money && bankAccount.getBonus() >= 0 && money >= 0) {
            double spend = money - bankAccount.getBonus();
            // 红包额度不足，则全部用来抵消费用,之后红包数值变为0
            bankAccount.setBonus(0);
            // 实际需要支付 花费减去红包 的资金
            return spend;
        } else {
            throw new BankAccountException("红包额度出错!");
        }
    }

    /**
     * @param bankAccount
     * @param bonus
     * @return
     * 账户获得红包
     */
    @Transactional
    public BankAccount getBonus(BankAccount bankAccount,double bonus) {
        if(bonus < 0) {
            throw new BankAccountException("红包数值不能为负数");
        }
        bankAccount.setBonus(bankAccount.getBonus() + bonus);
        bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() + "，目前红包余额为" + df.format(bankAccount.getBonus()));

        return bankAccount;
    }



}