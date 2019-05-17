package cn.tycoding.entity.LogisticsPlatform;

import java.io.Serializable;

public class Bank extends MoneyControler implements Actor ,Serializable {
    private String ID = "银行";

    //TODO 银行注册 POST /logistics/bank
    @Override
    public void register(Actor actor, double amount) {
        account_MoneyMap.put(actor,amount);
        account_FrozenMoneyMap.put(actor,0.00);
        account_DisposableMoneyMap.put(actor,amount);
        moneyLog = moneyLog + "\n银行新账户[" + actor.getID() + "]进行了注册！\n注册金额为" + df.format(amount);
    }
    @Override
    public String getID() {
        return ID;
    }
}
