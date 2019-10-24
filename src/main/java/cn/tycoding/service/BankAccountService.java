package cn.tycoding.service;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 11:15
 * 4
 */

import cn.tycoding.domain.BankAccount;
import cn.tycoding.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 1 由于只做模拟，不提供银行账户的注册注销接口；不提供设置账户初始资金的接口
 * 2 在进行所有增删前，先判断有没有该账户，没有则自动注册
 * 3 要实现的接口包含：查询所有账户；查看某一账户资金流水和开始和结束的金额；
 * 4 要实现的功能：增减某一账户的可用金额（对应账户的冻结解冻资金）；
 * 增减某一账户某一金额；查询某一账户当前可用金额
 */

@Service
public class BankAccountService {

    private final Logger logger = LoggerFactory.getLogger(BankAccountService.class);
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private BonusService bonusService;

    // 查询所有的注册账户
    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    // 检查 该参与方是否存在，没有则自动创建,如果已经存在则直接返回该账户
    @Transactional
    public synchronized BankAccount check(int id, String type) {
        if (type.equals("platform")) {
            type = "平台";
        } else if (type.equals("shipper")) {
            type = "发货方";
        } else if (type.equals("receiver")) {
            type = "收货方";
        } else if (type.equals("truck")) {
            type = "承运方";
        } else {
            logger.info(type);
            return null;
        }
        BankAccount bankAccount = bankAccountRepository.findBankAccountByIdAndType(id, type);

        if (bankAccount == null) {
            bankAccount = new BankAccount();
            bankAccount.setId(id);
            bankAccount.setType(type);
            logger.info("该银行账户不存在，自动为其创建银行账户！");
            if (type.equals("平台") || type.equals("platform")) {
                bankAccount.setMoney(0);
                bankAccount.setAvailableMoney(0);
            }
            bankAccount.setMoney((int) (100000 + 500000 * Math.random()));
            bankAccount.setAvailableMoney(bankAccount.getMoney());
            bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() + "参与方初始资金为" + String.format("%.2f",bankAccount.getMoney())
            + "，参与方初始可用资金为" + String.format("%.2f",bankAccount.getMoney()));

            if (type.equals("发货方") || type.equals("shipper")) {
                bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() +"，参与方初始红包为" + String.format("%.2f", bankAccount.getBonus()));
            }

            bankAccountRepository.save(bankAccount);
        } else {
            logger.info(type + id + "该银行账户存在！");

        }
        return bankAccount;
    }

    // 查询指定注册账户的流水
    public String findMoneyLog(int id, String type) {

        BankAccount bankAccount = check(id, type);
        String result = bankAccount.getBankAccountLog() + "\n参与方现有资金为" + String.format("%.2f",bankAccount.getMoney())
                + "    可用余额为" + String.format("%.2f",bankAccount.getAvailableMoney());
        if (type.equals("shipper") || type.equals("发货方")) {
            result = result + "    当前红包为" + String.format("%.2f",bankAccount.getBonus());
        }

        if (type.equals("truck") || type.equals("承运方")) {
            result = result + "\n\n" + insuranceAccountService.check(id, "truck").getInsuranceAccountLog()
                    + "\n可用担保额为" + insuranceAccountService.check(id, "truck").getAvailableMoney();
        }
        return result;
    }

    // 冻结资金，如果money为正则为解冻，money为负数则为冻结
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    @Transactional
    public boolean changeAvailableMoney(int id, String type, double money) {
        BankAccount bankAccount = check(id, type);
        return changeAvailableMoney(bankAccount, money);
    }

    // 冻结资金，如果money为正则为解冻，money为负数则为冻结
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    // 额外增加对是否为发货方的判断，虽然目前只有发货方才会用到冻结资金
    @Transactional
    public boolean changeAvailableMoney(BankAccount bankAccount, double money) {
        if (bankAccount.getAvailableMoney() < money) {
            logger.info("冻结失败，资金不足！");
            return false;
        }
        bankAccount.setAvailableMoney(bankAccount.getAvailableMoney() + money);
        if (money > 0) {
            bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() +
                    ", " + bankAccount.getType() + bankAccount.getId() + "解冻资金" + String.format("%.2f", money));
        } else {
            bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() +
                    ", " + bankAccount.getType() + bankAccount.getId() + "被冻结资金" + String.format("%.2f", (0 - money)));
        }

        String type = bankAccount.getType();
        String result =  "参与方现有资金为" + String.format("%.2f",bankAccount.getMoney())
                + "    可用余额为" + String.format("%.2f",bankAccount.getAvailableMoney());
        if (type.equals("shipper") || type.equals("发货方")) {
            result = result + "    当前红包为" + String.format("%.2f",bankAccount.getBonus());
        }
        addMoneyLog(bankAccount,result);

        bankAccountRepository.save(bankAccount);
        return true;
    }

    // 资金变动，A向B支付money数值的钱
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    @Transactional
    public boolean transferMoney(int A_id, String A_type, int B_id, String B_type, double money) {
        BankAccount bankAccountA = check(A_id, A_type);
        BankAccount bankAccountB = check(B_id, B_type);
        return transferMoney(bankAccountA, bankAccountB, money);
    }

    // 资金变动，A向B支付money数值的钱
    // 如果转账成功则返回true，失败则返回false，表示资金不足，不可以转账
    @Transactional
    public boolean transferMoney(BankAccount bankAccountA, BankAccount bankAccountB, double money) {
        if (bankAccountA.getMoney() < money) {
            logger.info("转账失败，转账人的资金不足！");
            return false;
        }
        // 每一笔转账前进行判断是否需要考虑红包,这里只考虑发货方向平台单方支付时的红包结算。 主要是对money对改变和log对变化
        // 如果是平台向发货方支付红包，则在外面直接结算并增加log
        if (bankAccountA.getType().equals("发货方") && bankAccountA.getBonus() > 0) {
            double spendWithBonus = bonusService.spendWithBonus(bankAccountA, money);
            bankAccountA.setBankAccountLog(bankAccountA.getBankAccountLog()
                    + ", " + bankAccountA.getType() + bankAccountA.getId()
                    + "减少资金" + String.format("%.2f", money)
                    + "，其中红包支付了" + String.format("%.2f", money - spendWithBonus)
                    + "，目前红包余额为" + String.format("%.2f", bankAccountA.getBonus())
            );
            bankAccountB.setBankAccountLog(bankAccountB.getBankAccountLog()
                    + ", " + bankAccountB.getType() + bankAccountB.getId()
                    + "增加资金" + String.format("%.2f", money )
                    + "，其中红包支付了" + String.format("%.2f", money - spendWithBonus)
            );
            money = spendWithBonus;
            bankAccountA.setMoney(bankAccountA.getMoney() - money);
            bankAccountB.setMoney(bankAccountB.getMoney() + money);
            bankAccountA.setAvailableMoney(bankAccountA.getAvailableMoney() - money);
            bankAccountB.setAvailableMoney(bankAccountB.getAvailableMoney() + money);
        } else {
            //无红包支付支付
            bankAccountA.setMoney(bankAccountA.getMoney() - money);
            bankAccountB.setMoney(bankAccountB.getMoney() + money);
            bankAccountA.setAvailableMoney(bankAccountA.getAvailableMoney() - money);
            bankAccountB.setAvailableMoney(bankAccountB.getAvailableMoney() + money);
            bankAccountA.setBankAccountLog(bankAccountA.getBankAccountLog()
                    + ", " + bankAccountA.getType() + bankAccountA.getId() + "减少资金" + String.format("%.2f", money));
            bankAccountB.setBankAccountLog(bankAccountB.getBankAccountLog()
                    + ", " + bankAccountB.getType() + bankAccountB.getId() + "增加资金" + String.format("%.2f", money));
        }
        bankAccountRepository.save(bankAccountA);
        bankAccountRepository.save(bankAccountB);
        String type = bankAccountA.getType();
        String result = "参与方现有资金为" + String.format("%.2f",bankAccountA.getMoney())
                + "    可用余额为" + String.format("%.2f",bankAccountA.getAvailableMoney());
        if (type.equals("shipper") || type.equals("发货方")) {
            result = result + "    当前红包为" + String.format("%.2f",bankAccountA.getBonus());
        }
        addMoneyLog(bankAccountA,result);

        type = bankAccountB.getType();
        result = "参与方现有资金为" + String.format("%.2f",bankAccountB.getMoney())
                + "    可用余额为" + String.format("%.2f",bankAccountB.getAvailableMoney());
        if (type.equals("shipper") || type.equals("发货方")) {
            result = result + "    当前红包为" + String.format("%.2f",bankAccountB.getBonus());
        }
        addMoneyLog(bankAccountB,result);

        return true;
    }

    // 查看当前可用资金
    public double getAvailableMoney(int id, String type) {
        BankAccount bankAccount = check(id, type);
        return bankAccount.getAvailableMoney();
    }

    // 添加流水记录
    public String addMoneyLog(BankAccount bankAccount, String log) {
        bankAccount.setBankAccountLog(bankAccount.getBankAccountLog() + "\n" + log);
        bankAccountRepository.save(bankAccount);
        return bankAccount.getBankAccountLog();
    }

}




