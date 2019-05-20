package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 *  Cargo  货车类 ，同时也是订单类
 *  属性： 货物固有属性：运输类型、体积、重量
 *        订单属性： 1 参与方：发货方，收货方，承运方（如果有），
 *                  2 运费：发货方支付运费，接单报价运费
 *                  3 时间以及货物状态
 *                  4 展示相关
 *                  5 订单转手相关
 *  方法：
 */

public class Cargo extends Parcel implements Actor {

    // 1 货物固有属性：运输类型、体积、重量
    /**运输类型，表示该车辆可以运输什么类型的货物 可继续添加
     * 1 --- 常规物品       ---常规车
     * 2 --- 需要冷藏物品   ---冷链车
     * 3 --- 危险品        ---可运危险品的车
     */

    private String ID = "-";
    private int neededCarryType = 1;
    // 货物的体积和重量
    private int volume = -1;
    private int weight = -1;

    // 2参与方：发货方，收货方，承运方（如果有），
    // 发货人
    private Shipper shipper = null;
    // 收货人
    private Receiver receiver = null;
    // 当前接单的货车(如果有的话)
    private Truck myTruck = null;

    //运费：发货方支付运费，接单报价运费
    // 发布订单的人支付的运费,担保额????????
    private double freightFare = -1;
    // 原始发货方支付的运费
    private double originFreightFare = -1;
    //实际 被接单的价格
    private int orderPrice = -1;
    //违约金 只有担保额大于违约金的承运方才可以接单(liquidatedDamages<freightFare)
    private double liquidatedDamages = freightFare;

    //3 时间约束
    // 货物注册时间 单位 tick
    private int registerTime = 0;
    // 货物注册到运达允许花费的小时数 单位 小时
    private int limitedHour = -1;
    //作为新订单还可以停留在平台公告牌上的时间 单位 tick
    private int publishingLife = -1;
    // 货物当前的状态，货车状态有7种 1 未被接单； 2 已接未运； 3 已运  4 已运达等待收货   5 转手中（正挂在平台公告栏上）6 订单异常  7 订单完结
    private int cargoStatus = 1;
    // 经历的tick数；当订单完成时，停止tick计时
    private int myTick = 0;
    // 订单完成时是否超时
    private boolean overTime = false;

    // 4 展示相关
    // 记录该订单的历史报价信息
    private String recodeBidding = " ";
    //记录该订单的结算信息
    private String recodeSettlement = "该订单暂未完成";
    //备注，可不填写
    private String remarks = "-";
    //出发地坐标、目的地坐标
    private int departureX,departureY;
    private int destinationX,destinationY;
    private String carrierCheck = "";
    private String shipperCheck = "";
    //记录该订单是否存在有效报价
    private boolean haveBid = false;
    //记录每辆车的报价，顺序为平台注册车辆的顺序 ,出价一律为整数
    private Map<Truck,Integer> tempbid= new HashMap<Truck,Integer>();
    //字符串保存所有报价信息
    private String myBidding = "";
    //订单完成状况
    private String myEnd = "该订单尚未完成";

    // 5 订单转手相关
    // 判断是否有订单转手， 是否需要特殊的计算方式
    private boolean haveOrderChange = false;
    // 使用3个栈分别保存：历任承运方信息；历任发货出价；历任接单报价；
    private Stack<Truck> truckStack = new Stack<>();
    private Stack<Double> offerPriceStack = new Stack<>();
    private Stack<Double> bidPriceStack = new Stack<>();
    // 订单转手相关状态 0 没有转手  1 转手中
    private int changeOrderStatus = 0;

    // 6 其他
    // 订单进行过验货
    private boolean alreadyCheck = false;
    // 订单收货通过
    private boolean receiverConfirm = false;
    // 记录订单 接单、接货、运达时间 的日志
    private String cargoTimeLog = "";

    Cargo(ParcelDTO dto) {
        super(dto);
        limitedHour = MyTool.getRandom(6,96);
        departureX = (int)MyTool.getCoordinates(this.getPickupLocation()).x;
        departureY = (int)MyTool.getCoordinates(this.getPickupLocation()).y;
        destinationX = (int)MyTool.getCoordinates(this.getDeliveryLocation()).x;
        destinationY = (int)MyTool.getCoordinates(this.getDeliveryLocation()).y;
    }

    // 货物订单信息展示
    public String cargoDescription(){
        DecimalFormat df = new DecimalFormat(".00");
        String result; //保存订单信息
        result = this.getID() + "号货物   发货方"+ getShipper().getID()+
                " \n运费:"+ df.format(this.freightFare) + " 抢单价格:"+ df.format(this.orderPrice) +
                ";\n运输类型:" + MyTool.getTypeString(this.neededCarryType) +
                "; 货物重量:" + this.getWeight() + "; 货物体积:" + this.getVolume()
                + ";\n出发地坐标(" + departureX + "," + departureY
                + "); 目的地坐标(" + destinationX + ","+ destinationY + "）"
                +";\n运输时间限制：" + limitedHour + "小时 "
                +";\n已用时：" + getUsedHour() + "小时 "
                + "\n货物当前  "+getMyStatus()+" \n";
        System.out.println(result);
        return result;
    }

    // 货物订单信息展示 不包含抢单价格
    public String cargoDescriptionWithoutOrderPrice(){
        DecimalFormat df = new DecimalFormat(".00");
        String result; //保存订单信息
        result = this.getID() + "号货物"
                + " 运费:"+ df.format(this.freightFare) + " 赔偿金:" + df.format(this.liquidatedDamages)
                + ";\n运输类型:" + MyTool.getTypeString(this.neededCarryType)
                + "; 货物重量:" + this.getWeight() + "; 货物体积:" + this.getVolume()
                + ";\n出发地坐标(" + departureX + "," + departureY
                + "); 目的地坐标(" + destinationX + ","+ destinationY + "）"
                +";\n运输剩余时间：" + getRemainHour() + "小时 "
                +";\n已用时：" + getUsedHour() + "小时 "
                + "\n货物当前  "+getMyStatus()+" \n";
        System.out.println(result);
        return result;
    }

    // 货物当前的状态，货车状态有7种 1 未被接单； 2 已接未运； 3 已运  4 已运达等待收货   5 转手中（正挂在平台公告栏上）6 订单异常  7 订单完结
    public String getMyStatus(){
        if(getCargoStatus() == 1){
           return "还未被接单";
        }
        if(getCargoStatus() == 2){
            return "已接未运";
        }
        if(getCargoStatus() == 3){
            return "运输中";
        }
        if(getCargoStatus() == 4){
            return "已运达等待收货";
        }
        if(getCargoStatus() == 5){
            return "转手订单";
        }
        if(cargoStatus == 6) {
            return "订单异常";
        }
        if(cargoStatus == 7) {
            return "订单完结";
        }

        return "";
    }



    //获取该订单剩余小时数
    public int getRemainHour(){
        return limitedHour - getUsedHour();
    }

    //获取已经耗费的小时数
    public int getUsedHour(){
        return MyTool.tickChangeToHour(myTick);
    }

    public int getNeededCarryType() {
        return neededCarryType;
    }

    public void setNeededCarryType(int neededCarryType) {
        this.neededCarryType = neededCarryType;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public Truck getMyTruck() {
        return myTruck;
    }

    public void setMyTruck(Truck myTruck) {
        this.myTruck = myTruck;
    }

    public double getFreightFare() {
        return freightFare;
    }

    public void setFreightFare(double freightFare) {
        this.freightFare = freightFare;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }


    public int getCargoStatus() {
        return cargoStatus;
    }

    public void setCargoStatus(int cargoStatus) {
        this.cargoStatus = cargoStatus;
    }

    public String getRecodeBidding() {
        return recodeBidding;
    }

    public String getRecodeSettlement() {
        return "订单详情如下：\n" + cargoDescription() + "\n接单车辆为：" + myTruck.getID()+"\n";
    }

    public void setRecodeSettlement(String recodeSettlement) {
        this.recodeSettlement = recodeSettlement;
    }

    public boolean isHaveOrderChange() {
        return haveOrderChange;
    }

    public void setHaveOrderChange(boolean haveOrderChange) {
        this.haveOrderChange = haveOrderChange;
    }


    public double getLiquidatedDamages() {
        return liquidatedDamages;
    }

    public void setLiquidatedDamages(double liquidatedDamages) {
        this.liquidatedDamages = liquidatedDamages;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getID() {
        return ID;
    }

    public void setID(String id) {
        ID = id;
    }

    public int getDepartureX() {
        return departureX;
    }

    public void setDepartureX(int departureX) {
        this.departureX = departureX;
    }

    public int getDepartureY() {
        return departureY;
    }

    public void setDepartureY(int departureY) {
        this.departureY = departureY;
    }

    public int getDestinationX() {
        return destinationX;
    }

    public void setDestinationX(int destinationX) {
        this.destinationX = destinationX;
    }

    public int getDestinationY() {
        return destinationY;
    }

    public void setDestinationY(int destinationY) {
        this.destinationY = destinationY;
    }

    public Map<Truck, Integer> getTempbid() {
        return tempbid;
    }

    public void setTempbid(Map<Truck, Integer> tempbid) {
        this.tempbid = tempbid;
    }

    public boolean isHaveBid() {
        return haveBid;
    }

    public void setHaveBid(boolean haveBid) {
        this.haveBid = haveBid;
    }

    public int getPublishingLife() {
        return publishingLife;
    }

    public void setPublishingLife(int publishingLife) {
        this.publishingLife = publishingLife;
    }

    public String getCarrierCheck() {
        return carrierCheck;
    }

    public void setCarrierCheck(String carrierCheck) {
        this.carrierCheck = carrierCheck;
    }

    public String getShipperCheck() {
        return shipperCheck;
    }

    public void setShipperCheck(String shipperCheck) {
        this.shipperCheck = shipperCheck;
    }

    public String getMyBidding() {
        return myBidding;
    }

    public void setMyBidding(String myBidding) {
        this.myBidding = myBidding;
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

    public int getLimitedHour() {
        return limitedHour;
    }

    public void setLimitedHour(int limitedHour) {
        this.limitedHour = limitedHour;
    }

    public String getMyEnd() {
        return myEnd;
    }

    public void setMyEnd(String myEnd) {
        this.myEnd = myEnd;
    }

    public boolean isOverTime() {
        return overTime;
    }

    public void setOverTime(boolean overTime) {
        this.overTime = overTime;
    }

    public void setRecodeBidding(String recodeBidding) {
        this.recodeBidding = recodeBidding;
    }

    public int getChangeOrderStatus() {
        return changeOrderStatus;
    }

    public void setChangeOrderStatus(int changeOrderStatus) {
        this.changeOrderStatus = changeOrderStatus;
    }

    public Stack<Double> getOfferPriceStack() {
        return offerPriceStack;
    }

    public void setOfferPriceStack(Stack<Double> offerPriceStack) {
        this.offerPriceStack = offerPriceStack;
    }

    public Stack<Double> getBidPriceStack() {
        return bidPriceStack;
    }

    public void setBidPriceStack(Stack<Double> bidPriceStack) {
        this.bidPriceStack = bidPriceStack;
    }

    public Stack<Truck> getTruckStack() {
        return truckStack;
    }

    public void setTruckStack(Stack<Truck> truckStack) {
        this.truckStack = truckStack;
    }

    public double getOriginFreightFare() {
        return originFreightFare;
    }

    public void setOriginFreightFare(double originFreightFare) {
        this.originFreightFare = originFreightFare;
    }

    public boolean isReceiverConfirm() {
        return receiverConfirm;
    }

    public void setReceiverConfirm(boolean receiverConfirm) {
        this.receiverConfirm = receiverConfirm;
    }

    public boolean isAlreadyCheck() {
        return alreadyCheck;
    }

    public void setAlreadyCheck(boolean alreadyCheck) {
        this.alreadyCheck = alreadyCheck;
    }

    public String getCargoTimeLog() {
        return cargoTimeLog;
    }

    public void setCargoTimeLog(String cargoTimeLog) {
        this.cargoTimeLog = cargoTimeLog;
    }
}

