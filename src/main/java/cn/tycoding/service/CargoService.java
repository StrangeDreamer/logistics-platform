package cn.tycoding.service;

import cn.tycoding.domain.*;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.*;
import cn.tycoding.websocket.WebSocketTest;
import cn.tycoding.websocket.WebSocketTest3;
import cn.tycoding.websocket.WebSocketTest4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//         System.out.println(df.format(new Date()));// new Date()为获取当前系统时间

@Service
@Transactional
public class CargoService {
    private final Logger logger = LoggerFactory.getLogger(CargoService.class);
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private ShipperRepository shipperRepository;
    @Autowired
    private ReceiverRepository receiverRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private TruckService truckService;
    @Autowired
    private WebSocketTest webSocketTest;
    @Autowired
    private WebSocketTest3 webSocketTest3;
    @Autowired
    private WebSocketTest4 webSocketTest4;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InspectionService inspectionService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TransCargoRepository transCargoRepository;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式


    @Transactional
    public void delCargoRedis(int cargoId) {
        String key = "cargo_" + cargoId;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            redisTemplate.delete(key);
            logger.info("从缓存中删除货物！");

        }
    }


    // 发货方创建订单
    @Transactional
    public Cargo createCargo(Cargo cargo) {
        // 检查发货方是否存在，检查收货方是否存在
        Shipper shipper = shipperRepository.findById(cargo.getShipperId()).orElseThrow(() -> new ShipperException("创建订单失败！该发货方不存在！"));
        receiverRepository.findById(cargo.getReceiverId()).orElseThrow(() -> new ReceiverException("创建订单失败！该收货方不存在"));
        Platform platform = platformRepository.findRecentPltf();
        double exhibitionFee = platform.getExhibitionFee();

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        // 发货方资金检查，资金充足(足够支付展位费和运费)才可以发货
        if (bankAccountShipper.getAvailableMoney() < (cargo.getFreightFare() + exhibitionFee)) {
            throw new ShipperException("发货方当前可用资金不足，无法发布订单！");
        }
        // 发货方资金检查，激活才可以发货
        if (!shipper.isActivated()) {
            throw new ShipperException("发货方尚未激活，无法发布订单！");
        }

        Cargo c = new Cargo();
        c.setShipperId(cargo.getShipperId());
        c.setFreightFare(cargo.getFreightFare());
        c.setPreFare(cargo.getFreightFare());
        c.setInsurance(cargo.getInsurance());
        c.setReceiverId(cargo.getReceiverId());
        c.setWeight(cargo.getWeight());
        c.setVolume(cargo.getVolume());
        c.setType(cargo.getType());
        c.setLimitedTime(cargo.getLimitedTime());
        c.setDeparture(cargo.getDeparture());
        c.setDestination(cargo.getDestination());
        c.setRemarks(cargo.getRemarks());
        c.setPosition(cargo.getDeparture());
        c.setCompleteRatio(0);
        c.setField(cargo.getField());
        cargoRepository.save(c);

        // 发货方冻结发货资金
        bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "提交订单");
        bankAccountService.changeAvailableMoney(bankAccountShipper, (0 - c.getFreightFare()));
        logger.info("发货方被冻结资金" + c.getFreightFare());

        // 发货方支付展位费
        bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "支付展位费");
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "支付展位费");
        bankAccountService.transferMoney(bankAccountShipper, bankAccountPlatform, exhibitionFee);
        logger.info("发货方支付展位费" + exhibitionFee);
        logger.info("新订单创建成功!");
        return c;
    }

    /**
     * 撤单
     *
     * @param id
     */
    @Transactional
    public Cargo withdrawCargo(int id) {
        // 获取撤单赔偿比例
        double withdrawFeeRatio = platformRepository.findRecentPltf().getWithdrawFeeRatio();
        Cargo cargo = findCargoById(id);

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        InsuranceAccount insuranceAccount = insuranceAccountService.check(cargo.getTruckId(), "truck");
        try {
            // 发布时无人接单撤单  第一阶段，货物未上挂撤单
            if (cargo.getStatus() == 0 || cargo.getStatus() == 1) {
                // 对第二阶段的撤单，还需要对所有出价的货车发送出价失败通知
                if (cargo.getStatus() == 1) {
                    List<Bid> bidlist = bidRepository.findAllByCargoId(cargo.getId());
                    for (Bid bid : bidlist) {
                        webSocketTest.sendToUser3(String.valueOf(bid.getTruckId()), 2);
                    }
                }
                cargo.setStatus(6);
                cargoRepository.save(cargo);
//                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());
                delCargoRedis(id);
                bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行撤单，展位费不予退回");
                bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行撤单，展位费不予退回");
                bankAccountService.changeAvailableMoney(bankAccountShipper,cargo.getFreightFare());
                logger.info("由于订单未被接单，直接撤单，展位费不予退换");
            }
            // 已接未运撤单  --撤单
            else if (cargo.getStatus() == 2) {
                // 资金流动 发货方向平台支付赔偿，平台将赔偿付给车辆。赔偿金的计算。
                double wMoney = (cargo.getFreightFare() * withdrawFeeRatio);

                if (bankAccountShipper.getAvailableMoney() < wMoney) {
                    throw new ShipperException("发货方资金不足，无法进行撤单");
                }
                logger.info("查询发货方" + cargo.getShipperId() + "是否有充足的可用撤单资金" + wMoney +
                        "资金充足才允许撤单");

                // 支付赔偿
                bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.addMoneyLog(bankAccountTruck, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.transferMoney(bankAccountShipper, bankAccountPlatform, wMoney);
                bankAccountService.transferMoney(bankAccountPlatform, bankAccountTruck, wMoney);

                // 解冻运费
                bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "的货物" + cargo.getId() + "已经取消，运费进行解冻");
                bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "的货物" + cargo.getId() + "已经取消，运费进行解冻");
                bankAccountService.changeAvailableMoney(bankAccountShipper, cargo.getFreightFare());

                logger.info("由于货物已接未运，发货方" + cargo.getShipperId() +
                        " 支付给承运方" + cargo.getTruckId() + "赔偿，" +
                        "赔偿为运费" + cargo.getFreightFare() + " 乘 撤单赔偿比例" + withdrawFeeRatio +
                        " = " + wMoney);

                // 车辆的担保额度的恢复
                insuranceAccountService.addMoneyLog(insuranceAccount, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，货车不再需要运送该订单");
                insuranceAccountService.changeAvailableMoney(insuranceAccount, cargo.getInsurance());
                logger.info("车辆" + cargo.getTruckId() + "的担保额度恢复" + cargo.getInsurance());
                cargo.setStatus(7);

                cargoRepository.save(cargo);
//                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());
                delCargoRedis(id);

                // 通知目标承运方撤单成功
                webSocketTest.sendToUser2(String.valueOf(cargo.getTruckId()), "5 " + cargo.getId());
            }
            // 已经运输的撤单，实际操作是为承运方新增一个订单，该订单与原订单除了出发地和目的地之外一切相同
            // 只有基于一些少见的情况下，这种做法才是或许可行的.此处不写资金结算，不予演示。
            else if (cargo.getStatus() == 3) {
                logger.info("询问发货方" + cargo.getShipperId() + "是否有充足的可用撤单资金" + cargo.getFreightFare() +
                        "资金充足才允许撤单");
                logger.info("由于货物已经运输，发货方" + cargo.getShipperId() +
                        " 支付平台" + cargo.getTruckId() + "双倍运费" +
                        (cargo.getFreightFare() * 2));

                // 车辆的担保额度的恢复(实际需要一个隐含条件，需要撤单时，该执行承运方也要有足够的担保额）
                if (bankAccountShipper.getAvailableMoney() < cargo.getFreightFare()) {
                    throw new ShipperException("发货方当前可用资金不足，无法进行已运货物撤单");
                }

                // 返程订单扣除担保额
                insuranceAccountService.addMoneyLog(insuranceAccount, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已运货物撤单，货车新增返程订单");
                insuranceAccountService.changeAvailableMoney(insuranceAccount, 0 - cargo.getInsurance());
                logger.info("车辆" + cargo.getTruckId() + "的担保额度减少" + cargo.getInsurance());
                // 为该车辆新创建返程订单
                Cargo cargoBack = createCargo(cargo);
                // 返程订单
                cargoBack.setStatus(12);
                cargoBack.setDeparture(cargo.getPosition());
                cargoBack.setDestination(cargo.getDeparture());
                cargoBack.setBidPrice(cargo.getBidPrice());
                cargoBack.setTruckId(cargo.getTruckId());
                cargoBack.setBidEndTime(cargo.getBidEndTime());
                cargoBack.setBidStartTime(cargo.getBidStartTime());
                cargoBack.setPosition(cargo.getPosition());
                cargoBack.setField(cargo.getField());

                // 通过发货人身份证检查是否存在撤单收货人，如果存在，则返程订单由撤单收货人作为收货人，否则，自动根据发货方信息生成撤单收货人
                Shipper shipper = shipperRepository.findShippersById(cargo.getShipperId());
                String bId = shipper.getIdgerenshenfenzheng();
                if (receiverRepository.existsReceiverByIdgerenshenfenzheng(bId)) {
                    // 存在撤单收货人，直接将收货方改为该收货人
                    Receiver bReceiver = receiverRepository.findReceiverByIdgerenshenfenzheng(bId);
                    cargoBack.setReceiverId(bReceiver.getId());
                } else {
                    // 不存在撤单收货人，则根据发货方信息新增收货人。
                    Receiver backReciver = new Receiver();
                    backReciver.setName(shipper.getName() + "的撤单收货账号");
                    backReciver.setActivated(true);
                    backReciver.setAddress(shipper.getAddress());
                    backReciver.setId_gongsitongyidaima(shipper.getId_gongsitongyidaima());
                    backReciver.setIdgerenshenfenzheng(shipper.getIdgerenshenfenzheng());
                    backReciver.setOccupation(shipper.getOccupation());
                    backReciver.setTelNumber(shipper.getTelNumber());
                    receiverRepository.save(backReciver);

                    this.userRepository.save(User.builder()
                            .username(backReciver.getName())
                            .kind(3)
                            .password(this.passwordEncoder.encode("123456"))
                            .ownId(backReciver.getId())
                            .roles(Arrays.asList( "ROLE_USER"))
                            .build()

                    );

                    cargoBack.setReceiverId(backReciver.getId());
                }


                // 原来的订单设置为运达目的地，并且验货正常自动正常完成
                cargo.setStatus(4);
                cargoRepository.save(cargoBack);
                cargoRepository.save(cargo);

                // 返还发货方撤单返程订单的展位费
                bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "的货物" + cargo.getId() + "被接单，展位费返还");
                bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "被接单，展位费返还");
                bankAccountService.transferMoney(bankAccountPlatform, bankAccountShipper, platformRepository.findRecentPltf().getExhibitionFee());

                Inspection inspection = new Inspection();
                inspection.setCargoId(cargo.getId());
                inspection.setInspectionResult(8);
                inspection.setTimeoutPeriod(0);
                inspectionService.inspectionCargo(inspection);
//                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());
                delCargoRedis(id);

                // 通知目标承运方撤单成功
                webSocketTest.sendToUser2(String.valueOf(cargo.getTruckId()), "5 " + cargo.getId());
            } else {
                logger.info("订单当前状态不允许撤单");
                throw new CargoException("订单当前状态不允许撤单");

            }
            cargoRepository.save(cargo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cargo;
    }

    /**
     * 转单
     *
     * @param cargoId
     * @param freightFare
     * @return
     */
    @Transactional
    public Cargo updateCargoInfo(int cargoId, double freightFare, String startingPoint) {

        Cargo cargo = findCargoById(cargoId);
        if (cargo.getStatus() != 2 && cargo.getStatus() != 3) {
            throw new CargoException("当前订单状态无法转单");
        }

        Platform platform = platformRepository.findRecentPltf();
        double exhibitionFee = platform.getExhibitionFee();
        Cargo transferredCargo = new Cargo();
        transferredCargo.setLimitedTime(cargo.getLimitedTime());
        transferredCargo.setType(cargo.getType());
        transferredCargo.setFreightFare(freightFare);
        transferredCargo.setReceiverId(cargo.getReceiverId());
        transferredCargo.setDeparture(startingPoint);
        transferredCargo.setDestination(cargo.getDestination());
        transferredCargo.setInsurance(cargo.getInsurance());
        transferredCargo.setField(cargo.getField());
        transferredCargo.setPreFare(cargo.getFreightFare());
        transferredCargo.setPreCargoId(cargo.getId());
        transferredCargo.setShipperId(cargo.getShipperId());
        transferredCargo.setVolume(cargo.getVolume());
        transferredCargo.setWeight(cargo.getWeight());

        // 更新新单状态
        cargo.setStatus(5);
        transferredCargo.setStatus(0);

        cargoRepository.save(cargo);
        delCargoRedis(cargoId);


        // 产生转单transcargo实体 记录转单发货承运方
        TransCargo transCargo = new TransCargo();
        transCargo.setCargoId(transferredCargo.getId());
        transCargo.setDirectShipperId(cargo.getTruckId());
        transCargo.setDirectShipperName(truckRepository.findTruckById(cargo.getTruckId()).getName());
        // 转单cargo变更相应字段
        transferredCargo.setTransCargoName(transCargo.getDirectShipperName());
        transCargoRepository.save(transCargo);
        cargoRepository.save(transferredCargo);



        // 转手承运方向平台支付展位费
        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "转单,平台收取承运方展位费");
        bankAccountService.addMoneyLog(bankAccountTruck, df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "转单,平台收取承运方展位费");
        bankAccountService.transferMoney(bankAccountTruck, bankAccountPlatform, exhibitionFee);

        // 转单前冻结原承运方需要支付运费
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "进行转单而发布了订单,冻结承运方运费");
        bankAccountService.addMoneyLog(bankAccountTruck, df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "进行转单而发布了订单,冻结承运方运费");
        bankAccountService.changeAvailableMoney(bankAccountTruck, 0 - freightFare);


        return transferredCargo;
    }

    public List<Cargo> findAllCargos() {
        return cargoRepository.findAll();
    }

    public Cargo findCargoById(int id) {
        //        Object truck = redisTemplate.boundHashOps(truckKey).get(id);
        String key = "cargo_" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        ValueOperations<String, Cargo> operations = redisTemplate.opsForValue();
        //redis中没有缓存该运单
        if (hasKey) {
            //不支持热部署，否则会类型无法转换
            Cargo cargo = operations.get(key);
            logger.info("从缓存中获取Cargo");
            return cargo;
        }
        //从数据库中获取
        Cargo cargoDb = cargoRepository.findById(id).orElseThrow(() -> new TruckException("该货物不存在"));
        //插入缓存
        operations.set(key, cargoDb, 30, TimeUnit.SECONDS); //缓存的时间仅有30秒钟
        logger.info("Cargo插入缓存 >> ");
        return cargoDb;
    }

    // 查找发货方的所有订单
    public List<Cargo> findAllByShipperId(int shipperId) {
        shipperRepository.findById(shipperId).orElseThrow(() -> new ShipperException("该发货方不存在"));
        return cargoRepository.findAllByShipperId(shipperId);
    }

    // 查找收货方的所有订单
    public List<Cargo> findAllByReceiverId(int receiverId) {
        receiverRepository.findById(receiverId).orElseThrow(() -> new ReceiverException("该收货方不存在"));
        return cargoRepository.findAllByReceiverId(receiverId);
    }

    // 查找承运方的所有订单
    public List<Cargo> findAllByTruckId(int truckId) {
        truckRepository.findById(truckId).orElseThrow(() -> new TruckException("该承运方不存在"));
        return cargoRepository.findAllByTruckId(truckId);
    }

    //查询订单流通历史
    public List<Cargo> findAllByPreCargoId(int preCargoId) {
        return cargoRepository.findAllByPreCargoId(preCargoId);
    }

    public List<Cargo> findAllNormalCargos() {
        return cargoRepository.findAllByStatus(8);
    }

    public List<Cargo> findAllTimeOutCargos() {
        return cargoRepository.findAllByStatus(9);
    }

    public List<Cargo> findAbnormalCargos() {
        return cargoRepository.findAllByStatus(10);
    }


    // 更新承运方/货物位置信息
    @Transactional
    public List<Cargo> refreshPosition(int truckId, String position) {
        Truck truck = truckService.findTruckById(truckId);
        truck.setPosition(position);
        List<Cargo> cargos = findAllByTruckId(truckId);
        List<Cargo> res = new ArrayList<>(cargos.size());
        for (Cargo c :
                cargos) {
            if (c.getStatus() == 3) {
                Cargo cargoRedis = findCargoById(c.getId());
                cargoRedis.setPosition(position);
//                redisTemplate.boundHashOps(cargoKey).put(c.getId(),cargoRedis);
                cargoRepository.save(cargoRedis);
                delCargoRedis(c.getId());
                res.add(cargoRedis);
            } else {
                res.add(c);
            }
        }
        return res;
    }

    // 更新完成度
    @Transactional
    public Cargo refreshCompleteRatio(int cargoId, Double ratio) {
        Cargo cargo = cargoService.findCargoById(cargoId);
        cargo.setCompleteRatio(ratio);
//        redisTemplate.boundHashOps(cargoKey).put(cargoId,cargo);
        cargoRepository.save(cargo);
        delCargoRedis(cargoId);
        return cargo;
    }

    // 更新货物状态为收货方未按时验货
    @Transactional
    public Cargo statusChangeTo13(int cargoId) {
        Cargo cargo = cargoRepository.findCargoById(cargoId);
        if (cargo.getStatus() == 4 || cargo.getStatus() == 14) {
            cargo.setStatus(13);
        } else {
            throw new CargoException("当前状态不为4或者14，无法改为状态13");
        }
        cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date()) + " 验货超时！收货方未在指定时间前对订单"
                + cargo.getId() + "进行验收！");
        cargoRepository.save(cargo);

        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");

        bankAccountService.addMoneyLog(bankAccountTruck, df.format(new Date()) + " 由于订单" + cargo.getId() + "验收超时，订单挂起，交给律师处理");
        bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + " 由于订单" + cargo.getId() + "验收超时，订单挂起，交给律师处理");
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + " 由于订单" + cargo.getId() + "验收超时，订单挂起，交给律师处理");

        delCargoRedis(cargoId);
        //通知发货方和收货方订单验货超时
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()), "4*" + String.valueOf(cargoId));
        webSocketTest4.sendToUser2(String.valueOf(cargo.getReceiverId()), "3*" + String.valueOf(cargoId));

        return cargo;
    }




    // 拒单
    @Transactional
    public Cargo statusChangeTo15(int cargoId) {
        Cargo cargo = findCargoById(cargoId);
        if (cargo.getStatus() == 2) {
            cargo.setStatus(15);
        } else {
            throw new CargoException("当前状态不为2（已接未运），无法改为状态15（拒单）");
        }
        cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date()) + " 接单承运方拒绝装货运输订单"
                + cargo.getId() + "！订单挂起！");


        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");

        bankAccountService.addMoneyLog(bankAccountTruck, df.format(new Date()) + " 由于订单" + cargo.getId() + "被拒单，订单挂起，交给律师处理");
        bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + " 由于订单" + cargo.getId() + "被拒单，订单挂起，交给律师处理");
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + " 由于订单" + cargo.getId() + "被拒单，订单挂起，交给律师处理");


        cargoRepository.save(cargo);
        delCargoRedis(cargoId);
        //通知发货方和收货方订单 订单被拒绝
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()), "6*" + String.valueOf(cargoId));
        webSocketTest4.sendToUser2(String.valueOf(cargo.getReceiverId()), "5*" + String.valueOf(cargoId));
        return cargo;
    }


    // 更新货物状态为 提醒验货时刻
    @Transactional
    public Cargo statusChangeTo14 (int cargoId) {
        Cargo cargo = findCargoById(cargoId);
        if (cargo.getStatus() == 4) {
            cargo.setStatus(14);
        } else {
            throw new CargoException("当前状态不为4，无法改为状态14");
        }
        cargo.setCargoStatusLog(cargo.getCargoStatusLog() + "\n" + df.format(new Date()) + " 验货即将超时！提醒收货方尽快对货物"
                + cargo.getId() + "进行验收！ ");
        cargoRepository.save(cargo);
        delCargoRedis(cargoId);
        //通知发货方和收货方订单验货超时
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()), "5*" + String.valueOf(cargoId));
        webSocketTest4.sendToUser2(String.valueOf(cargo.getReceiverId()), "4*" + String.valueOf(cargoId));
        return cargo;
    }


    // 查找转单历史
    public Stack<Cargo> getTransCargoHistory(int cargoId){
        Stack<Cargo> ans = new Stack<>();
        if (!cargoRepository.existsById(cargoId)){
            throw new CargoException("该货物不存在！");
        }
        Cargo cargo = cargoRepository.findCargoById(cargoId);
        ans.push(cargo);
        while (cargo.getPreCargoId() != null) {
            Cargo tempCargo = cargoRepository.findCargoById(cargo.getPreCargoId());
            ans.push(tempCargo);
            cargo = tempCargo;
        }
        return ans;
    }


}
