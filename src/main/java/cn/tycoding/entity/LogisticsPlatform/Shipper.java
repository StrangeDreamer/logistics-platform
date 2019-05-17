package cn.tycoding.entity.LogisticsPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Shipper Shipper   发货方   发送订单与货物类的区别：通过发货方计算资金往来
 * 可以固定发货方的个数，使得随机产生的货物依附于发货方，思考下 发货方与货物的区别在哪里:在于金额结算
 * 属性：发送的货物，发货方级别
 */
public class Shipper implements Actor{

    //给予发货方划分等级 1~10 。默认为1级，级别越高，利润分享中获得的收益越高
    private int shipperLevel = 1;
    private final int NUM_MAXCARGO = 100;
    //当前的订单
    private int currentCargoNum = 0;
    private String ID = "-1";
    // 记录该货车是否在平台中被激活
    private boolean activated = false;
    private int registerTime = 0;
    // 经历的tick数；
    private int myTick = 0;

    //保存该发货方发送的的历史订单信息
    private List<Cargo> cargos = new ArrayList<Cargo>();

    // 记录接手过的订单以及该订单获得的收益
    private HashMap<Cargo,Double> cargoBenefit = new HashMap<>();

    //接手过的订单资金流动日志
    private String moneyLog = "";

    public Shipper(int shipperLevel, String ID) {
        this.shipperLevel = shipperLevel;
        this.ID = ID;
    }

    public Shipper(String ID) {
        this.ID = ID;
    }

    public Shipper() {
        shipperLevel = MyTool.getRandom(3,10);
    }

    //TODO 查询发货方的订单列表  API GET logistics/getCurrentCargos/{senderId}
    public String showCurrentStatus(){
        return  "发货方ID:"+ID+" 注册时间:"+MyTool.tickChangeToHour(getRegisterTime())+ " 当前状态:" +  (this.isActivated()?"已激活":"未激活" )+" 当前订单 "+currentCargoNum + " 件\n";
    }

    //TODO 查询发货方相关信息  API GET logistics/getSender/{senderId}
    public String showOverview(){
        return "发货方ID:"+getID()+"  注册时间"+MyTool.tickChangeToHour(getRegisterTime())+"  当前状态:" +  (this.isActivated()?"已激活":"未激活" );
    }

    public int getShipperLevel() {
        return shipperLevel;
    }

    public void setShipperLevel(int shipperLevel) {
        this.shipperLevel = shipperLevel;
    }

    public int getNUM_MAXCARGO() {
        return NUM_MAXCARGO;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }

    public int getCurrentCargoNum() {
        return currentCargoNum;
    }

    public void setCurrentCargoNum(int currentCargoNum) {
        this.currentCargoNum = currentCargoNum;
    }

    public String getID() {
        return ID;
    }

    public void setID(String id) {
        ID = id;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public int getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(int registerTime) {
        this.registerTime = registerTime;
    }

    public int getMyTick() {
        return myTick;
    }

    public void setMyTick(int myTick) {
        this.myTick = myTick;
    }

    public String getMoneyLog() {
        return moneyLog;
    }

    public void setMoneyLog(String moneyLog) {
        this.moneyLog = moneyLog;
    }

    public HashMap<Cargo, Double> getCargoBenefit() {
        return cargoBenefit;
    }

    public void setCargoBenefit(HashMap<Cargo, Double> cargoBenefit) {
        this.cargoBenefit = cargoBenefit;
    }
}
