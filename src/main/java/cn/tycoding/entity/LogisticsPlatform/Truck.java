package cn.tycoding.entity.LogisticsPlatform;//

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Scanner;

/**
 * = Carrier 承运方：判断、过滤信息，接单，运输，可以有自己的运输路网范围
 * 货车类/承运方，包含 货车ID，购买的保险额度
 * 属性：承运方编号numToShow
 * 方法：前往接单；运货；显示自身信息；路径规划（通过控制台）；
 *
 * 功能一：货车拥有过滤选项cargoFilter，当打开过滤，距离自己较远当货物将不再参与抢单。
 * 功能二：给予货车 "路径规划" 2种模式：自动模式（路径规划随机自动决定）；手动模式（路径规划取决于控制台输入）
 */
class Truck extends Vehicle implements Actor{

    // 自动模式开关, 这个开关会通过构造函数与TruckLogistics中但模式开关保持一致
    private  static  boolean AutomaticModeOpen = false;
    // 过滤选项，默认关闭
    private boolean openCargoFliter = false;

    // 真正控制演示中 车速的属性           之前遇到车辆速度过快的罪魁祸首
    private static final double SPEED = 5000d;
    // 货车可搭载货物类型，在系统创建的时候随机赋予
    private int carryType = 1;

    // 货车的 最大重量 ；最大体积 ；当前体积；当前重量
    private int currentVolume = -1;
    private int currentWeight = -1;

    // 车牌号前缀，车牌号设置为：前缀 + 货车编号
    private static String plateToShow = "沪A10";
    // 平台给予货车的编号，用于显示货车编号
    private String ID = "-";
    // 用来记录货车是否要在平台上展示出来
    private boolean showInGraph = true;

    // 用来表示当前是否有订单结束，需要进行资金结算。
    private boolean endOfOrder = false;
    // 用来记录要进行结算的货物订单
    private Cargo cargoNeedEnd = null;

    // 货车/承运方 的客户评级，评级越高，获得的利润分享越高 ;如果是特殊客户，评级不会动态改变
    private boolean spacial = false;
    private int truckLevel = 0;

    // 已经接单还没有运输的货物以及数量
    private Cargo[] myToDoCargo= new Cargo[100];
    private int myToDoCargoNum = 0;
    // 正在运输的货物以及数量
    //️ 因为是对象数组，无法获得该对象的个数，故使用一个变量专门记录数组中该对象的个数
    private Cargo[] myDoingCargo= new Cargo[100];
    private int myDoingCargoNum = 0;

    // 记录当前是否有新的货物要运
    private boolean haveNewOrder = false;
    // 记录最近/最新接到的订单
    private Cargo newOrder = null;

    // 记录当前要执行的操作 1表示前往并完成订单装货；2表示将该订单运达
    private int truckDoing = -1;
    // 记录当前要执行的订单下标；
    private int currentOrder = -1;

    // 保存承运方所注册的担保方
    private Guarantor guarantor = null;

    // 货车所承运的历史订单记录
    private String cargoRecode = "该货车已完成的历史订单如下:\n";

    // 记录该货车是否在平台中被激活
    private boolean activated = false;

    //承运方注册时间
    private int registerTime = 0;
    // 经历的tick数；
    private int myTick = 0;

    // 记录特殊货物完结的日志，包括撤单、订单转手
    private String spacialCargoEndLog = "";

    // 记录资金往来日志
    private String moneyLog = "";

    // 记录担保额度变化
    private String guaranteeLog = "";

    // 记录货物运输关键时间（接单，接货，运达）的日志
    private String cargoTimeLog = "";

    // 记录接手过的订单以及该订单获得的收益
    private HashMap<Cargo,Double> cargoBenefit = new HashMap<>();



    @Override
    public void afterTick(TimeLapse timeLapse) {}
    /**原始构造函数*/
    //默认的，继承vehicle类的构造方法
    Truck(Point startPosition, int capacity) {
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        init();
    }

    // 货车初始化方法
    private void init() {
        this.AutomaticModeOpen = true;
        this.setTruckLevel(MyTool.getRandom(4,10));
    }

    /** 货车逐帧（极短时间段）要做的事情
     * 1 遍历 已抢未装货列表；遍历 已装货列表
     * 2 a 如果均为空，则原地等待。
     *   b 如果只有一个订单则直接执行。
     *   c 如果有多项，展示并询问先执行哪一种操作，哪一个订单。然后执行如下操作之一：前往并完成该订单的装货/将该订单货物运达
     * 3 返回第一步，重新规划货车下一次行程
     * 4 在这些过程中，随时有新货物产生，当新货物产生，询问司机是否抢单。
     * 5 当成功抢到新订单，将该订单加入已经抢单未装货列表中，返回第一步，重新规划货车下一次行程。
     * */
    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        if (!time.hasTimeLeft()) {
            return;
        }
        //路径规划  truckDoing 为-1则需要规划路径；
        // 为1的时候 货车前往指定的已接单的出发地进行接单。
        // 为2的时候 货车前往运输至指定已装货物的目的地卸货
        //没有新订单则执行原计划
        if (haveNewOrder == false) {
            //检查当前要做的事情
            if (truckDoing == -1) {//当前没有要做的事情，则要决定接下来要做的事情。
                //只有当所有未完成订单数量大于等于2的时候才会 询问，
                if ((myToDoCargoNum + myDoingCargoNum) >= 2) {
                    //在所有可能的选项里面 等概率随机选择
//自动模式
                    if (AutomaticModeOpen) {
                        if(myToDoCargoNum == 0){                        //当没有 已接未运 订单的时候，自动选择去运输 已装货 订单
                            truckDoing = 2;
                            currentOrder = (int)(Math.random() * myDoingCargoNum); //在可选择订单中随机选择
                            //令还在公告栏中的货物不会成为运输目标
                        } else if (myDoingCargoNum == 0) {                 //当没有 已装货 订单的时候，自动前往 已接未运 订单
                            truckDoing = 1;
                            // 这里不判断是否超出 体积/重量 ；我们认为：当货车没有运货时，已经接单的货物，无论选哪一个，都能装下
                            currentOrder = (int)(Math.random() * myToDoCargoNum); //同上
                            //令还在公告栏中的转手订单不会成为运输目标
                           if (myToDoCargo[currentOrder].getCargoStatus() == 5){
                               truckDoing = -1;
                           }
                        }
                        else {

                            //同时有一件等待装货和一件等待卸货，优先选择卸货、
                            if (myToDoCargoNum == 1 && myDoingCargoNum == 1) {
                                truckDoing = 2;
                                currentOrder = (int)(Math.random() * myDoingCargoNum);
                            }
                            else if (Math.random() > 0.5) {                      //当既有 已接未运 又有 已装货 订单的时候 随机选择行动
                                truckDoing = 1;                             //前往 已接未运订单
                                currentOrder = (int)(Math.random() * myToDoCargoNum);
                                if((myToDoCargo[currentOrder].getVolume() > this.getCurrentVolume())
                                        ||(myToDoCargo[currentOrder].getWeight() > this.getCurrentWeight())
                                        || (myToDoCargo[currentOrder].getCargoStatus() == 5))
                                {        //当要去装货的订单超过当前剩余体积/重量时，或者其为正在转手订单，
                                        // 则该订单不会成为接下来要装货的目标
                                    truckDoing = -1;
                                }
                            }
                            else {
                                truckDoing = 2;
                                currentOrder = (int)(Math.random() * myDoingCargoNum);
                            }
                        }
                    }
//手动模式，通过控制台询问下一步
// 👉
                    else {
                        if (myToDoCargoNum == 0) {//当没有 已接未运 订单的时候 ，自动选择去运输 已装货 订单
                            truckDoing = 2;
                            // 手动询问接下来执行当订单
                            // 同样，这里不判断是否超出 体积/重量 ；我们认为：当货车没有运货时，已经接单当货物，无论选哪一个，都能装下
                            currentOrder = getNextOrder(truckDoing);
                        } else if (myDoingCargoNum == 0) {                 //当没有 已装货 订单的时候， 自动前往 已接未运 订单
                            truckDoing = 1;
                            currentOrder = getNextOrder(truckDoing);
                        } else {
                            //手动询问行为 和 执行订单
                            truckDoing = getNextAction();
                            currentOrder = getNextOrder(truckDoing);
                        }
                    }
                } else if ((myToDoCargoNum + myDoingCargoNum) == 1) {
                    //当只有一个订单，则自动将该订单作为接下来要完成的订单
                    System.out.println("\n只有一个订单，自动执行操作\n");
                    if (myToDoCargoNum == 1) {
                        truckDoing = 1;
                        currentOrder = 0;
                    } else {
                        truckDoing = 2;
                        currentOrder = 0;
                    }
                } else {//无任何订单直接空过 即货车原地等待
                }
            } else if (truckDoing == 1) { //要做的事情是：1前往并完成订单点；2装货；3 清空状态，等待询问下一个行动
/**已抢未运，去接货
 */
                if(rm.containsObject(myToDoCargo[currentOrder])){
                    rm.moveTo(this, myToDoCargo[currentOrder], time);
                }
                //接货
                if (rm.equalPosition(this, myToDoCargo[currentOrder])) {
                    pm.pickup(this, myToDoCargo[currentOrder], time);
                    myToDoCargo[currentOrder].setCargoStatus(3);
                    //接货时减少货车当前剩余重量/体积
                    this.setCurrentWeight(this.getCurrentWeight() - myToDoCargo[currentOrder].getWeight());
                    this.setCurrentVolume(this.getCurrentVolume() - myToDoCargo[currentOrder].getVolume());

                    //货物时间日志更改
                    myToDoCargo[currentOrder].setCargoTimeLog(myToDoCargo[currentOrder].getCargoTimeLog() + "\n货车" + this.getID() + "在" + TruckLogistics.getTickCount() + "时刻，订单" + myToDoCargo[currentOrder].getID() + "开始装货");
                    this.setCargoTimeLog(this.getCargoTimeLog() + "\n货车" + this.getID() + "在" + TruckLogistics.getTickCount() + "时刻，订单" + myToDoCargo[currentOrder].getID() + "开始装货");

                    //接货完成后，已接未运列表移除该货物项，正在运输列表添加该项，同时改变列表中的个数
                    addDoingCargo(myToDoCargo[currentOrder]);
                    deleteToDoCargo(currentOrder);

                    truckDoing = -1;//完成后初始化这2项,以便于重新决策接下来的操作
                    currentOrder = -1;
                }
            } else if (truckDoing == 2) {//要做的事情是：1将该订单运达；2卸货；3担保额恢复；4清空状态，等待下一个行动
/**已经装货，去卸货！
 */
                rm.moveTo(this, myDoingCargo[currentOrder].getDeliveryLocation(), time);
                //卸货，订单完成。
                if (rm.getPosition(this).equals(myDoingCargo[currentOrder].getDeliveryLocation())) {
                    if (pm.containerContains(this,myDoingCargo[currentOrder])) {
                        pm.deliver(this, myDoingCargo[currentOrder], time);
                    }

                    //货物时间日志更改
                    myDoingCargo[currentOrder].setCargoTimeLog(myDoingCargo[currentOrder].getCargoTimeLog() + "\n货车" + this.getID() + "在" + TruckLogistics.getTickCount() + "时刻，订单" + myDoingCargo[currentOrder].getID() + "运达，开始卸货");
                    this.setCargoTimeLog(this.getCargoTimeLog() + "\n货车" + this.getID() + "在" + TruckLogistics.getTickCount() + "时刻，订单" + myDoingCargo[currentOrder].getID() + "开始卸货");

                    //卸货要恢复货车剩余重量/体积
                    this.setCurrentWeight(this.getCurrentWeight() + myDoingCargo[currentOrder].getWeight());
                    this.setCurrentVolume(this.getCurrentVolume() + myDoingCargo[currentOrder].getVolume());
                    //进行接货完成的资金结算/设置flag，平台检查flag，当要进行资金结算，平台获取要进行结算的承运方和发货方（通过货物），
                    //通过修改他们当银行账号的金额来进行资金结算
                    endOfOrder = true;
                    cargoNeedEnd = myDoingCargo[currentOrder];
                    System.out.println("卸货订单信息为：");
                    cargoNeedEnd.cargoDescription();
                    //此时正在运输列表移除该项，同时改变列表中的个数
                    deleteDoingCargo(currentOrder);
                    //卸货完成后，初始化这2项
                    truckDoing = -1;
                    currentOrder = -1;
                    this.showTruckOrder();
                }
            } else {//其他数字的订单状态无效
                System.out.println("错误，当前订单状态无效");
            }
        } else {            //如果产生了新订单， 初始化truckDoing为-1，然后重新规划
            truckDoing = -1;
            haveNewOrder = false; //新订单的到来，已经重置来当前计划，
        }
    }

    // 展示货车信息
    public String showTruck(){
        DecimalFormat df = new DecimalFormat(".00");
        String result;
        result = getID()+":\n货车类型:" + MyTool.getTypeString(this.carryType) +
                ";\n当前状态:" +  (this.isActivated()?"已激活":"未激活" )+
                ";\n当前剩余可载重量:" + this.getCurrentWeight() +
                "; 当前剩余可载体积:" + this.getCurrentVolume() +
                ";\n货车当前订单数量:" + (myDoingCargoNum+myToDoCargoNum)+
                "；剩余担保额：" + df.format(guarantor.getAccount_DisposableMoneyMap().get(this))+
                "\n货车评级："+ this.getTruckLevel()+"当前有订单"+(myDoingCargoNum+myToDoCargoNum)+
                (this.isOpenCargoFliter()?"\n货车只抢附近的订单":"\n货车参与所有抢单");
        System.out.println(result);
        return result;
    }

    // 展示货车信息以及其 已抢未运 和 正在运输 订单信息，为空则显示出来
    public String showTruckOrder() {
        //记录：货车信息 + 该车运输货物信息
        String result;
        result = this.showTruck() + "\n";
        if (this.myToDoCargoNum == 0) {
            System.out.println("该货车还没有任何已抢未运的订单！");
            result = result + "-\n该货车还没有任何已抢未运的订单！\n";
        } else {
            System.out.println("已抢未运订单列表如下");
            result = result + "-\n已抢未运订单列表如下，共有"+this.myToDoCargoNum+"项\n";
            for (int i = 0;i < this.myToDoCargoNum;i ++) {
                System.out.print("📦编号" + i + " ");
                result = result + myToDoCargo[i].cargoDescription();
                result = result + "\n";
            }
        }
        if (this.myDoingCargoNum == 0) {
            System.out.println("该货车上没有装载任何货物！");
            result = result + "-\n该货车上没有装载任何货物！\n";
        } else {
            System.out.println("已装货订单列表如下");
            result = result + "-\n已装货订单列表如下,共有"+this.myDoingCargoNum+"项\n";
            for (int i = 0;i < this.myDoingCargoNum;i ++){
                System.out.print("📦编号" + i + " ");
                result = result + myDoingCargo[i].cargoDescription();
                result = result + "\n";
            }
        }
        System.out.println(" ");
        return result;
    }

    //询问获取接下来的行动
    public  int getNextAction() {
        System.out.println("询问接下来的行动");
        System.out.print("🚚编号"+getID());
        showTruckOrder();
        int nextAction = -1;
        System.out.println("请选择接下来的行为，1：前往已抢未运的订单，2：运输已经装货的订单");
        //遇到无效输入则重新询问
        while ((nextAction!=1 )|| (nextAction!=2)) {
            Scanner sc = new Scanner(System.in);
            nextAction =sc.nextInt();
            if ((nextAction != 1 ) || (nextAction != 2)) {
                System.out.println("输入无效，请重新输入！");
            }
        }
        return nextAction;
    }

   //与上面函数一起用，询问获取将要执行的订单
    public int getNextOrder(int truckDoing) {
        showTruckOrder();
        System.out.println("请选择要执行的订单");
        int nextOrder = -1;
        int orderNum = -1;
        if (truckDoing == 1) {
            orderNum = getMyToDoCargoNum();
        } else {
            orderNum = getMyDoingCargoNum();
        }
        //遇到无效输入则重新询问
        while ((nextOrder < 0)||(nextOrder > orderNum)) {
            Scanner sc = new Scanner(System.in);
            nextOrder =sc.nextInt();
            if ((nextOrder < 0) || (nextOrder > orderNum)) {
                System.out.println("输入无效，请重新输入！");
            }
        }
        return nextOrder;
    }

    //向货车中已抢未运订单列表添加 新的货物
    public void addToDoCargo(Cargo newOrder) {
        myToDoCargo[myToDoCargoNum] = newOrder;
        myToDoCargoNum++;
    }

    //向货车中已运输订单列表中添加 新的货物 ,修改车辆当前承接重量
    public void addDoingCargo(Cargo newOrder) {
        myDoingCargo[myDoingCargoNum] = newOrder;
        myDoingCargoNum++;
    }

    //在已抢未接订单列表 删除指定下标的货物，其后面的货物向前移动补全，该订单个数减一
    public void deleteToDoCargo(int index) {
        if ((index + 1) == myToDoCargoNum) {//如果最后一项，则只需要数量减一即可
            myToDoCargoNum--;
        } else {
            for(int i = index;i < (myToDoCargoNum - 1);i++){
                myToDoCargo[i] = myToDoCargo[i+1];
            }
            myToDoCargoNum--;
        }
    }

    public int findToDoCargoIndex(Cargo cargo){
        int ans = -1;
        for(int i = 0; i < myToDoCargoNum; i++){
            if (myToDoCargo[i] == cargo){
                ans = i;
            }
        }
        System.out.println("ans = " + ans);
        return ans;
    }

    //订单撤销时，从已经运输的订单（包括已接未运和已运输）中，删除指定货物
    public boolean deleteWithdrawCargo(Cargo cargo) {
        boolean ret = false;
        for (int i = 0;i < myToDoCargoNum;i++){
            if (cargo == myToDoCargo[i]) {
                System.out.println("\n\n\n\n找到待删除货物1\n");
                deleteToDoCargo(i);
                ret = true;
                break;
            }
        }
        if(!ret){
            for (int i = 0;i < myDoingCargoNum;i++) {
                if (cargo == myDoingCargo[i]) {
                    System.out.println("\n\n\n\n找到待删除货物2\n");
                    deleteDoingCargo(i);
                    ret = true;
                    break;
                }
            }
        }
        if (!ret) {
            System.out.println("撤销发生错误，要撤销的订单不存在");
        }
        return ret;
    }

    // 在一运输订单列表中 删除指定下标的货物，恢复车辆的重量/体积 后面的货物向前移动补全,该订单个数减一
    public void deleteDoingCargo(int index) {
        this.setCurrentVolume(this.getCurrentVolume() + myDoingCargo[index].getVolume());
        this.setCurrentWeight(this.getCurrentWeight() + myDoingCargo[index].getWeight());

        if ((index + 1) == myDoingCargoNum){//如果最后一项，则只需要数量减一即可
            myDoingCargoNum--;
        } else {
            for (int i = index; i < (myDoingCargoNum - 1); i++) {
                myDoingCargo[i] = myDoingCargo[i+1];
            }
            myDoingCargoNum--;
        }
    }

    // 用来初始化的时候生成车牌号 作为承运方ID
    public static String generateTruckID(int i) {
        String text ;
        if (i >= 10) {
            text = "" + plateToShow + i;
        }
        else {
            text = "" +plateToShow + 0 + i;
        }
        return text;
    }

    //展示或者整体状况，在注册清单上展示用
    public String showOverview(){
        return "承运方ID:"+getID()+"  注册时间"+MyTool.tickChangeToHour(getRegisterTime())+"  当前状态:" +  (this.isActivated()?"已激活":"未激活" );
    }

    // 测试用，展示所需参数的值
    public void showTest1(){
        System.out.println("haveNewOrder;trukDoing;currOrder分别为 " + haveNewOrder+truckDoing+currentOrder);
    }

    public static double getSPEED() {
        return SPEED;
    }

    public int getCarryType() {
        return carryType;
    }

    public Cargo[] getMyToDoCargo() {
        return myToDoCargo;
    }

    public int getMyToDoCargoNum() {
        return myToDoCargoNum;
    }

    public void setCarryType(int carryType) {
        this.carryType = carryType;
    }


    public void setMyToDoCargo(Cargo[] myToDoCargo) {
        this.myToDoCargo = myToDoCargo;
    }

    public void setMyToDoCargoNum(int myToDoCargoNum) {
        this.myToDoCargoNum = myToDoCargoNum;
    }

    public void setMyDoingCargo(Cargo[] myDoingCargo) {
        this.myDoingCargo = myDoingCargo;
    }

    public void setMyDoingCargoNum(int myDoingCargoNum) {
        this.myDoingCargoNum = myDoingCargoNum;
    }

    public void setHaveNewOrder(boolean haveNewOrder) {
        this.haveNewOrder = haveNewOrder;
    }

    public void setNewOrder(Cargo newOrder) {
        this.newOrder = newOrder;
    }

    public void setTruckDoing(int truckDoing) {
        this.truckDoing = truckDoing;
    }

    public void setCurrentOrder(int currentOrder) {
        this.currentOrder = currentOrder;
    }

    public Cargo[] getMyDoingCargo() {
        return myDoingCargo;
    }

    public int getMyDoingCargoNum() {
        return myDoingCargoNum;
    }

    public boolean isHaveNewOrder() {
        return haveNewOrder;
    }

    public Cargo getNewOrder() {
        return newOrder;
    }

    public int getTruckDoing() {
        return truckDoing;
    }

    public int getCurrentOrder() {
        return currentOrder;
    }

    public boolean isEndOfOrder() {
        return endOfOrder;
    }

    public void setEndOfOrder(boolean endOfOrder) {
        this.endOfOrder = endOfOrder;
    }
    public void setCargoNeedEnd(Cargo cargoNeedEnd) {
        this.cargoNeedEnd = cargoNeedEnd;
    }

    public Cargo getCargoNeedEnd() {
        return cargoNeedEnd;
    }

    public void setTruckLevel(int truckLevel) {
        this.truckLevel = truckLevel;
    }

    public int getTruckLevel() {
        return truckLevel;
    }

    public static boolean isAutomaticModeOpen() {
        return AutomaticModeOpen;
    }

    public int getCurrentVolume() {
        return currentVolume;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public static void setAutomaticModeOpen(boolean automaticModeOpen) {
        AutomaticModeOpen = automaticModeOpen;
    }

    public boolean isOpenCargoFliter() {
        return openCargoFliter;
    }

    public void setOpenCargoFliter(boolean openCargoFliter) {
        this.openCargoFliter = openCargoFliter;
    }

    public void setCurrentVolume(int currentVolume) {
        this.currentVolume = currentVolume;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getPlateToShow() {
        return plateToShow;
    }

    public void setPlateToShow(String plateToShow) {
        this.plateToShow = plateToShow;
    }

    public Guarantor getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(Guarantor guarantor) {
        this.guarantor = guarantor;
    }

    public String getCargoRecode() {
        return cargoRecode;
    }

    public void setCargoRecode(String cargoRecode) {
        this.cargoRecode = cargoRecode;
    }

    public String getID() {
        return ID;
    }

    public void setID(String id) {
        ID = id;
    }

    public boolean isSpacial() {
        return spacial;
    }

    public void setSpacial(boolean spacial) {
        this.spacial = spacial;
    }

    public boolean isEmpty(){
        if(myDoingCargoNum == 0 && myToDoCargoNum == 0) return true;
        else return false;
    }

    public boolean isShowInGraph() {
        return showInGraph;
    }

    public void setShowInGraph(boolean showInGraph) {
        this.showInGraph = showInGraph;
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

    public String getSpacialCargoEndLog() {
        return spacialCargoEndLog;
    }

    public void setSpacialCargoEndLog(String spacialCargoEndLog) {
        this.spacialCargoEndLog = spacialCargoEndLog;
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

    public String getGuaranteeLog() {
        return guaranteeLog;
    }

    public void setGuaranteeLog(String guaranteeLog) {
        this.guaranteeLog = guaranteeLog;
    }

    public String getCargoTimeLog() {
        return cargoTimeLog;
    }

    public void setCargoTimeLog(String cargoTimeLog) {
        this.cargoTimeLog = cargoTimeLog;
    }
}



