package cn.tycoding.entity.LogisticsPlatform;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MoneyControler {
    protected DecimalFormat df = new DecimalFormat(".00");
    // 绑定参与者的账号 保存其账户金额
    protected Map<Actor,Double> account_MoneyMap = new HashMap<Actor, Double>();
    //绑定参与者的账号 保存其在冻结的金额
    protected Map<Actor,Double> account_FrozenMoneyMap = new HashMap<Actor, Double>();
    //绑定参与者的账号 保存其实际可支配的金额
    protected Map<Actor,Double> account_DisposableMoneyMap = new HashMap<Actor, Double>();
    protected String moneyLog = "日志";

    // TODO 账户注册，客户可以是承运方、发货方、平台
    public void register(Actor actor, double amount) {
        account_MoneyMap.put(actor,amount);
        account_FrozenMoneyMap.put(actor,0.00);
        account_DisposableMoneyMap.put(actor,amount);
        moneyLog = moneyLog + "\n新账户[" + actor.getID() + "]注册，注册资金为 " + df.format(amount);
    }

    // 判断账户拥有的可支配资金是否达到目标金额
    public boolean enoughAmountWith(Actor actor,double amount) {
        if(!account_DisposableMoneyMap.containsKey(actor)) {
            moneyLog = moneyLog + "\n账户[" + actor.getID() + "]可用资金不足" + df.format(amount);
            return false;
        }
        if(account_DisposableMoneyMap.get(actor) >= amount) {
            return true;
        }
        return false;
    }

    // 金额转移，a账户向b账户转移money数值的资金
    public boolean transfer(Actor a,Actor b,double money) {
        if(!enoughAmountWith(a,money)){
            return false;
        }
        if (money < 0) {
            return false;
        }
        account_MoneyMap.put(a,account_MoneyMap.get(a) - money);
        account_MoneyMap.put(b,account_MoneyMap.get(b) + money);
        account_DisposableMoneyMap.put(a,account_DisposableMoneyMap.get(a) - money);
        account_DisposableMoneyMap.put(b,account_DisposableMoneyMap.get(b) + money);
        moneyLog = moneyLog + "\n账户[" + a.getID() + "]向账户[" + b.getID() + "]支付了" + df.format(money)
        + ";当前账户" + a.getID()+ "余额为" + df.format(account_MoneyMap.get(a))
                + ";当前账户" + b.getID() + "余额为" + df.format(account_MoneyMap.get(b));
        return true;
    }

    //冻结账户指定金额
    public boolean freezeMoney(Actor actor,double money) {
        if(!enoughAmountWith(actor,money)){
            moneyLog = moneyLog + "\n账户[" + actor.getID() + "]可用资金不足，冻结失败";
            return false;
        }
        if (money < 0) {
            return false;
        }
        account_FrozenMoneyMap.put(actor, account_FrozenMoneyMap.get(actor) + money);
        account_DisposableMoneyMap.put(actor,account_DisposableMoneyMap.get(actor) - money);
        moneyLog = moneyLog + "\n账户[" + actor.getID() + "]被冻结了资金 " + df.format(money)
                + ";目前可用资金为" + df.format(account_DisposableMoneyMap.get(actor));
        return true;
    }

    // 恢复解冻指定金额
    public void freezeMoneyRecovery(Actor actor,double money) {
        if(!account_FrozenMoneyMap.containsKey(actor) || money < 0 || account_FrozenMoneyMap.get(actor) < money) {
            return;
        }
        account_FrozenMoneyMap.put(actor,account_FrozenMoneyMap.get(actor) - money);
        account_DisposableMoneyMap.put(actor,account_DisposableMoneyMap.get(actor) + money);
        moneyLog = moneyLog + "\n账户[" + actor.getID() + "]被解冻了资金 " + df.format(money)
        + ";目前可用资金为" + df.format(account_DisposableMoneyMap.get(actor));
    }

    public Map<Actor, Double> getAccount_MoneyMap() {
        return account_MoneyMap;
    }

    public void setAccount_MoneyMap(Map<Actor, Double> account_MoneyMap) {
        this.account_MoneyMap = account_MoneyMap;
    }

    public Map<Actor, Double> getAccount_FrozenMoneyMap() {
        return account_FrozenMoneyMap;
    }

    public void setAccount_FrozenMoneyMap(Map<Actor, Double> account_FrozenMoneyMap) {
        this.account_FrozenMoneyMap = account_FrozenMoneyMap;
    }

    public Map<Actor, Double> getAccount_DisposableMoneyMap() {
        return account_DisposableMoneyMap;
    }

    public void setAccount_DisposableMoneyMap(Map<Actor, Double> account_DisposableMoneyMap) {
        this.account_DisposableMoneyMap = account_DisposableMoneyMap;
    }

    public String getMoneyLog() {
        return moneyLog;
    }

    public void setMoneyLog(String moneyLog) {
        this.moneyLog = moneyLog;
    }

}
