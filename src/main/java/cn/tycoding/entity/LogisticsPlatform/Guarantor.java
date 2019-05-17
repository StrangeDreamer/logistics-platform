package cn.tycoding.entity.LogisticsPlatform;

public class Guarantor extends MoneyControler implements Actor{
    private String ID = "担保方";
    //TODO 担保方注册 POST /logistics/guarantor
    @Override
    public void register(Actor actor, double amount) {
        account_MoneyMap.put(actor,amount);
        account_FrozenMoneyMap.put(actor,0.0);
        account_DisposableMoneyMap.put(actor,amount);
        moneyLog = moneyLog + "\n新账户[" + actor.getID() + "]注册，担保额度为 " + df.format(amount);
    }
    @Override
    public String getID() {
        return ID;
    }
}
