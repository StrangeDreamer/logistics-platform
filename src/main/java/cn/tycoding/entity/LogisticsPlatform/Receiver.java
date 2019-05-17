package cn.tycoding.entity.LogisticsPlatform;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver 收货方：验货
 * 属性：
 * 方法：验货
 */

public class Receiver implements Actor{

    //保存该收货方要收获的历史订单信息
    private List<Cargo> cargos = new ArrayList<Cargo>();
    //保存等待收货确认的订单
    private List<Cargo> cargosNeedConfirm = new ArrayList<Cargo>();
    private int currentCargoNum = 0;
    private String ID = "-";
    // 记录该货车是否在平台中被激活
    private boolean activated = false;
    //注册时间
    private int registerTime = 0;
    // 经历的tick数；
    private int myTick = 0;

    public Receiver() {
    }

    public Receiver(String ID) {
        this.ID = ID;
    }

    // TODO 收货方根据货物当前的状态进行验货。作为一个API
    public boolean SuccessfulInspection() {
        boolean ret = false;
        if(Math.random() < 0.95){
            System.out.println("验货成功！");
            ret = true;
        }
        return  ret;
    }
    //TODO 查询收货方的订单列表  API GET logistics/getCurrentCargos/{recieverId}
    public String showCurrentStatus(){
        return  "收货方ID"+ID+" 注册时间:"+MyTool.tickChangeToHour(getRegisterTime())+ "; 当前状态:" +  (this.isActivated()?"已激活":"未激活" )+" 当前订单 "+currentCargoNum + " 件 \n";
    }

    //TODO 查询收货方相关信息  API GET logistics/getRecriver/{recieverId}
    public String showOverview(){
        return "收货方ID:"+getID()+"  注册时间"+MyTool.tickChangeToHour(getRegisterTime())+"  当前状态:" +  (this.isActivated()?"已激活":"未激活" );
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
}
