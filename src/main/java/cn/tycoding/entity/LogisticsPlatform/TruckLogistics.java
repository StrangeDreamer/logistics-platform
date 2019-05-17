package cn.tycoding.entity.LogisticsPlatform;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Float.POSITIVE_INFINITY;

/**
 * 第一次使用时，在配置 -> VM optiong 选项框内填入 -XstartOnFirstThread
 * 撤单的时候，在查询订单界面请不要选中要进行撤单的货物，
 * 撤单的货物满足条件要立刻点击确定（或者暂停演示），否则本来满足条件的货物因为期间完成而出错
 */
public final class TruckLogistics {

    // 自动模式的开关：发货方自动产生货物、货车自动出价、自动确认收货，该开关随界面而改动
    private static boolean automaticModeOpen = true;
    // 平台方
    private static LogisticsPlatform lp = new LogisticsPlatform(new Point(3246025.6513103773,2.5680426171636064E7));
    // 担保方
    private static Guarantor guarantor = new Guarantor();
    // 银行,银行的第一个用户：平台方
    private static Bank bank = new Bank();
    // 律师
    private static Lawyer lawyer = new Lawyer();
    // 货车选择开启过滤的概率
    private static double chooseFilterProb = 0.5;
    // 货车参与抢单概率
    private static double automaticBidRatio = 0.5;
    // 非普通货物出现概率
    private static double spacialCargoProb = 0.6;
    // 发货方重新提交订单概率
    private static double automaticResubmitRatio = 0.8;
    // 新货物出现概率  每个时间间隔进行一次判定
    private static double newCargoProb = .025;
    // 撤单概率
    private static double withdrawalProbability = 0.000;
    // 订单转手概率
    private static double orderChangeProbability = 0.000;
    // 接受的订单的总数
    private static int receivedOrderNum = 0;
    // 有效订单的数量
    private static int validOrderNum = 0;
    // 初始货车数量
    private static int NUM_TRUCKS = 0 ;
    // 初始发货方数量
    private static int NUM_SHIPPER = 3;
    // 初始收货方数量
    private static int NUM_RECEIVER = 3;

    // 设置一个发货方资金基本值，所有货车的保证金将在这个数值上下50%浮动
    private static double SHIPPER_MONEY = 50000;
    // 设置一个货物的基本值，所有订单的价格在其 20%~500% 浮动
    private static double CARGO_MONEY = 2000;
    // 设置一个货车担保额度的基本值，所有货车在其 50%~200% 浮动
    private static double TRUCK_GURARANTEE = 20000;

    // 平台利润分享比例,该比例的利润空间金额进行分享
    // 发货方
    private static double PROFIT_SHARE_RATIO1 = 0.33;
    // 承运方
    private static double PROFIT_SHARE_RATIO2 = 0.33;
    // 平台方
    private static double PROFIT_SHARE_RATIO3 = 0.33;

    // 抢单报价下限, 当报价低于这个，就出价无效
    private static double MIN_ORDERPRICE = 0.3;
    // 红包上限比例，红包最高只能达到该比例与原运费的乘积
    private static double BONUS_MAX_RATIO = 0.05;
    // 设置展位费 默认展位费用为1
    private static double EXHIBITION_FEE = 1;

    // 发货方重新提交订单增加运费的额度
    private static double AutomaticResubmitIncrease = 0.1;
    // 已接未运货物撤单收费百分比
    private static double WITHDRAWALFEERATIO = 0.1;

    // 装货和卸货 所需花费时间
    private static long SERVICE_DURATION = 40000;
    //公告牌上发布可以停留的时间
    private static int PUBLISHING_LIFE = 100;
    // 这3项属性没有用处？ 并不能改变速度
    private static long TEST_STOP_TIME = 20 * 60 * 1000;
    private static int TEST_SPEED_UP = 64;
    private static int SPEED_UP = 4;

    // 地图文件路径
    private static final String MAP_FILE = "/data/maps/leuven-simple.dot";
    private static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE =
            newHashMap();

    // 记录是否进行了初始化
    private static boolean haveInit = false;
    // 当前界面
    private static JFrameSetup jFrameSetup = null;
    private static JFrameUseCaseTest jFrameUseCaseTest = null;
    private static JFrameChooseActor jFrameChooseActor = null;

    // 在此保存指定评级货车的id 和其 评级
    private static String specialTruckID;
    private static int specialTruckRanking;

    // 保留小数的位数
    private static DecimalFormat df = new DecimalFormat(".00");
    // 计算tick的次数，用来计时
    private static int tickCount = 0;


    public static Simulator run(final long endTime, String graphFile, Display display) {
        System.out.println("run");
        final View.Builder view = createGui(display);
        // 设置地图
        final Simulator simulator = Simulator.builder()
                .addModel(RoadModelBuilders.staticGraph(loadGraph(graphFile)))
                .addModel(DefaultPDPModel.builder())
                .addModel(view)
                .build();
        final RandomGenerator rng = simulator.getRandomGenerator();
        final RoadModel roadModel = simulator.getModelProvider().getModel(
                RoadModel.class);
        final PDPModel pdpModel = simulator.getModelProvider().getModel(PDPModel.class);
        //参数设置界面
        jFrameSetup  = new JFrameSetup(lp);

        simulator.addTickListener(new TickListener() {
            //每个tick实际上最多产生一个货物，所以每个tick最多接单一个货物
            @Override
            public void tick(TimeLapse time) {

                // 为所有参与方计时
                tickIncrease(lp);
                // 未设置参数则要先设置参数，
                if (!jFrameSetup.isSetUpImplemented()) {
                    System.out.println("参数未设置，请设置参数开始模拟器");
                    return;
                }

                // 未初始化则要先初始化
                if (!haveInit) {
                    System.out.println("车辆、发货方、收货方等正在注册/初始化");
                    // 初始化各项参数、货车、发货方、接货方、界面等
                    init(roadModel, rng, simulator);
                    haveInit = true;
                }

                // log的刷新
                jFrameChooseActor.getjFrameBankLog().refresh();
                jFrameChooseActor.getjFrameBillingLog().refresh();
                jFrameChooseActor.getjFrameGuarantorLog().refresh();

                // 订单提交：概率产生新的货物并提交到平台的正在发布的订单列表中，手动模式不自动产生订单
                if (rng.nextDouble() < newCargoProb && automaticModeOpen) {
                    orderSubmit(roadModel,rng,simulator);
                }

                // 抢单，订单发布公告牌不为空时执行
                if (lp.getCargoPublishing().size() > 0 ) {
                    grabOrder(roadModel);
                }
                // 处理手动出价
                manualBidding();

                // 接单，订单的公告牌不为空时执行
                if (lp.getCargoPublishing().size() > 0) {
                    receiveOrder(simulator);
                }

                // 订单完成，轮询所有订单，如果有订单需要结束，则进行订单结算等操作。手动确认出价包含在内
                endOrder();

                // 令没有运货的货车不可见
                setTruckInvisible(simulator,pdpModel);

                // 用例测试
                useCaseTest(simulator,roadModel,rng);

                // 读取并变更自动模式
                automaticModeOpen = !jFrameChooseActor.getHandModel().isSelected();
            }
            @Override
            public void afterTick(TimeLapse timeLapse) {}
        });
        simulator.start();
        return simulator;
    }

    // 计时
    private static void tickIncrease(LogisticsPlatform lp){
        tickCount++;
        for (int i = 0; i < lp.getTrucksInPlatform().size(); i++) {
            lp.getTrucksInPlatform().get(i).setMyTick(lp.getTrucksInPlatform().get(i).getMyTick()+1);
        }
        for (int i = 0; i < lp.getCargosInPlatform().size(); i++) {
            // 只有订单被接单未运，或者未运达才会计算其时间
            if (lp.getCargosInPlatform().get(i).getCargoStatus() ==2 || lp.getCargosInPlatform().get(i).getCargoStatus() ==3 ) {
                lp.getCargosInPlatform().get(i).setMyTick(lp.getCargosInPlatform().get(i).getMyTick()+1);
            }
        }
        for (int i = 0; i < lp.getShippersInPlatform().size(); i++) {
            lp.getShippersInPlatform().get(i).setMyTick(lp.getShippersInPlatform().get(i).getMyTick()+1);
        }
        for( int i = 0; i < lp.getReceiversInPlatform().size(); i++ ){
            lp.getReceiversInPlatform().get(i).setMyTick(lp.getReceiversInPlatform().get(i).getMyTick()+1);
        }
    }

    // 初始化模拟器
    private static void init(RoadModel roadModel, RandomGenerator rng, Simulator simulator) {
        // 通过设置界面，获取所设置的参数
        JFrameSetup j = jFrameSetup;
        transferParameters(j);

        // 平台注册，给与一个初始银行账号资金
        bank.register(lp,100000);

        bank.setMoneyLog("日志");
        //添加承运方
        /**初始化货车各项信息 包括：可运重量，体积，担保额度，货车类型，是否过滤； 以及货车 在银行注册、在平台注册* */
        for (int i = 0; i < NUM_TRUCKS; i++) {
            //capacity被体积和重量替换
            Truck tempTruck = new Truck(roadModel.getRandomPosition(rng),0);
            tempTruck.setCurrentVolume(MyTool.getRandom(300,400));
            tempTruck.setCurrentWeight(MyTool.getRandom(300,400));
            // 担保额假设随机基准的50%~200%
            double tempGuaranteeAmount = MyTool.getRandom(TRUCK_GURARANTEE * 0.5,TRUCK_GURARANTEE * 2);
            // 货车可运送类型的设置 70% 正常 15%冷链 15%危险品
            double rand2 = Math.random();
            if (rand2 > 0.3) {
                tempTruck.setCarryType(1);
            } else if (rand2 > 0.15) {
                tempTruck.setCarryType(2);
            } else {
                tempTruck.setCarryType(3);
            }
            //固定第一辆为冷链车，第二辆为危险品车
            if (i == 0) {
                tempTruck.setCarryType(2);
            }
            if (i == 1) {
                tempTruck.setCarryType(3);
            }
            // 固定第一辆车过滤，第二辆车不过滤，后面车随机过滤
            if (i == 0) {
                tempTruck.setOpenCargoFliter(true);
            }
            if (i == 1) {
                tempTruck.setOpenCargoFliter(false);
            }
            if (i > 1) {
                if (Math.random() < chooseFilterProb) {
                    tempTruck.setOpenCargoFliter(true);
                }
            }

            //给与指定车辆 指定评级
            if (tempTruck.getID() == specialTruckID) {
                tempTruck.setTruckLevel(specialTruckRanking);
            }
            System.out.print("🚚编号" + i + " ");
            tempTruck.setID(Truck.generateTruckID(i));
            // 给货车在银行注册账号
            // 承运方在担保方注册
            guarantor.register(tempTruck, tempGuaranteeAmount);
            tempTruck.setGuaranteeLog("注册额度：" + df.format(tempGuaranteeAmount));
            tempTruck.setGuarantor(guarantor);

            bank.register(tempTruck, 100000);
            lp.getTrucksInPlatform().add(tempTruck);
            lp.getTrucksInPlatform().get(i).getID();
            lp.getTrucksInPlatform().get(i).showTruck();
            //激活承运方
            tempTruck.setActivated(true);
            simulator.register(tempTruck);
            tempTruck.setRegisterTime(tickCount);
        }

        // 添加发货方，评级随机
        for (int i = 0; i < NUM_SHIPPER; i++) {
            int tempLevel = MyTool.getRandom(3,10);
            Shipper shipper = new Shipper(tempLevel,i+ "");
            bank.register(shipper,SHIPPER_MONEY);
            lp.getShippersInPlatform().add(shipper);
            System.out.println("\n\n生成发货方编号"+lp.findShipperByID(i+"").getID());
            //激活发货方
            shipper.setActivated(true);
            shipper.setRegisterTime(tickCount);
        }

        // 添加收货方
        for (int i = 0; i < NUM_RECEIVER; i++) {
            Receiver receiver = new Receiver(i + "");
            //bank.registerBank(lp.getReceiversInPlatform().get(i),1000);         可以令收货方注册银行账号
            lp.getReceiversInPlatform().add(receiver);
            //激活收货方
            receiver.setActivated(true);
            receiver.setRegisterTime(tickCount);
        }

        // 产生界面
        jFrameChooseActor = new JFrameChooseActor(lp,bank,guarantor);

        // 将车辆信息同步到查询界面
        for( int i = 0;i < NUM_TRUCKS;i++) {
            //同步到查询界面
            jFrameChooseActor.getjFrameInquiry().addCmd2(i);
            jFrameChooseActor.getjFrameInquiry().addCmd4(i);
        }
        jFrameUseCaseTest = new JFrameUseCaseTest(lp
                ,roadModel, rng, bank,guarantor
        );
        // 将车辆信息同步到查询界面
        for (int i = 0;i < NUM_TRUCKS;i++) {
            //同步到查询界面
        }
    }

    //订单发布
    private static void orderSubmit(RoadModel roadModel,RandomGenerator rng,Simulator simulator) {
        if (lp.getTrucksInPlatform().size() <= 0 || lp.getShippersInPlatform().size() <= 0 || lp.getReceiversInPlatform().size() <= 0) {
            return;
        }
        Cargo cargo  = randCargo(roadModel,rng);
        Shipper shipper = cargo.getShipper();

        // 如果发货方的余额不足以支付订单费用，则该订单无法发布。该发货方必须要激活
        if (orderReleaseSuccess(cargo,shipper,simulator) && shipper.isActivated()) {
            System.out.println("\n新订单发布成功!"+cargo.getID());
            lp.getCargoPublishing().add(cargo);
            if (cargo.getID().equals("-")) {
                ++receivedOrderNum;
                cargo.setID(receivedOrderNum + "");
            }
            jFrameChooseActor.getjFramePublishing().refresh();
            jFrameChooseActor.getjFramePublishing().setResult("新订单发布！订单ID为" + cargo.getID());
            lp.setBillingLog(lp.getBillingLog() + "\n新订单发布" + cargo.getID() + "报价上限为" + df.format(cargo.getFreightFare()));
        }
    }

    // 抢单：轮询所有正在发布的订单，检查有没有这些订单有无新的出价
    // 轮询所有正在发布的订单，检查并记录新的出价，然后公告牌寿命减一
    private static void grabOrder(RoadModel roadModel) {
        for (int i = 0; i < lp.getCargoPublishing().size(); i++) {
            Cargo cargo = lp.getCargoPublishing().get(i);
            // 对每个货车逐个询问出价,最终获取所有车辆到出价结果，保存到cargo的tempbid中,已经激活的车辆才能抢单
            // 自动模式则自动出价，否则跳过自动出价
            if(automaticModeOpen) {
                pollingBidding(cargo,lp,roadModel);
            }
            // 公告牌寿命减少
            cargo.setPublishingLife(cargo.getPublishingLife() - 1);
        }
    }

    // 手动出价 读取手动出价结果（参数包括：货物，出价承运方，出价数值。调用该方法时，则认为这些参数都是合法的）
    private static void manualBidding(){
        Boolean haveNewBidding = jFrameChooseActor.getjFramePublishing().isHaveNewBidding();
        if (haveNewBidding) {
            Cargo cargo = jFrameChooseActor.getjFramePublishing().getCargo();
            Truck truck = jFrameChooseActor.getjFramePublishing().getTruck();
            int bidding = jFrameChooseActor.getjFramePublishing().getBiddingPrice();
            // 只有有资格才能进行参与出价
            if (lp.typeCanBePick(truck.getCarryType(),cargo.getNeededCarryType())//运输类型需要符合要求
                    //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
                    && haveEnoughCapacity(cargo,truck)
                    //担保额要够
                    && guarantor.enoughAmountWith(truck,cargo.getLiquidatedDamages())
                    //不能给自己转手的订单出价
                    && (cargo.getMyTruck() != truck)
                    // 出价区间合理
                    && (bidding <= cargo.getFreightFare()) && (bidding >= cargo.getFreightFare() * MIN_ORDERPRICE)
            ){

                cargo.getTempbid().put(truck,bidding);
                cargo.setHaveBid(true);
                lp.setBillingLog(lp.getBillingLog() + "\n" + truck.getID() + "对订单" + cargo.getID() + "出价" + bidding);
                guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于" + truck.getID() + "对订单" + cargo.getID() + "抢单出价");
                guarantor.freezeMoney(truck,cargo.getLiquidatedDamages());
                truck.setGuaranteeLog(truck.getGuaranteeLog() + "\n由于" + truck.getID() + "对订单" + cargo.getID() + "抢单出价"
                        + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(truck)));
            }
            else {
                lp.setBillingLog(lp.getBillingLog() + "\n" + truck.getID() + "本次对订单" + cargo.getID() + "出价无效！");
            }
            jFrameChooseActor.getjFramePublishing().setHaveNewBidding(false);
        }
    }


    // 轮询获取报价
    private static void pollingBidding(Cargo tempCargo,LogisticsPlatform lp,RoadModel roadModel) {
        Cargo cargo = tempCargo;
        //对注册的车辆轮流询问报价
        for (int i = 0; i < lp.getTrucksInPlatform().size(); i++) {
            Truck truck = lp.getTrucksInPlatform().get(i);
            //已经激活的车辆才能抢单
            if (!truck.isActivated()) {
                continue;
            }
//            // 如果该货车打开辆过滤，并且新订单距离自己比较远，会自动放弃出价
//            if (truck.isOpenCargoFliter()
//                    && MyTool.getCoordinatesPointDistance(roadModel.getPosition(truck), roadModel.getPosition(cargo)) > 5) {
//                continue;
//            }
            //如果已经出价，则直接跳过
            if (cargo.getTempbid().containsKey(truck)){
                continue;
            }

            // 自动出价，要考虑符合 重量、体积、运输类型、保障金的要求
            if (automaticModeOpen) {
                System.out.print("🚚编号"+i);
                // 只有有资格才能进行参与出价
                if (lp.typeCanBePick(truck.getCarryType(),cargo.getNeededCarryType())//运输类型需要符合要求
                        //车辆剩余足够的体积和重量，这样可以保证车辆当前是装得下该货物的
                        && haveEnoughCapacity(cargo,truck)
                        && guarantor.enoughAmountWith(truck,cargo.getLiquidatedDamages())//担保额要够
                        && (cargo.getMyTruck() != truck))//不能给自己转手的订单出价
                {
                    // 给定概率自动出价
                    if (Math.random() < automaticBidRatio) {
                        int price = (int)(tempCargo.getFreightFare() * MIN_ORDERPRICE
                                + (tempCargo.getFreightFare() * (1 - MIN_ORDERPRICE)) * Math.random());
                        cargo.getTempbid().put(truck,price);
                        cargo.setHaveBid(true);
                        lp.setBillingLog(lp.getBillingLog() + "\n" + truck.getID() + "对订单" + cargo.getID() + "出价" + price);
                        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于" + truck.getID() + "对货物" + cargo.getID() + "抢单出价");
                        guarantor.freezeMoney(truck,cargo.getLiquidatedDamages());
                        truck.setGuaranteeLog(truck.getGuaranteeLog() + "\n由于" + truck.getID() + "对订单" + cargo.getID() + "抢单出价"
                                + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(truck)));
                    } else {
                        System.out.println("自动放弃出价");
                    }
                }
                else {
                    System.out.println("不具备出价资格");
                }
            }
        }
    }

    // 接单:轮询所有正在发布的订单，如果有订单发公告牌发布寿命为0，则执行接单或者取消订单，然后从公告牌删除
    private static void receiveOrder(Simulator simulator) {
        // 轮询正在发布的订单剩余时间有没有为0的订单
        for (int i = 0; i < lp.getCargoPublishing().size(); i++) {
            Cargo cargo = lp.getCargoPublishing().get(i);
            if (cargo.getPublishingLife() < 0) {
                //对于有承运方接单的订单，选择出价最低的承运方，令其接单
                if (cargo.isHaveBid()) {
                    // 寻找最低出价方
                    int bestBidding = (int)POSITIVE_INFINITY;
                    Truck myTruck = lp.getTrucksInPlatform().get(0);
                    for (int j = 0; j < lp.getTrucksInPlatform().size(); j++) {
                        if (!cargo.getTempbid().containsKey(lp.getTrucksInPlatform().get(j))) {
                            continue;
                        }
                        if (cargo.getTempbid().get(lp.getTrucksInPlatform().get(j)) < bestBidding) {
                            myTruck = lp.getTrucksInPlatform().get(j);
                            bestBidding = cargo.getTempbid().get(lp.getTrucksInPlatform().get(j));
                        }
                    }
                    // 其他没有接到订单的出价货车的担保额度恢复
                    for (int j = 0; j < lp.getTrucksInPlatform().size(); j++) {
                        if(lp.getTrucksInPlatform().get(j) != myTruck) {
                            if(cargo.getTempbid().containsKey(lp.getTrucksInPlatform().get(j))){
                                guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于" + lp.getTrucksInPlatform().get(j).getID() + "没有抢到货物" + cargo.getID());
                                guarantor.freezeMoneyRecovery(lp.getTrucksInPlatform().get(j),cargo.getLiquidatedDamages());
                                lp.getTrucksInPlatform().get(j).setGuaranteeLog(lp.getTrucksInPlatform().get(j).getGuaranteeLog() + "\n由于" + lp.getTrucksInPlatform().get(j).getID() + "没有抢到订单" + cargo.getID()
                                        + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(lp.getTrucksInPlatform().get(j))));
                            }
                        }
                    }

                    //货物时间日志更改
                    cargo.setCargoTimeLog(cargo.getCargoTimeLog() + "\n货车" + myTruck.getID() + "在" + TruckLogistics.getTickCount() + "时刻，抢到订单" + cargo.getID() );
                    myTruck.setCargoTimeLog(myTruck.getCargoTimeLog() + "\n货车" + myTruck.getID() + "在" + TruckLogistics.getTickCount() + "时刻，抢到订单" + cargo.getID());

                    // 判断是否是自己接单(自己接单必然是转手订单)，不允许自己接自己转手的订单
                    if (myTruck == cargo.getMyTruck()) {
                        jFrameChooseActor.getjFramePublishing().setResult(cargo.getID()+"号订单时间段内无人接单，转手失败");
                        return;
                    }
                    // 判断该订单是否是转手订单
                    // 转手订单
                    else if (cargo.getCargoStatus() == 5) {
                        // 原订单从原车辆删除
                        cargo.getMyTruck().deleteToDoCargo(cargo.getMyTruck().findToDoCargoIndex(cargo));
                        jFrameChooseActor.getjFrameInquiry().deleteCargo(cargo.getID());
                        // 订单转手成功，则原来承运方的担保额恢复
                        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于订单转手");
                        guarantor.freezeMoneyRecovery(cargo.getMyTruck(),cargo.getLiquidatedDamages());
                        cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于" + cargo.getMyTruck().getID() + "转手成功"
                                + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));

                        // 货车truck 以 x 的价格接单cargo
                        truckGetOrderWithBidding(simulator,myTruck,cargo,bestBidding);
                        // 价格和车辆压入栈
                        cargo.getOfferPriceStack().push(cargo.getFreightFare());
                        cargo.getBidPriceStack().push((double)cargo.getOrderPrice());
                        cargo.getTruckStack().push(cargo.getMyTruck());
                        lp.setBillingLog(lp.getBillingLog() + "\n订单" + cargo.getID() + "转手成功，货车" + myTruck.getID() +"以" + bestBidding + "的价格获取该订单");
                    }
                    // 不是转手订单，是第一手订单
                    else {


                        cargo.getShipper().getCargoBenefit().put(cargo,0.0);
                        // 货车truck 以 x 的价格接单cargo
                        truckGetOrderWithBidding(simulator,myTruck,cargo,bestBidding);
                        // 更新发货方和收货方的当前订单信息
                        cargo.getReceiver().getCargos().add(cargo);
                        cargo.getReceiver().setCurrentCargoNum(cargo.getReceiver().getCurrentCargoNum() + 1);
                        cargo.getShipper().setCurrentCargoNum(cargo.getShipper().getCurrentCargoNum() + 1);
                        // 将发货方在该订单上花费的展位费用归还
                        bank.transfer(lp,cargo.getShipper(), EXHIBITION_FEE);
                        // 价格和车辆压入栈
                        cargo.getOfferPriceStack().push(cargo.getFreightFare());
                        cargo.getBidPriceStack().push((double)cargo.getOrderPrice());
                        cargo.getTruckStack().push(cargo.getMyTruck());

                        lp.setBillingLog(lp.getBillingLog() + "\n订单" + cargo.getID() + "接单成功，货车" + myTruck.getID() +"以" + bestBidding + "的价格接单");
                    }
                    // 给新接单车辆加入该订单订单收益记录
                    myTruck.getCargoBenefit().put(cargo,0.0);
                    // 展示
                    String mybiding = "";
                    for(int k = 0; k < cargo.getTempbid().size(); k++){
                        mybiding = mybiding +""+ cargo.getTempbid().get(i) +" \n";
                    }
                }
                // 对于没人接单的订单，直接取消，展位费不返回
                else {
                    // 如果是转手订单，则订单由原来车辆继续执行
                    if( cargo.getCargoStatus() == 5) {
                        // 订单价格的恢复
                        cargo.setFreightFare(cargo.getOfferPriceStack().peek());
                        jFrameChooseActor.getjFramePublishing().setResult(cargo.getID()+"号订单时间段内无人接单，转手失败");
                        lp.setBillingLog(lp.getBillingLog() + "\n订单" + cargo.getID() + "转手失败");
                        cargo.setCargoStatus(2);
                    }
                    // 非转手订单则直接撤单
                     else {
                         bank.setMoneyLog(bank.getMoneyLog() + "\n由于货物无人接单自动撤单");
                         bank.freezeMoneyRecovery(cargo.getShipper(),cargo.getOriginFreightFare());
                         simulator.unregister(cargo);
                         jFrameChooseActor.getjFramePublishing().setResult(cargo.getID()+"号订单时间段内无人接单，自动取消");
                         lp.setBillingLog(lp.getBillingLog() + "\n订单" + cargo.getID() + "无人接单，自动取消");
                    }
                }
                // 无论是否有接单，最后均从正在发布订单列表中删除
                lp.getCargoPublishing().remove(cargo);
            }
        }
        jFrameChooseActor.getjFramePublishing().refresh();
    }

    // 货车truck 以 x 的价格接单cargo
    private static void truckGetOrderWithBidding(Simulator simulator,Truck myTruck,Cargo cargo,int bestBidding) {
        //让接单的车辆在地图中可见
        if (!myTruck.isShowInGraph()) {
            myTruck.setShowInGraph(true);
            simulator.register(myTruck);
        }
        cargo.setMyTruck(myTruck);
        cargo.setOrderPrice(bestBidding);
        myTruck.addToDoCargo(cargo);
        // 告知货车接到新订单
        myTruck.setHaveNewOrder(true);
        // 给有效的订单编号
        validOrderNum++;
        // 向平台中添加该货物
        lp.getCargosInPlatform().add(cargo);
        cargo.setCargoStatus(2);
        // 同步到查询界面
        jFrameChooseActor.getjFrameInquiry().addCmd3(cargo.getID());
        jFrameChooseActor.getjFramePublishing().setResult(cargo.getID()+"号订单被接单，承运方为" + myTruck.getID());
    }

    // 订单结束：正常结束/订单超时/异常订单
    private static void endOrder() {
        //找出并保存已经运输到目的地的订单
        for (int i = 0;i < lp.getTrucksInPlatform().size();i++) {
            if (lp.getTrucksInPlatform().get(i).isEndOfOrder()) {
                Truck truckNeedSettle = lp.getTrucksInPlatform().get(i);
                Cargo cargoNeedSettle = lp.getTrucksInPlatform().get(i).getCargoNeedEnd();
                // 添加进待结算列表
                lp.getCargosNeedSettleInPlatform().add(cargoNeedSettle);
                // 状态更改为 运输完成但未结算
                cargoNeedSettle.setCargoStatus(4);
                // 订单运输结束，初始化flag和需要结算的订单货物
                truckNeedSettle.setEndOfOrder(false);
                truckNeedSettle.setCargoNeedEnd(null);
            }
        }

        // 对等待结算的订单进行结算
        for (int i = 0; i < lp.getCargosNeedSettleInPlatform().size(); i++) {
            Cargo cargoNeedSettle = lp.getCargosNeedSettleInPlatform().get(i);
            // 订单结算, 需要：发货方，收货方，承运方，平台方，银行，担保方,货物的参与
            freightSettlement(cargoNeedSettle);
        }
    }

    // 判断订单是否允许发布(仅处理第一手订单)，是则扣除相应费用
    private static boolean orderReleaseSuccess(Cargo tempCargo, Shipper shipper, Simulator simulator) {
        boolean success = false;
        // 当发货方剩余资金不足以支付运费以及展位费 直接结束
        if ((bank.getAccount_DisposableMoneyMap().get(shipper) - tempCargo.getOriginFreightFare() - EXHIBITION_FEE ) < 0) {
            System.out.println("该发货方资金不足，无法继续发货，订单自动取消\n");
            simulator.unregister(tempCargo);
            success = false;
        }
        // 当发货方费用足够时才继续
        else {
            // 冻结发货方运费
            bank.setMoneyLog(bank.getMoneyLog() + "\n由于发布了新订单");
            bank.freezeMoney(shipper,tempCargo.getFreightFare());
            // 发货方支付展位费
            bank.transfer(shipper,lp,EXHIBITION_FEE);

            simulator.register(tempCargo);
            success = true;
        }
        return success;
    }

    // 订单结算的方法,需要：发货方，收货方，承运方，平台方，银行，担保方，结算货物的参与
    private static void freightSettlement(Cargo cargo) {
        // 自动模式默认 收货方验货通过
        if (automaticModeOpen) {
            cargo.setAlreadyCheck(true);
            cargo.setReceiverConfirm(true);
        }

        //如果没人验货，则跳出，等待有人验货才进行结算
        if (!cargo.isAlreadyCheck()) {
            return;
        }

        // 已经验货，则判断是否验货通过
        if (cargo.isReceiverConfirm()) {
            //验货通过
            regularSettlement(cargo);
            cargo.setCargoStatus(7);
        } else {
            // 异常订单处理
            exceptionSettlement(cargo);
            cargo.setCargoStatus(6);
        }

        // 最后把结算完的订单移除
        lp.getCargosNeedSettleInPlatform().remove(cargo);
    }

    // 订单验货不通过的异常结算，挂起：向所有的货物的承运方、平台（加入异常列表）、向发货方、收货方通知该订单失效
    private static void exceptionSettlement(Cargo cargo) {
        jFrameChooseActor.getjFrameAbnormalOrder().refresh();
        lp.setAbnormalOrderLog(lp.getAbnormalOrderLog() + "\n订单" +cargo.getID() + "验货未通过\n出现异议走法律程序交给律师解决");
        cargo.setRecodeSettlement("验货未通过\n出现异议走法律程序交给律师解决");
        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于出现订单异常，" + cargo.getMyTruck().getID() + "的担保额不再退还");
        cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于出现订单异常，担保额不再退还"
                + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));

        bank.setMoneyLog(bank.getMoneyLog() + "\n由于出现订单异常，" + cargo.getID() + "的资金结算挂起");
        jFrameChooseActor.getjFrameAbnormalOrder().refresh();
    }

    // 订单正常结算 包括订单超时和订单准时到达
    // 关于担保额度的变化，这里认为转手成功后，原车辆的风险就转移到了下一辆车。所以转手操作中，转手车辆担保额恢复，接受转手的货车担保额度冻结
    private static void regularSettlement(Cargo cargo) {
        // 先判断订单是否超时
        if (cargo.getRemainHour() < 0) {
           overTimeSettlement(cargo);
        } else {
            // 准时到达订单的结算
            onTimeSettlement(cargo);
        }
        //订单状态改为已经完成
        cargo.setCargoStatus(4);
    }

    // 订单超时结算
    private static void overTimeSettlement(Cargo cargo) {
        Truck truck = cargo.getMyTruck();

        // 担保额处罚：订单超时的结算额外处罚结算,扣除相应额度上限

        //冻结资金不再冻结，直接扣除上限，可用
        guarantor.getAccount_FrozenMoneyMap().put(truck,guarantor.getAccount_FrozenMoneyMap().get(truck) - cargo.getLiquidatedDamages());
        guarantor.getAccount_MoneyMap().put(truck,guarantor.getAccount_MoneyMap().get(truck) - cargo.getLiquidatedDamages());
        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n\n" + truck.getID() + "由于没有按时完成订单，扣除其担保额上限"
                + "\n扣除后的担保额度上限为" + df.format(guarantor.getAccount_MoneyMap().get(truck)));
        cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于没有按时完成订单，扣除其担保额上限"
                + "\n扣除后的担保额度上限为" + df.format(guarantor.getAccount_MoneyMap().get(truck)));

        // 超时罚款：对最后的车辆进行超时处罚，之后调用订单准时到达的结算
        cargo.setOverTime(true);
        // 超时时长
        int t = 0 - cargo.getRemainHour();
        double m = (t * cargo.getLiquidatedDamages() * 0.05);

        cargo.setMyEnd(cargo.getID()+"号订单超时\n运输要求"+cargo.getLimitedHour()+"\n实际用时"+cargo.getUsedHour()
                +"个单位时间\n超出时长"+t+"个单位时间\n最后运输订单车辆需要支付赔偿"+ df.format(m)+ "");
        // 超时赔偿 承运方支付给平台，平台支付给发货方
        bank.setMoneyLog(bank.getMoneyLog() + "\n由于超时赔偿");
        bank.transfer(cargo.getMyTruck(),lp,m);
        bank.transfer(lp,cargo.getShipper(),m);
        // 承运方与发货方的单独资金流通记录
        cargo.getShipper().setMoneyLog(cargo.getShipper().getMoneyLog() + "\n由于订单" + cargo.getID() + "超时，获得赔偿" + df.format(m));
        truck.setMoneyLog(truck.getMoneyLog() + "\n由于订单" + cargo.getID() + "超时，支付赔偿" + df.format(m));

        cargo.getShipper().getCargoBenefit().put(cargo,cargo.getShipper().getCargoBenefit().get(cargo) + m);
        truck.getCargoBenefit().put(cargo,truck.getCargoBenefit().get(cargo) - m);

        //资金结算
        moneySettlement(cargo);

        // 结算保存到该订单的历史记录
        truck.setCargoRecode(truck.getCargoRecode() + "\n订单" + cargo.getID() +"超时完成");
        cargo.setRecodeSettlement("\n订单超时完成");
    }

    // 订单准时完成的结算
    private static void onTimeSettlement(Cargo cargo) {

        // 完成订单的车辆的担保额度恢复
        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n承运方" + cargo.getMyTruck().getID() + "由于按时完成订单");
        guarantor.freezeMoneyRecovery(cargo.getMyTruck(),cargo.getLiquidatedDamages());
        cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于按时完成订单"
                + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));

        cargo.setMyEnd(cargo.getID()+"号订单已经按时完成！\n运输要求"+cargo.getLimitedHour()+"个单位时间\n实际用时"+cargo.getUsedHour()+"个单位时间");

        //资金结算
        moneySettlement(cargo);

        // 结算保存到该订单的历史记录
        cargo.getMyTruck().setCargoRecode(cargo.getMyTruck().getCargoRecode() + "\n订单" + cargo.getID()+ "准时完成");
        cargo.setRecodeSettlement("\n订单准时完成");
    }


    // 资金结算（不包含赔偿）；无论是否超时均调用该方法
    private static void moneySettlement(Cargo cargo){
        // 平台结算该订单之前的资金
        double lpBeforeMoney = bank.getAccount_MoneyMap().get(lp);
        Shipper shipper = cargo.getShipper();
        Truck tempTruck = cargo.getMyTruck();
        cargo.getTruckStack().pop();
        // 循环出栈，直到该订单接手车辆遍历到最初接单车辆，用来处理有订单转手的结算
        while (cargo.getTruckStack().size() > 0) {
            Truck t1 = cargo.getTruckStack().pop();
            Truck t2 = tempTruck;

            double A = cargo.getOfferPriceStack().pop();
            double B = cargo.getBidPriceStack().pop();
            double bonus1 = lp.bonusOfProfitSharing(A,B,PROFIT_SHARE_RATIO1,BONUS_MAX_RATIO);
            double bonus2 = lp.bonusOfProfitSharing(A,B,PROFIT_SHARE_RATIO2,BONUS_MAX_RATIO);

            double realBonusT1 = bonus1 * t1.getTruckLevel()/10;
            double realBonusT2 = bonus2 * t2.getTruckLevel()/10;
            double realMoneyT1Pay = A - realBonusT1;
            double realMoneyT2Get = B + realBonusT2;

            //实际收益记录
            t1.getCargoBenefit().put(cargo,t1.getCargoBenefit().get(cargo) - realMoneyT1Pay);
            t2.getCargoBenefit().put(cargo,t2.getCargoBenefit().get(cargo) + realMoneyT2Get);
            bank.setMoneyLog(bank.getMoneyLog() + "\n由于订单" + cargo.getID() + "的完成");
            // t1恢复冻结的金额 然后付给平台该付的前
            bank.freezeMoneyRecovery(t1,A);
            bank.transfer(t1,lp,A);
            // t2获得该得到的钱
            bank.transfer(lp,t2,B);
            // 平台进行红包反馈
            bank.setMoneyLog(bank.getMoneyLog() + "\n对于评级为分别为"+t1.getTruckLevel()+"和"+t2.getTruckLevel()+"的承运方，由于红包奖励");
            bank.transfer(lp,t1,realBonusT1);
            bank.transfer(lp,t2,realBonusT2);
            System.out.println("\n\n\n进入循环\n");
            t1.setMoneyLog(t1.getMoneyLog() + "\n由于对订单" + cargo.getID() + "进行了转手，支付" + df.format(A));
            t1.setMoneyLog(t1.getMoneyLog() + "\n由于平台红包奖励，获得" + df.format(realBonusT1));
            t2.setMoneyLog(t2.getMoneyLog() + "\n由于完成订单" + cargo.getID() + "获得" + df.format(B));
            t2.setMoneyLog(t2.getMoneyLog() + "\n由于平台红包奖励，获得"  + df.format(realBonusT2));
            tempTruck = t1;
        }

        //对最初接单车辆进行结算。如果没有转手，则不会进行上面的while，而是直接只进行下面代码
        Receiver receiver = cargo.getReceiver();
        double A = cargo.getOfferPriceStack().pop();
        double B = cargo.getBidPriceStack().pop();

        double bonus1 = lp.bonusOfProfitSharing(A,B,PROFIT_SHARE_RATIO1,BONUS_MAX_RATIO);
        double bonus2 = lp.bonusOfProfitSharing(A,B,PROFIT_SHARE_RATIO2,BONUS_MAX_RATIO);

        Truck t2 = tempTruck;
        double realBonusT1 = bonus1 * shipper.getShipperLevel()/10;
        double realBonusT2 = bonus2 * t2.getTruckLevel()/10;

        double realMoneyT1Pay = A - realBonusT1;
        double realMoneyT2Get = B + realBonusT2;

        //实际收益记录
        shipper.getCargoBenefit().put(cargo,shipper.getCargoBenefit().get(cargo) - realMoneyT1Pay);
        t2.getCargoBenefit().put(cargo,t2.getCargoBenefit().get(cargo) + realMoneyT2Get);

        // 对最早的承运方进行结算
        // 由于订单完成，发货方要付给平台
        bank.setMoneyLog(bank.getMoneyLog() + "\n由于订单" + cargo.getID() + "的完成");
        bank.freezeMoneyRecovery(shipper,cargo.getOriginFreightFare());
        bank.transfer(shipper,lp,cargo.getOriginFreightFare());
        bank.transfer(lp,t2,B);
        // 平台进行红包反馈
        bank.setMoneyLog(bank.getMoneyLog() + "\n对于评级为的"+ shipper.getShipperLevel() +"发货方，评级为"+ t2.getTruckLevel() +"的承运方,由于红包奖励");
        bank.transfer(lp,shipper,realBonusT1);
        bank.transfer(lp,t2,realBonusT2);

        shipper.setMoneyLog(shipper.getMoneyLog() + "\n由于订单" + cargo.getID() + "完成，支付" + df.format(cargo.getOriginFreightFare()));
        shipper.setMoneyLog(shipper.getMoneyLog() + "\n由于平台红包奖励，获得" + df.format(realBonusT1));
        t2.setMoneyLog(t2.getMoneyLog() + "\n由于完成订单" + cargo.getID() + "，获得" + df.format(B));
        t2.setMoneyLog(t2.getMoneyLog() + "\n由于平台红包奖励，获得"  + df.format(realBonusT2));

        shipper.setCurrentCargoNum(shipper.getCurrentCargoNum() - 1);
        receiver.setCurrentCargoNum(receiver.getCurrentCargoNum() - 1);

        // 平台结算该订单之后的资金
        double lpNowMoney = bank.getAccount_MoneyMap().get(lp);
        // 平台该单实际的收入
        double profit = lpNowMoney - lpBeforeMoney;
        // 平台收益界面刷新
        jFrameChooseActor.getjFramePlatformProfit().addNewOrderSettlementRecord("订单" + cargo.getID() + "完成，平台此次收益为" + df.format(profit));
        lp.setTrueProfit(lp.getTrueProfit() + profit);
        jFrameUseCaseTest.refreshUseCase10();

    }

    // 该函数用来 重新产生一个与各属性与原定单相同，可以做项修改的货物：运费的重新设定（撤单重新提交），出发地与目的地的互换（已运撤单）。
    private static Cargo getNewSimilarCargo(Simulator simulator,Cargo tempCargo,double newFare,boolean changePosition) {
        Shipper shipper = tempCargo.getShipper();
        Receiver receiver = tempCargo.getReceiver();
        Cargo tempNewCargo;
        if (changePosition) {
            tempNewCargo = new Cargo(
                    Parcel.builder(tempCargo.getDeliveryLocation(),
                            tempCargo.getPickupLocation())
                            .serviceDuration(SERVICE_DURATION)
                            .neededCapacity(tempCargo.getNeededCapacity())
                            .buildDTO());
        } else {
            tempNewCargo = new Cargo(
                    Parcel.builder(tempCargo.getPickupLocation(),
                            tempCargo.getDeliveryLocation())
                            .serviceDuration(SERVICE_DURATION)
                            .neededCapacity(tempCargo.getNeededCapacity())
                            .buildDTO());
        }
        // 重量体积改为原订单
        tempNewCargo.setVolume(tempCargo.getVolume());
        tempNewCargo.setWeight(tempCargo.getWeight());
        // 货物所需运输类型更改
        tempNewCargo.setNeededCarryType(tempCargo.getNeededCarryType());
        // 新订单的运费更改
        tempNewCargo.setFreightFare(newFare);
        tempNewCargo.setOriginFreightFare(newFare);
        // 要求时间
        tempNewCargo.setLimitedHour(tempCargo.getLimitedHour());
        // 赔偿额度
        tempNewCargo.setLiquidatedDamages(tempCargo.getLiquidatedDamages());

        // 冻结新的运费数值的金额
        bank.freezeMoney(shipper,newFare);
        System.out.println("收取新运费");
        // 继续收取展位费
        bank.transfer(shipper,lp,EXHIBITION_FEE);

        // 将新货物与发货方、收货方关联
        shipper.getCargos().add(tempCargo);
        tempNewCargo.setShipper(shipper);
        tempNewCargo.setReceiver(receiver);
        simulator.register(tempNewCargo);
        return tempNewCargo;
    }

    // 判断货车是否剩余 足够的 体积 和 重量
    public static boolean haveEnoughCapacity(Cargo cargo,Truck truck) {
        if (cargo.getVolume() > truck.getCurrentVolume()) {
            System.out.println("该货车剩余体积不足! 货物要求体积为"+cargo.getVolume()+"  而货车当前体积容量为"+truck.getCurrentVolume());
            return false;
        } else if (cargo.getWeight() > truck.getCurrentWeight()) {
            System.out.println("该货车剩余体积不足! 货物要求承重为"+cargo.getWeight()+"  而货车当前还能承重为"+truck.getCurrentWeight());
            return false;
        }
        else {
            return true;
        }
    }

    // 令没运货的货车变为不可见
    private static void  setTruckInvisible(Simulator simulator,PDPModel pdpModel) {
        int n = lp.getTrucksInPlatform().size();
        for(int i = 0; i < n; i++) {
            Truck truck = lp.getTrucksInPlatform().get(i);
            // 当货车不再有订单，货车当前可见，货车当前不在卸货 这三个条件均满足才可以令货车不可见
            if (((truck.getMyDoingCargoNum() + truck.getMyToDoCargoNum()) == 0) && truck.isShowInGraph()
                    && (pdpModel.getVehicleState(truck) != PDPModel.VehicleState.DELIVERING)
            ) {
                truck.setShowInGraph(false);
                simulator.unregister(truck);
            }
        }
    }

    // 从设置界面获取所传递的参数，以此修改模拟器的参数
    private static void transferParameters(JFrameSetup j) {
        int temp1_1 = Integer.parseInt((j.getT11t().getText()));
        PROFIT_SHARE_RATIO1 = temp1_1 / 100.0;
        int temp1_2 = Integer.parseInt((j.getT12t().getText()));
        PROFIT_SHARE_RATIO2 = temp1_2 / 100.0;
        int temp1_3 = Integer.parseInt((j.getT13t().getText()));
        PROFIT_SHARE_RATIO3 = temp1_3 / 100.0;

        int temp2 = Integer.parseInt((j.getT2().getText()));
        BONUS_MAX_RATIO = temp2 / 100.0;
        int temp3 = Integer.parseInt((j.getT3().getText()));
        MIN_ORDERPRICE = temp3 / 100.0;
        int temp4 = Integer.parseInt((j.getT4().getText()));
        EXHIBITION_FEE = temp4;
        int temp5 = Integer.parseInt((j.getT5().getText()));
        WITHDRAWALFEERATIO = temp5 / 100.0;
        int temp6 = Integer.parseInt((j.getT6().getText()));
        NUM_TRUCKS = temp6;

        // 设置货源多少    按照每1辆车 0.35为中 0.45为多 0.25为少
        double temp7 = 0;
        if (j.getGroup1Rb1().isSelected()) {
            temp7 = 0.20/temp6;
            SHIPPER_MONEY = 1000000;
        }
        if (j.getGroup1Rb2().isSelected()) {
            temp7 = 0.10/temp6;
            SHIPPER_MONEY = 100000;
        }
        if (j.getGroup1Rb3().isSelected()) {
            temp7 = 0.05/temp6;
            SHIPPER_MONEY = 10000;
        }

        newCargoProb = temp7;

        // 设置货车抢单意愿  按照 0.05为高 0.02为中 0.01为低
        double temp8 = 0;
        if (j.getGroup2Rb1().isSelected()) {
            temp8 = 0.05;
        }
        if (j.getGroup2Rb2().isSelected()) {
            temp8 = 0.02;
        }
        if (j.getGroup2Rb3().isSelected()) {
            temp8 = 0.01;
        }
        automaticBidRatio = temp8;

        // 设置货车只接附近货车概率  按照 0.9为高 0.5为中 0.2为低
        double temp9 = 0;
        if (j.getGroup3Rb1().isSelected()) {
            temp9 = 0.9;
        }
        if (j.getGroup3Rb2().isSelected()) {
            temp9 = 0.5;
        }
        if (j.getGroup3Rb3().isSelected()) {
            temp9 = 0.2;
        }
        chooseFilterProb = temp9;

        String str = j.getTruckID().getText();
        specialTruckID = str;

        int temp11 = Integer.parseInt((j.getRanking().getText()));
        specialTruckRanking = temp11;

        System.out.println("specialTruckID = "+ specialTruckID +"    specialTruckRanking = "+specialTruckRanking);
    }

    // 随机产生一个货物订单
    public static Cargo randCargo(RoadModel roadModel, RandomGenerator rng) {
        // 随机指定一个发货方来进行发货
        int shipperIndex = (int)(Math.random()*lp.getShippersInPlatform().size());
        Shipper shipper = lp.getShippersInPlatform().get(shipperIndex);
        // 随机指定一个收货方来收货
        int receiverIndex = (int)(Math.random()*lp.getReceiversInPlatform().size());
        Receiver receiver = lp.getReceiversInPlatform().get(receiverIndex);
        // 货物分布正常随机产生
        Cargo tempCargo = new Cargo(
                Parcel.builder(roadModel.getRandomPosition(rng),
                        roadModel.getRandomPosition(rng))
                        .serviceDuration(SERVICE_DURATION)
                        .buildDTO());

        // 给新货物随机产生重量和 10~110
        tempCargo.setWeight(MyTool.getRandom(10,100));
        tempCargo.setVolume(MyTool.getRandom(10,100));

        // 随机产生运费 基准值的20%~500%
        tempCargo.setFreightFare(MyTool.getRandom(CARGO_MONEY * 2,CARGO_MONEY * 5) );
        tempCargo.setOriginFreightFare(tempCargo.getFreightFare());
        // 随机产生违约金数值
        tempCargo.setLiquidatedDamages(MyTool.getRandom(CARGO_MONEY * 2,CARGO_MONEY * 5) );

        // 货物所需运输类型
        double rand1 = Math.random();
        if (rand1 > spacialCargoProb) {
            tempCargo.setNeededCarryType(1);
        } else if (rand1 > spacialCargoProb / 2) {
            tempCargo.setNeededCarryType(2);
        } else {
            tempCargo.setNeededCarryType(3);
        }

        // 将该货物与发货方、收货方关联
        shipper.getCargos().add(tempCargo);
        tempCargo.setShipper(shipper);
        tempCargo.setReceiver(receiver);

        //为货物添加公告板发布寿命
        tempCargo.setPublishingLife(PUBLISHING_LIFE);
        return tempCargo;
    }

    // 产生一个指定出发地 目的地的货物订单
    public static Cargo positionCargo(RoadModel roadModel, double x1,double y1,double x2,double y2) {
        // 随机指定一个发货方来进行发货
        int shipperIndex = (int)(Math.random()*lp.getShippersInPlatform().size());
        Shipper shipper = lp.getShippersInPlatform().get(shipperIndex);
        // 随机指定一个收货方来收货
        int receiverIndex = (int)(Math.random()*lp.getReceiversInPlatform().size());
        Receiver receiver = lp.getReceiversInPlatform().get(receiverIndex);
        // 货物分布正常随机产生
        Cargo tempCargo = new Cargo(
                Parcel.builder(new Point(x1,y1),
                        new Point(x2,y2))
                        .serviceDuration(SERVICE_DURATION)
                        .buildDTO());

        // 给新货物随机产生重量和 10~110
        tempCargo.setWeight(MyTool.getRandom(10,100));
        tempCargo.setVolume(MyTool.getRandom(10,100));

        // 随机产生运费 基准值的20%~500%
        tempCargo.setFreightFare(MyTool.getRandom(CARGO_MONEY * 2,CARGO_MONEY * 5) );
        tempCargo.setOriginFreightFare(tempCargo.getFreightFare());
        // 随机产生违约金数值
        tempCargo.setLiquidatedDamages(MyTool.getRandom(CARGO_MONEY * 2,CARGO_MONEY * 5) );

        // 货物所需运输类型
        double rand1 = Math.random();
        if (rand1 > spacialCargoProb) {
            tempCargo.setNeededCarryType(1);
        } else if (rand1 > spacialCargoProb / 2) {
            tempCargo.setNeededCarryType(2);
        } else {
            tempCargo.setNeededCarryType(3);
        }

        // 将该货物与发货方、收货方关联
        shipper.getCargos().add(tempCargo);
        tempCargo.setShipper(shipper);
        tempCargo.setReceiver(receiver);

        //为货物添加公告板发布寿命
        tempCargo.setPublishingLife(PUBLISHING_LIFE);
        return tempCargo;
    }

    //用例测试，获取要测试的事件并执行
    public static void useCaseTest(Simulator simulator,RoadModel roadModel,RandomGenerator rng) {
        if (jFrameUseCaseTest.getTestEvent() > -1) {
            int testEvent = jFrameUseCaseTest.getTestEvent();
            String para1 = jFrameUseCaseTest.para1;
            int para2 = jFrameUseCaseTest.para2;
            int para3 = jFrameUseCaseTest.para3;
            Truck para4 = jFrameUseCaseTest.para4;
            String para5 = jFrameUseCaseTest.para5;
            Shipper para6 = jFrameUseCaseTest.para6;
            String para7 = jFrameUseCaseTest.para7;
            Receiver para8 = jFrameUseCaseTest.para8;
            Cargo submitCargo = jFrameUseCaseTest.useCaseTest6Cargo;
            Cargo useCaseTest7Cargo = jFrameUseCaseTest.useCaseTest7Cargo;
            Cargo useCaseTest9Cargo = jFrameUseCaseTest.useCaseTest9Cargo;


            // 检查并进行指定的用例测试
            switch (testEvent) {
                case 0:
                    useCase0Process(simulator,roadModel,rng,para1,para2,para3,jFrameUseCaseTest.u0Weight,jFrameUseCaseTest.u0Volumn,jFrameUseCaseTest.u0BankMoney);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 1:
                    useCase1Process(simulator,lp,para4);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 2:
                    useCase2Process(lp,para5,jFrameUseCaseTest.u2BankMoney);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 3:
                    useCase3Process(lp,para6);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 4:
                    useCase4Process(lp,para7);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 5:
                    useCase5Process(lp,para8);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 6:
                    useCase6Process(simulator,submitCargo);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 7:
                    useCase7Process(simulator,useCaseTest7Cargo);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                case 9:
                    useCase9Process(simulator,useCaseTest9Cargo);
                    jFrameUseCaseTest.setTestEvent(-1);
                    break;
                default:
                    System.out.println("用例事件错误！");
            }
        } else {
            // 没有要测试的事件则跳过
            return;
        }
    }

    // 处理用例测试的代码
    // 注册承运方 处理用例测试0
    public static void useCase0Process(Simulator simulator, RoadModel roadModel, RandomGenerator rng, String para1, int para2, int para3,int weight, int volumn, double bankMoney){
        //capacity被体积和重量替换
        Truck tempTruck = new Truck(roadModel.getRandomPosition(rng),0);
        tempTruck.setCurrentVolume(MyTool.getRandom(300,400));
        tempTruck.setCurrentWeight(MyTool.getRandom(300,400));

        tempTruck.setCurrentWeight(weight);
        tempTruck.setCurrentVolume(volumn);
        double tempGuaranteeAmount = para3;
        tempTruck.setCarryType(para2);
        tempTruck.setOpenCargoFliter(false);

        // 记录到平台
        tempTruck.setID(para1);
        // 给货车在银行注册账号
        bank.register(tempTruck,bankMoney);
        // 承运方在担保方注册
        guarantor.register(tempTruck, tempGuaranteeAmount);
        tempTruck.setGuaranteeLog("注册额度：" + df.format(tempGuaranteeAmount));
        tempTruck.setGuarantor(guarantor);

        lp.getTrucksInPlatform().add(tempTruck);
        simulator.register(tempTruck);
        jFrameChooseActor.getjFrameInquiry().addCmd2(lp.getTrucksInPlatform().size() - 1);
        jFrameChooseActor.getjFrameInquiry().addCmd4(lp.getTrucksInPlatform().size() - 1);
        tempTruck.setRegisterTime(tickCount);
    }

    // 注销承运方 处理用例测试1
    public static void useCase1Process(Simulator simulator, LogisticsPlatform lp, Truck para4) {
        jFrameChooseActor.getjFrameInquiry().deleteItem(para4.getID());
        lp.getTrucksInPlatform().remove(para4);
    }

    // 注册发货方 处理用例测试2
    public static void useCase2Process(LogisticsPlatform lp, String para, double u2BnakMoney) {

        Shipper shipper = new Shipper(para);
        bank.register(shipper,u2BnakMoney);
        shipper.setRegisterTime(tickCount);
        lp.getShippersInPlatform().add(shipper);
    }

    // 注销发货方 处理用例测试3
    public static void useCase3Process(LogisticsPlatform lp,Shipper para) {
        lp.getShippersInPlatform().remove(para);
    }

    // 注册收货方 处理用例测试4
    public static void useCase4Process(LogisticsPlatform lp,String para) {
        Receiver receiver = new Receiver(para);
        receiver.setRegisterTime(tickCount);
        lp.getReceiversInPlatform().add(receiver);
    }

    // 注销收货方 处理用例测试5
    public static void useCase5Process(LogisticsPlatform lp,Receiver para) {
        lp.getReceiversInPlatform().remove(para);
    }

    // 订单发布 处理用例测试6
    public static void useCase6Process(Simulator simulator,Cargo cargo) {
        Shipper shipper = cargo.getShipper();
        // 如果发货方的余额不足以支付订单费用，则该订单无法发布。该发货方必须要激活
        if (orderReleaseSuccess(cargo,shipper,simulator) && shipper.isActivated()) {
            System.out.println("\n新订单发布成功!"+cargo.getID());
            lp.getCargoPublishing().add(cargo);
            jFrameChooseActor.getjFramePublishing().refresh();
            jFrameChooseActor.getjFramePublishing().setResult("新订单发布！订单ID为"+ cargo.getID());
            lp.setBillingLog(lp.getBillingLog() + "\n新订单" + cargo.getID() + " 发布;报价为 " + cargo.getOriginFreightFare());
        }
        else {
            jFrameChooseActor.getjFramePublishing().setResult("并没有发布成功");
        }
    }

    // 撤单 处理用例测试7
    public static void useCase7Process(Simulator simulator,Cargo cargo) {
        // 撤单时，货物的状态
        int status = cargo.getCargoStatus();
        jFrameChooseActor.getjFrameInquiry().initCMB();
        switch (status){
            case 1:
                // 由于撤单，要先把之前的担保额度返还
                for (int j = 0; j < lp.getTrucksInPlatform().size(); j++) {
                    if(cargo.getTempbid().containsKey(lp.getTrucksInPlatform().get(j))){
                        guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于" + cargo.getID() + "进行了撤单");
                        guarantor.freezeMoneyRecovery(lp.getTrucksInPlatform().get(j),cargo.getLiquidatedDamages());
                        cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于进行了撤单"
                                + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));
                    }
                }
                // 未接单，直接删除订单
                lp.getCargoPublishing().remove(cargo);
                //更新到公告栏
                jFrameChooseActor.getjFramePublishing().refresh();
                jFrameChooseActor.getjFramePublishing().setResult("订单"+cargo.getID()+"进行了撤单！");
                lp.getCargosInPlatform().remove(cargo);
                bank.setMoneyLog(bank.getMoneyLog() + "\n由于进行了【未被接单】货物的撤单");
                bank.freezeMoneyRecovery(cargo.getShipper(),cargo.getOriginFreightFare());

                simulator.unregister(cargo);
                break;
            case 2:
                // 接单未运,从平台、承运方删除订单，发货方支付承运方运费
                lp.getCargosInPlatform().remove(cargo);
                Truck truck = cargo.getMyTruck();
                truck.setSpacialCargoEndLog(truck.getSpacialCargoEndLog() + "\n订单" + cargo.getID()
                        + "进行了【已接未运】货物的撤单\n承运方获得赔偿" + cargo.getFreightFare() * WITHDRAWALFEERATIO);
                truck.deleteToDoCargo(truck.findToDoCargoIndex(cargo));
                jFrameChooseActor.getjFrameInquiry().deleteCargo(cargo.getID());
                lp.getCargosInPlatform().remove(cargo);
                bank.setMoneyLog(bank.getMoneyLog() + "\n由于进行了【已接未运】货物的撤单");
                bank.freezeMoneyRecovery(cargo.getShipper(),cargo.getOriginFreightFare());
                bank.transfer(cargo.getShipper(),lp,cargo.getOriginFreightFare() * WITHDRAWALFEERATIO);
                bank.transfer(lp,cargo.getMyTruck(),cargo.getOriginFreightFare() * WITHDRAWALFEERATIO);
                guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于进行了【已接未运】货物的撤单");
                guarantor.freezeMoneyRecovery(cargo.getMyTruck(),cargo.getLiquidatedDamages());
                cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于进行了【已接未运】货物的撤单"
                        + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));
                cargo.getReceiver().getCargos().remove(cargo);
                simulator.unregister(cargo);
                break;
            case 3:
                bank.setMoneyLog(bank.getMoneyLog() + "\n由于进行了【已运】货物的撤单");
                //已经在运，额外令发货方发一个新的、出发到达地相反的订单，原承运方直接获得该订单
                Cargo newCargo = getNewSimilarCargo(simulator,cargo,cargo.getOriginFreightFare(),true);
                guarantor.setMoneyLog(guarantor.getMoneyLog() + "\n由于进行了【已运】货物的撤单");
                guarantor.freezeMoney(cargo.getMyTruck(),cargo.getLiquidatedDamages());
                cargo.getMyTruck().setGuaranteeLog(cargo.getMyTruck().getGuaranteeLog() + "\n由于进行了【已运】货物的撤单"
                        + "\n目前可用担保额为" + df.format(guarantor.getAccount_DisposableMoneyMap().get(cargo.getMyTruck())));

                newCargo.setID("[back]"+cargo.getID());
                newCargo.getReceiver().getCargos().add(newCargo);
                // 接单
                truckGetOrderWithBidding(simulator,cargo.getMyTruck(),newCargo,cargo.getOrderPrice());
                break;
            default:
                System.out.println("\n\n撤单货物状态不正确！\n\n");
                break;
        }
    }

    // 处理用例测试8
    public static void useCase8Process(LogisticsPlatform lp,Receiver para) {
    }

    // 订单转手 处理用例测试9
    public static void useCase9Process(Simulator simulator,Cargo cargo) {
        // 重新增加展示时间
        cargo.setPublishingLife(PUBLISHING_LIFE);
        // 转手方的新出价
        int offerPrice = jFrameUseCaseTest.useCaseTest9CargoOfferMoney;
        // 此时令新出价作为货物的原始价格，当没人接单的时候，恢复为出栈的价格；有人接单，则该价格入栈
        cargo.setFreightFare(offerPrice);
        lp.setBillingLog(lp.getBillingLog() + "\n新订单发布" + cargo.getID() + "报价为" + cargo.getFreightFare());
        cargo.setCargoStatus(5);
        // 清空原来的出价
        cargo.setHaveBid(false);
        cargo.setTempbid(newHashMap());
        // 放入公告栏
        lp.getCargoPublishing().add(cargo);
        jFrameChooseActor.getjFramePublishing().refresh();
        jFrameChooseActor.getjFramePublishing().setResult("新订单发布！订单ID为"+ cargo.getID());
    }

    // 处理用例测试10
    public static void useCase10Process(LogisticsPlatform lp,Receiver para) {

    }
    // 处理用例测试11
    public static void useCase11Process(LogisticsPlatform lp,Receiver para) {
    }

    // 绘制界面
    static View.Builder createGui(Display display) {
    View.Builder view = View.builder()
            .with(GraphRoadModelRenderer.builder())
            .with(RoadUserRenderer.builder()
                    .withImageAssociation(
                            Truck.class, "/graphics/perspective/semi-truck-32.png")
                    .withImageAssociation(
                            Cargo.class, "/graphics/perspective/deliverypackage.png"))
            .with(TruckRenderer.builder(TruckRenderer.Language.CHINESE))
            .withTitleAppendix("物流平台模拟")
            .withDisplay(display);
    return view;
    }

    // 加载文件
    static Graph<MultiAttributeData> loadGraph(String name) {
        try {
            if (GRAPH_CACHE.containsKey(name)) {
                return GRAPH_CACHE.get(name);
            }
            final Graph<MultiAttributeData> g = DotGraphIO
                    .getMultiAttributeGraphIO(
                            Filters.selfCycleFilter())
                    .read(
                            TruckLogistics.class.getResourceAsStream(name));
            GRAPH_CACHE.put(name, g);
            return g;
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // 承运方评级的动态变化  根据评分规则，第一条这里一定满足，345默认满足，实际要判断的是第二条
    private static void dynamicRanking(Truck truck,double bidding,Cargo cargo){
        // 1 3 4 5的基础得分，默认满分
        int rank = 4+1+1;
        // 单独第二点的得分计算
        double point2 = bidding/cargo.getFreightFare() *4;
        rank += (int)point2;
        truck.setTruckLevel(rank);
    }

    public static boolean isAutomaticModeOpen() {
        return automaticModeOpen;
    }

    public static void setAutomaticModeOpen(boolean automaticModeOpen) {
        TruckLogistics.automaticModeOpen = automaticModeOpen;
    }

    public static LogisticsPlatform getLp() {
        return lp;
    }

    public static void setLp(LogisticsPlatform lp) {
        TruckLogistics.lp = lp;
    }

    public static Guarantor getGuarantor() {
        return guarantor;
    }

    public static void setGuarantor(Guarantor guarantor) {
        TruckLogistics.guarantor = guarantor;
    }

    public static Bank getBank() {
        return bank;
    }

    public static void setBank(Bank bank) {
        TruckLogistics.bank = bank;
    }

    public static Lawyer getLawyer() {
        return lawyer;
    }

    public static void setLawyer(Lawyer lawyer) {
        TruckLogistics.lawyer = lawyer;
    }

    public static double getChooseFilterProb() {
        return chooseFilterProb;
    }

    public static void setChooseFilterProb(double chooseFilterProb) {
        TruckLogistics.chooseFilterProb = chooseFilterProb;
    }

    public static double getAutomaticBidRatio() {
        return automaticBidRatio;
    }

    public static void setAutomaticBidRatio(double automaticBidRatio) {
        TruckLogistics.automaticBidRatio = automaticBidRatio;
    }

    public static double getSpacialCargoProb() {
        return spacialCargoProb;
    }

    public static void setSpacialCargoProb(double spacialCargoProb) {
        TruckLogistics.spacialCargoProb = spacialCargoProb;
    }

    public static double getAutomaticResubmitRatio() {
        return automaticResubmitRatio;
    }

    public static void setAutomaticResubmitRatio(double automaticResubmitRatio) {
        TruckLogistics.automaticResubmitRatio = automaticResubmitRatio;
    }

    public static double getNewCargoProb() {
        return newCargoProb;
    }

    public static void setNewCargoProb(double newCargoProb) {
        TruckLogistics.newCargoProb = newCargoProb;
    }

    public static double getWithdrawalProbability() {
        return withdrawalProbability;
    }

    public static void setWithdrawalProbability(double withdrawalProbability) {
        TruckLogistics.withdrawalProbability = withdrawalProbability;
    }

    public static double getOrderChangeProbability() {
        return orderChangeProbability;
    }

    public static void setOrderChangeProbability(double orderChangeProbability) {
        TruckLogistics.orderChangeProbability = orderChangeProbability;
    }

    public static int getValidOrderNum() {
        return validOrderNum;
    }

    public static void setValidOrderNum(int validOrderNum) {
        TruckLogistics.validOrderNum = validOrderNum;
    }

    public static int getNumTrucks() {
        return NUM_TRUCKS;
    }

    public static void setNumTrucks(int numTrucks) {
        NUM_TRUCKS = numTrucks;
    }

    public static int getNumShipper() {
        return NUM_SHIPPER;
    }

    public static void setNumShipper(int numShipper) {
        NUM_SHIPPER = numShipper;
    }

    public static int getNumReceiver() {
        return NUM_RECEIVER;
    }

    public static void setNumReceiver(int numReceiver) {
        NUM_RECEIVER = numReceiver;
    }

    public static double getShipperMoney() {
        return SHIPPER_MONEY;
    }

    public static void setShipperMoney(double shipperMoney) {
        SHIPPER_MONEY = shipperMoney;
    }

    public static double getCargoMoney() {
        return CARGO_MONEY;
    }

    public static void setCargoMoney(double cargoMoney) {
        CARGO_MONEY = cargoMoney;
    }

    public static double getTruckGurarantee() {
        return TRUCK_GURARANTEE;
    }

    public static void setTruckGurarantee(double truckGurarantee) {
        TRUCK_GURARANTEE = truckGurarantee;
    }

    public static double getProfitShareRatio1() {
        return PROFIT_SHARE_RATIO1;
    }

    public static void setProfitShareRatio1(double profitShareRatio1) {
        PROFIT_SHARE_RATIO1 = profitShareRatio1;
    }

    public static double getProfitShareRatio2() {
        return PROFIT_SHARE_RATIO2;
    }

    public static void setProfitShareRatio2(double profitShareRatio2) {
        PROFIT_SHARE_RATIO2 = profitShareRatio2;
    }

    public static double getProfitShareRatio3() {
        return PROFIT_SHARE_RATIO3;
    }

    public static void setProfitShareRatio3(double profitShareRatio3) {
        PROFIT_SHARE_RATIO3 = profitShareRatio3;
    }

    public static int getTickCount() {
        return tickCount;
    }

    public static void setTickCount(int tickCount) {
        TruckLogistics.tickCount = tickCount;
    }

    public static double getMinOrderprice() {
        return MIN_ORDERPRICE;
    }

    public static void setMinOrderprice(double minOrderprice) {
        MIN_ORDERPRICE = minOrderprice;
    }

    public static void setBonusMaxRatio(double bonusMaxRatio) {
        BONUS_MAX_RATIO = bonusMaxRatio;
    }

    public static double getExhibitionFee() {
        return EXHIBITION_FEE;
    }

    public static void setExhibitionFee(double exhibitionFee) {
        EXHIBITION_FEE = exhibitionFee;
    }

    public static double getAutomaticResubmitIncrease() {
        return AutomaticResubmitIncrease;
    }

    public static void setAutomaticResubmitIncrease(double automaticResubmitIncrease) {
        AutomaticResubmitIncrease = automaticResubmitIncrease;
    }

    public static double getWITHDRAWALFEERATIO() {
        return WITHDRAWALFEERATIO;
    }

    public static void setWITHDRAWALFEERATIO(double WITHDRAWALFEERATIO) {
        TruckLogistics.WITHDRAWALFEERATIO = WITHDRAWALFEERATIO;
    }

    public static long getServiceDuration() {
        return SERVICE_DURATION;
    }

    public static void setServiceDuration(long serviceDuration) {
        SERVICE_DURATION = serviceDuration;
    }

    public static long getTestStopTime() {
        return TEST_STOP_TIME;
    }

    public static void setTestStopTime(long testStopTime) {
        TEST_STOP_TIME = testStopTime;
    }

    public static int getTestSpeedUp() {
        return TEST_SPEED_UP;
    }

    public static void setTestSpeedUp(int testSpeedUp) {
        TEST_SPEED_UP = testSpeedUp;
    }

    public static int getSpeedUp() {
        return SPEED_UP;
    }

    public static void setSpeedUp(int speedUp) {
        SPEED_UP = speedUp;
    }

    public static String getMapFile() {
        return MAP_FILE;
    }

    public static Map<String, Graph<MultiAttributeData>> getGraphCache() {
        return GRAPH_CACHE;
    }

    public static boolean isHaveInit() {
        return haveInit;
    }

    public static void setHaveInit(boolean haveInit) {
        TruckLogistics.haveInit = haveInit;
    }

    public static JFrameSetup getjFrameSetup() {
        return jFrameSetup;
    }

    public static int getSpecialTruckRanking() {
        return specialTruckRanking;
    }

    public static void setSpecialTruckRanking(int specialTruckRanking) {
        TruckLogistics.specialTruckRanking = specialTruckRanking;
    }

    public static DecimalFormat getDf() {
        return df;
    }

    public static void setDf(DecimalFormat df) {
        TruckLogistics.df = df;
    }

    public static void setjFrameSetup(JFrameSetup jFrameSetup) {
        TruckLogistics.jFrameSetup = jFrameSetup;
    }

    public static JFrameUseCaseTest getjFrameUseCaseTest() {
        return jFrameUseCaseTest;
    }

    public static void setjFrameUseCaseTest(JFrameUseCaseTest jFrameUseCaseTest) {
        TruckLogistics.jFrameUseCaseTest = jFrameUseCaseTest;
    }

    public static String getSpecialTruckID() {
        return specialTruckID;
    }

    public static void setSpecialTruckID(String specialTruckID) {
        TruckLogistics.specialTruckID = specialTruckID;
    }

    public static int getReceivedOrderNum() {
        return receivedOrderNum;
    }

    public static void setReceivedOrderNum(int receivedOrderNum) {
        TruckLogistics.receivedOrderNum = receivedOrderNum;
    }

    public static int getPublishingLife() {
        return PUBLISHING_LIFE;
    }

    public static void setPublishingLife(int publishingLife) {
        PUBLISHING_LIFE = publishingLife;
    }

    public static JFrameChooseActor getjFrameChooseActor() {
        return jFrameChooseActor;
    }

    public static void setjFrameChooseActor(JFrameChooseActor jFrameChooseActor) {
        TruckLogistics.jFrameChooseActor = jFrameChooseActor;
    }

    public static double getBonusMaxRatio() {
        return BONUS_MAX_RATIO;
    }
}
