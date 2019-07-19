package cn.tycoding.service;

import cn.tycoding.domain.*;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.*;
import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
@Transactional
public class CargoService {
    private final Logger logger = LoggerFactory.getLogger(CargoService.class);
    @Autowired
    private  CargoRepository cargoRepository;
    @Autowired
    private CargoService cargoService;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private  TruckRepository truckRepository;

    @Autowired
    private  ShipperRepository shipperRepository;

    @Autowired
    private  ReceiverRepository receiverRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private InsuranceAccountRepository insuranceAccountRepository;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    WebSocketTest webSocketTest;



    private final String cargoKey = "Cargo";
    private final String truckKey = "Truck";
    private final String shipperKey = "Shipper";
    private final String receiverKey = "Receiver";



    public Cargo createCargo(Cargo cargo) {
        // 检查发货方是否存在，检查收货方是否存在
        Shipper shipper = shipperRepository.findById(cargo.getShipperId()).orElseThrow(()->new ShipperException("创建订单失败！该发货方不存在！"));
        receiverRepository.findById(cargo.getReceiverId()).orElseThrow(()->new ReceiverException("创建订单失败！该收货方不存在"));
        Platform platform = platformRepository.findRecentPltf();
        double exhibitionFee = platform.getExhibitionFee();

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(),"shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1,"platform");
        // 发货方资金检查，资金充足(足够支付展位费和运费)才可以发货
        if (bankAccountShipper.getAvailableMoney() < (cargo.getFreightFare() + exhibitionFee)){
            throw new ShipperException("发货方当前可用资金不足，无法发布订单！");
        }
        // 发货方资金检查，激活才可以发货
        if (!shipper.isActivated()) {
            throw new ShipperException("发货方尚未激活，无法发布订单！");
        }

        logger.info("发货方资金检查，资金充足才可以发货");



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

        cargoRepository.save(c);

        // 发货方冻结发货资金
        bankAccountService.addMoneyLog(bankAccountShipper, "发货方对货物" + c.getId() + "进行发货");
        bankAccountService.changeAvailableMoney(bankAccountShipper, (0 - c.getFreightFare()));
        logger.info("发货方冻结资金" + c.getFreightFare());

        // 发货方支付展位费
        bankAccountService.addMoneyLog(bankAccountShipper, "发货方支付展位费" );
        bankAccountService.addMoneyLog(bankAccountPlatform, "发货方支付展位费" );
        bankAccountService.transferMoney(bankAccountShipper,bankAccountPlatform,exhibitionFee);
        logger.info("发货方支付展位费" + exhibitionFee);

        logger.info("新订单创建成功!");
        return c;
    }

    /**
     * 撤单
     * @param id
     */
    public Cargo withdrawCargo(int id) {
        // 获取撤单赔偿比例
        double  withdrawFeeRatio = platformRepository.findRecentPltf().getWithdrawFeeRatio();
        Cargo cargo = cargoService.findCargoById(id);

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        InsuranceAccount insuranceAccount = insuranceAccountService.check(cargo.getTruckId(), "truck");
        try {
            // 发布时无人接单撤单  --撤单
            if (cargo.getStatus() == 0 ){
                cargo.setStatus(6);
                logger.info("由于订单未被接单，直接撤单，展位费不予退换");

            }
            // 已接未运撤单  --撤单
            else if (cargo.getStatus() == 2){
                // 资金流动 发货方向平台支付赔偿，平台将赔偿付给车辆。赔偿金的计算。
                double wMoney = (cargo.getFreightFare() * withdrawFeeRatio);

                if (bankAccountShipper.getAvailableMoney() < wMoney) {
                    throw new ShipperException("发货方资金不足，无法进行撤单");
                }
                logger.info("查询发货方" + cargo.getShipperId() + "是否有充足的可用撤单资金" + wMoney +
                "资金充足才允许撤单");

                bankAccountService.addMoneyLog(bankAccountShipper,"由于出现已接未运撤单");
                bankAccountService.addMoneyLog(bankAccountPlatform,"由于出现已接未运撤单");
                bankAccountService.addMoneyLog(bankAccountTruck,"由于出现已接未运撤单");

                bankAccountService.transferMoney(bankAccountShipper,bankAccountPlatform,wMoney);
                bankAccountService.transferMoney(bankAccountPlatform,bankAccountTruck,wMoney);

                logger.info("由于货物已接未运，发货方" + cargo.getShipperId() +
                        " 支付给承运方" + cargo.getTruckId() + "赔偿，" +
                        "赔偿为运费" + cargo.getFreightFare() + " 乘 撤单赔偿比例" + withdrawFeeRatio +
                        " = " + wMoney);

                // 车辆的担保额度的恢复
                insuranceAccountService.addMoneyLog(insuranceAccount,"由于出现已接未运撤单");
                insuranceAccountService.changeAvailableMoney(insuranceAccount,cargo.getInsurance());
                logger.info("车辆" +cargo.getTruckId() + "的担保额度恢复" + cargo.getInsurance());
                cargo.setStatus(9);

                // 通知目标承运方撤单成功
                webSocketTest.sendToUser2(String.valueOf(cargo.getTruckId()),"5 " + cargo.getId());


            }
            // 已经运输的撤单，实际操作是为承运方新增一个订单，该订单与原订单除了出发地和目的地之外一切相同
            // 只有基于一些少见的情况下，这种做法才是或许可行的.此处不写资金结算，不予演示。
            else if (cargo.getStatus() == 3) {
                logger.info("询问发货方" + cargo.getShipperId() + "是否有充足的可用撤单资金" + cargo.getFreightFare() +
                        "资金充足才允许撤单");
                logger.info("由于货物已经运输，发货方" + cargo.getShipperId() +
                        " 支付平台" + cargo.getTruckId() + "双倍运费" +
                        (cargo.getFreightFare() * 2));
                // 车辆的担保额度的恢复
                logger.info("车辆" +cargo.getTruckId() + "的担保额度减少" + cargo.getInsurance());
                // 为该车辆新创建返程订单
                Cargo cargoBack = createCargo(cargo);
                cargoBack.setStatus(2);
                cargoBack.setDeparture(cargo.getPosition());
                cargoBack.setDestination(cargo.getDeparture());
                cargoBack.setBidPrice(cargo.getBidPrice());
                cargoBack.setTruckId(cargo.getTruckId());
                cargoBack.setBidEndTime(cargo.getBidEndTime());
                cargoBack.setBidStartTime(cargo.getBidStartTime());
                cargoRepository.save(cargoBack);

                // 将原来对订单设置为等待验货状态
                cargo.setStatus(4);
                cargoRepository.save(cargo);

                // 通知目标承运方撤单成功
                webSocketTest.sendToUser2(String.valueOf(cargo.getTruckId()),"5 " + cargo.getId());
            }
            else {
                logger.info("订单当前状态不允许撤单" );
            }
            cargoRepository.save(cargo);

        }catch (Exception e){
            e.printStackTrace();
        }
        return cargo;
    }

    /**
     * TODO 转单业务逻辑实现
     * @param cargoId
     * @param freightFare
     * @return
     */
    public Cargo updateCargoInfo(int cargoId, double freightFare) {

        Cargo cargo = findCargoById(cargoId);
        if( cargo.getStatus()!=2) {
            throw new CargoException("当前订单状态无法转单");
        }

        Platform platform = platformRepository.findRecentPltf();
        double exhibitionFee = platform.getExhibitionFee();
//        TransferredCargo transferredCargo=new TransferredCargo();
//        transferredCargo.setCargoId(cargo.getId());
//        transferredCargo.setFreightFare(freightFare);
//        transferredCargoRepo.save(transferredCargo);
        Cargo transferredCargo = new Cargo();
        transferredCargo.setLimitedTime(cargo.getLimitedTime());
        transferredCargo.setType(cargo.getType());
        transferredCargo.setFreightFare(freightFare);
        transferredCargo.setReceiverId(cargo.getReceiverId());
        transferredCargo.setDeparture(cargo.getDeparture());
        transferredCargo.setDestination(cargo.getDestination());
        transferredCargo.setInsurance(cargo.getInsurance());

        //转单更新
        transferredCargo.setPreFare(cargo.getFreightFare());

        //首次转单以及多次转单
        transferredCargo.setPreCargoId(cargo.getId());

        transferredCargo.setShipperId(cargo.getShipperId());
        transferredCargo.setVolume(cargo.getVolume());
        transferredCargo.setWeight(cargo.getWeight());

        //更新新单状态
        cargo.setStatus(5);
        transferredCargo.setStatus(0);
        cargoRepository.save(cargo);
        cargoRepository.save(transferredCargo);
        // TODO： 转手承运方向平台支付展位费

        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        bankAccountService.addMoneyLog(bankAccountPlatform,"由于转单需要收取展位费");
        bankAccountService.addMoneyLog(bankAccountTruck,"由于转单需要收取展位费");
        bankAccountService.transferMoney(bankAccountTruck,bankAccountPlatform,exhibitionFee);

        logger.info("由于订单转手，承运方" + cargo.getTruckId() + "向平台支付展位费" + exhibitionFee);

//        transferredCargo.setStatus(5);
        cargoRepository.save(transferredCargo);

        logger.info("转单创建成功！");
        return transferredCargo;
    }


  public List<Cargo> findAllCargos() {
//
//        List<Cargo> cargosList = redisTemplate.boundHashOps(cargoKey).values();
//        if (cargosList == null || cargosList.size() == 0) {
//            //说明缓存中没有秒杀列表数据
//            //查询数据库中秒杀列表数据，并将列表数据循环放入redis缓存中
//            cargosList = cargoRepository.findAll();
//            for (Cargo cargo : cargosList) {
//                //将秒杀列表数据依次放入redis缓存中，key:秒杀表的ID值；value:秒杀商品数据
//                redisTemplate.boundHashOps(cargoKey).put(cargo.getId(),cargo);
//                logger.info("findAll -> 从Mysql数据库中读取并放入Redis缓存中");
//            }
//            cargosList = redisTemplate.boundHashOps(cargoKey).values();
//        } else {
//            logger.info("findAll -> 从Redis缓存中读取");
//        }

        return cargoRepository.findAll();
    }

    public Cargo findCargoById(int id){
        Cargo cargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(id);
        //redis中没有缓存该运单
        if (cargo == null){
            Cargo cargoDb = cargoRepository.findById(id).orElseThrow(()->new CargoException("该订单不存在！"));
            redisTemplate.boundHashOps(cargoKey).put(id, cargoDb);
            logger.info("RedisTemplate -> 从数据库中读取并放入缓存中");
            cargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(id);
        }

        return cargo;
    }

    // 查找发货方的所有订单
    public List<Cargo> findAllByShipperId(int shipperId) {
        shipperRepository.findById(shipperId).orElseThrow(()->new ShipperException("该发货方不存在"));
        return cargoRepository.findAllByShipperId(shipperId);
    }

    // 查找收货方的所有订单
    public List<Cargo> findAllByReceiverId(int receiverId) {
        receiverRepository.findById(receiverId).orElseThrow(()->new ReceiverException("该收货方不存在"));
        return cargoRepository.findAllByReceiverId(receiverId);
    }

    // 查找承运方的所有订单
    public List<Cargo> findAllByTruckId(int truckId) {
        truckRepository.findById(truckId).orElseThrow(()->new TruckException("该承运方不存在"));
        return cargoRepository.findAllByTruckId(truckId);
    }

    //查询订单流通历史
    public List<Cargo> findAllByPreCargoId(int preCargoId){
        return cargoRepository.findAllByPreCargoId(preCargoId);
    }

    public List<Cargo> findAllNormalCargos(){
        return cargoRepository.findAllByStatus(8);
    }

    public List<Cargo> findAllTimeOutCargos(){
        return cargoRepository.findAllByStatus(9);
    }

    public List<Cargo> findAbnormalCargos(){
        return cargoRepository.findAllByStatus(10);
    }


    // 更新承运方/货物位置信息
    public List<Cargo> refreshPosition(int truckId, String position) {
        Truck truck = truckRepository.findById(truckId).orElseThrow(()->new TruckException("该承运方不存在"));
        truck.setPosition(position);
        truckRepository.save(truck);

        List<Cargo> cargos = findAllByTruckId(truckId);

        for(int i = 0; i < cargos.size(); i++) {
            if (cargos.get(i).getStatus() == 3) {
                cargos.get(i).setPosition(position);
                cargoRepository.save(cargos.get(i));
            }
        }

        return cargos;
    }

}
