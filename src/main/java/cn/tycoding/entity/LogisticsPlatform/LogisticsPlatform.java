package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogisticsPlatform 平台方：记录，分发消息，确定承运人，分配利润
 * 属性：货车
 * 方法：判断货车是否有资质接单,注册注销承运方
 *
 */
public class LogisticsPlatform extends Depot implements Actor{
    private String ID = "平台";
    private final int MAXCARGO = 100000;
    private final int MAXSHIPPER = 10000;

    // 平台记录货车、货物、发货方 、收货方。
    // 平台上已注册到达所有车辆
    private List<Truck> trucksInPlatform = new ArrayList<Truck>();

    //平台上所有被接过单的货物订单
    private List<Cargo> cargosInPlatform = new ArrayList<Cargo>();

    //平台上当前公布的等待被接单的订单
    private List<Cargo> cargoPublishing = new ArrayList<Cargo>();

    //平台当前还在执行的货物订单
    private List<Cargo> currentCargosInPlatform = new ArrayList<Cargo>();

    //平台当前完成但还未结算的订单
    private List<Cargo> cargosNeedSettleInPlatform = new ArrayList<Cargo>();

    //平台上所有注册发货方
    private List<Shipper> shippersInPlatform = new ArrayList<Shipper>();

    //平台上所有注册收货方
    private List<Receiver> receiversInPlatform = new ArrayList<Receiver>();

    //实际收益，仅当撤单（展位费）和 订单完成时，才会增加
    private double trueProfit = 0;

    // 记录平台获得的利润
    private double profitOfPlatform = 0;
    //异常订单日志，特指验货不通过的订单
    private String abnormalOrderLog = "";

    // 根据 货车在平台的账号，获取货车在担保方的账号。用map保存
    // 通用，根据平台账号，获取货车在银行的账号。用map保存
    private Map<Integer,Integer> platformID_AndGuarantorID = new HashMap<Integer,Integer>();
    private Map<Integer,Integer> platformID_AndBankID = new HashMap<Integer,Integer>();

    // 记录接手过的订单以及该订单获得的收益
    private HashMap<Cargo,Double> cargoBenefit = new HashMap<>();

    //平台上所有的出价信息日志
    private String billingLog = "抢单日志";
    public LogisticsPlatform(Point position) {
        super(position);
    }

    /** 利润分配的比例，平台从该利润空间中获取r%的利润，
     *  发货方和承运方分别获得（1-r%）/ 2 的利润空间的奖励
     *  之后可进行更改
     */
    private final double platformRate = 0.33;

    public static boolean typeCanBePick(int carryType,int neededCarryType){
        boolean canBePick = false;
        if(carryType == neededCarryType) canBePick = true;
        if(neededCarryType == 1) canBePick = true;
        if(!canBePick){
            System.out.println("车辆类型不符合要求");
        }
        return canBePick;
    }

    // 对利润空间进行分配，获取应该分配的红包金额
    // 参数是原始订单价格，抢单价格，利润分享率，红包最高比例,返回红包金额
    public double bonusOfProfitSharing(double freightFare,double orderPrice,double profitSharingRatio,
                                              double bonusMaxRatio) {
        double bonus = -1;
        double profitMargins = freightFare - orderPrice;  //利润空间
        double profitShouldshare = profitMargins*profitSharingRatio;    //根据利润分享率 获取利润分享
        double maxBonus = freightFare*bonusMaxRatio;                     //根据红包最高比例和原始订单价格获取红包上限

        //实际红包金额为 利润分享和红包上限 的最小值
        bonus = profitShouldshare < maxBonus ? profitShouldshare : maxBonus;
        System.out.println("利润分享为："+profitShouldshare+"; 红包上限为"+maxBonus+"; 实际红包金额为 利润分享和红包上限 的最小值"+bonus);
        return bonus;
    }

    //查询该ID车辆是否存在
    public boolean haveTruckByID(String ID){
        boolean ret = false;
        for(int i =0;i < trucksInPlatform.size(); i++) {
            if(trucksInPlatform.get(i).getID().equals(ID)){
               return true;
            }
        }
        return ret;
    }
    //通过ID寻找车辆，没有则返回null；
    public Truck findTruckByID(String ID){
        Truck ans = null;
        for(int i = 0;i < trucksInPlatform.size();i++){
            if(trucksInPlatform.get(i).getID().equals(ID)){
                ans = trucksInPlatform.get(i);
            }
        }
        return ans;
    }

    //查询该ID货物订单是否存在
    public boolean haveCargoByID(String ID){
        boolean ret = false;
        for(int i =0;i < cargosInPlatform.size(); i++) {
            if(cargosInPlatform.get(i).getID().equals(ID)){
                return true;
            }
        }
        return ret;
    }

    // 查询ID货物是否在公告栏上
    public boolean havePublishingCargoByID(String ID){
        boolean ret = false;
        for(int i =0;i < cargoPublishing.size(); i++) {
            if(cargoPublishing.get(i).getID().equals(ID)){
                return true;
            }
        }
        return ret;
    }

    //通过ID寻找货物订单，没有则返回null；
    public Cargo findCargoByID(String ID){
        Cargo ans = null;
        for(int i = 0;i < cargosInPlatform.size();i++){
            if(cargosInPlatform.get(i).getID().equals(ID)){
                ans = cargosInPlatform.get(i);
            }
        }
        return ans;
    }

    // 通过ID获取公告栏上的货物
    public Cargo findPublishingCargoByID(String ID){
        Cargo ans = null;
        for(int i = 0;i < cargoPublishing.size();i++){
            if (cargoPublishing.get(i).getID().equals(ID)){
                ans = cargoPublishing.get(i);
            }
        }
        return ans;
    }

    //查询该ID发货方是否存在
    public boolean haveShipperByID(String ID){
        boolean ret = false;
        for(int i =0;i < shippersInPlatform.size(); i++) {
            if(shippersInPlatform.get(i).getID().equals(ID)){
                return true;
            }
        }
        return ret;
    }

    //通过ID寻找发货方
    public Shipper findShipperByID(String ID){
        Shipper ans = null;
        for(int i = 0;i < shippersInPlatform.size();i++){
            if(shippersInPlatform.get(i).getID().equals(ID)){
                ans = shippersInPlatform.get(i);
            }
        }
        return ans;
    }

    //查询该ID发货方是否存在
    public boolean haveReceiverByID(String ID){
        boolean ret = false;
        for(int i =0;i < receiversInPlatform.size(); i++) {
            if(receiversInPlatform.get(i).getID().equals(ID)){
                return true;
            }
        }
        return ret;
    }
    //通过ID寻找发货方，没有则返回null；
    public Receiver findReceiverByID(String ID){
        Receiver ans = null;
        for(int i = 0;i < receiversInPlatform.size();i++){
            if(receiversInPlatform.get(i).getID().equals(ID)){
                ans = receiversInPlatform.get(i);
            }
        }
        return ans;
    }

    public int getMAXCARGO() {
        return MAXCARGO;
    }

    public int getMAXSHIPPER() {
        return MAXSHIPPER;
    }



    public List<Truck> getTrucksInPlatform() {
        return trucksInPlatform;
    }

    public void setTrucksInPlatform(List<Truck> trucksInPlatform) {
        this.trucksInPlatform = trucksInPlatform;
    }

    public List<Cargo> getCargosInPlatform() {
        return cargosInPlatform;
    }

    public void setCargosInPlatform(List<Cargo> cargosInPlatform) {
        this.cargosInPlatform = cargosInPlatform;
    }

    public List<Cargo> getCurrentCargosInPlatform() {
        return currentCargosInPlatform;
    }

    public void setCurrentCargosInPlatform(List<Cargo> currentCargosInPlatform) {
        this.currentCargosInPlatform = currentCargosInPlatform;
    }

    public List<Cargo> getCargosNeedSettleInPlatform() {
        return cargosNeedSettleInPlatform;
    }

    public void setCargosNeedSettleInPlatform(List<Cargo> cargosNeedSettleInPlatform) {
        this.cargosNeedSettleInPlatform = cargosNeedSettleInPlatform;
    }

    public List<Shipper> getShippersInPlatform() {
        return shippersInPlatform;
    }

    public void setShippersInPlatform(List<Shipper> shippersInPlatform) {
        this.shippersInPlatform = shippersInPlatform;
    }

    public List<Receiver> getReceiversInPlatform() {
        return receiversInPlatform;
    }

    public void setReceiversInPlatform(List<Receiver> receiversInPlatform) {
        this.receiversInPlatform = receiversInPlatform;
    }

    public double getTrueProfit() {
        return trueProfit;
    }

    public void setTrueProfit(double trueProfit) {
        this.trueProfit = trueProfit;
    }

    public double getProfitOfPlatform() {
        return profitOfPlatform;
    }

    public void setProfitOfPlatform(double profitOfPlatform) {
        this.profitOfPlatform = profitOfPlatform;
    }

    public Map<Integer, Integer> getPlatformID_AndGuarantorID() {
        return platformID_AndGuarantorID;
    }

    public void setPlatformID_AndGuarantorID(Map<Integer, Integer> platformID_AndGuarantorID) {
        this.platformID_AndGuarantorID = platformID_AndGuarantorID;
    }

    public Map<Integer, Integer> getPlatformID_AndBankID() {
        return platformID_AndBankID;
    }

    public void setPlatformID_AndBankID(Map<Integer, Integer> platformID_AndBankID) {
        this.platformID_AndBankID = platformID_AndBankID;
    }

    public double getPlatformRate() {
        return platformRate;
    }

    public List<Cargo> getCargoPublishing() {
        return cargoPublishing;
    }

    public void setCargoPublishing(List<Cargo> cargoPublishing) {
        this.cargoPublishing = cargoPublishing;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getBillingLog() {
        return billingLog;
    }

    public void setBillingLog(String billingLog) {
        this.billingLog = billingLog;
    }

    public String getAbnormalOrderLog() {
        return abnormalOrderLog;
    }

    public void setAbnormalOrderLog(String abnormalOrderLog) {
        this.abnormalOrderLog = abnormalOrderLog;
    }

    public HashMap<Cargo, Double> getCargoBenefit() {
        return cargoBenefit;
    }

    public void setCargoBenefit(HashMap<Cargo, Double> cargoBenefit) {
        this.cargoBenefit = cargoBenefit;
    }




}
