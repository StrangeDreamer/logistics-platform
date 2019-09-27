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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;
// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//         System.out.println(df.format(new Date()));// new Date()为获取当前系统时间

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

    private final String cargoKey = "Cargo";
    private final String truckKey = "Truck";
    private final String shipperKey = "Shipper";
    private final String receiverKey = "Receiver";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    // 发货方创建订单
    @Transactional
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
        bankAccountService.addMoneyLog(bankAccountShipper,   df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "提交订单");
        bankAccountService.changeAvailableMoney(bankAccountShipper, (0 - c.getFreightFare()));
        logger.info("发货方被冻结资金" + c.getFreightFare());

        // 发货方支付展位费
        bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "支付展位费" );
        bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + c.getId() + "支付展位费" );
        bankAccountService.transferMoney(bankAccountShipper,bankAccountPlatform,exhibitionFee);
        logger.info("发货方支付展位费" + exhibitionFee);
        logger.info("新订单创建成功!");
        return c;
    }

    /**
     * 撤单
     * @param id
     */
    @Transactional
    public Cargo withdrawCargo(int id) {
        // 获取撤单赔偿比例
        double  withdrawFeeRatio = platformRepository.findRecentPltf().getWithdrawFeeRatio();
        Cargo cargo = cargoService.findCargoById(id);

        BankAccount bankAccountShipper = bankAccountService.check(cargo.getShipperId(), "shipper");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        InsuranceAccount insuranceAccount = insuranceAccountService.check(cargo.getTruckId(), "truck");
        try {
            // 发布时无人接单撤单  第一阶段，货物未上挂撤单
            if (cargo.getStatus() == 0 || cargo.getStatus() == 1 ){
                // 对第二阶段的撤单，还需要对所有出价的货车发送出价失败通知
                if (cargo.getStatus() == 1) {
                    List<Bid> bidlist = bidRepository.findAllByCargoId(cargo.getId());
                    for (Bid bid : bidlist) {
                        webSocketTest.sendToUser3(String.valueOf(bid.getTruckId()), 2);
                    }
                }
                cargo.setStatus(6);
                cargoRepository.save(cargo);
                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());
                bankAccountService.addMoneyLog(bankAccountShipper, df.format(new Date()) + "由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行撤单，展位费不予退回" );
                bankAccountService.addMoneyLog(bankAccountPlatform, df.format(new Date()) + "由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行撤单，展位费不予退回" );
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

                bankAccountService.addMoneyLog(bankAccountShipper,df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.addMoneyLog(bankAccountPlatform,df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.addMoneyLog(bankAccountTruck,df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，发货方向承运方支付赔偿");
                bankAccountService.transferMoney(bankAccountShipper,bankAccountPlatform,wMoney);
                bankAccountService.transferMoney(bankAccountPlatform,bankAccountTruck,wMoney);

                logger.info("由于货物已接未运，发货方" + cargo.getShipperId() +
                        " 支付给承运方" + cargo.getTruckId() + "赔偿，" +
                        "赔偿为运费" + cargo.getFreightFare() + " 乘 撤单赔偿比例" + withdrawFeeRatio +
                        " = " + wMoney);

                // 车辆的担保额度的恢复
                insuranceAccountService.addMoneyLog(insuranceAccount,df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已接未运撤单，货车不再需要运送该订单");
                insuranceAccountService.changeAvailableMoney(insuranceAccount,cargo.getInsurance());
                logger.info("车辆" +cargo.getTruckId() + "的担保额度恢复" + cargo.getInsurance());
                cargo.setStatus(7);

                cargoRepository.save(cargo);
                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());

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

                // 车辆的担保额度的恢复(实际需要一个隐含条件，需要撤单时，该执行承运方也要有足够的担保额）
                if (bankAccountShipper.getAvailableMoney() < cargo.getFreightFare()) {
                    throw new ShipperException("发货方当前可用资金不足，无法进行已运货物撤单");
                }

                // 由于部分功能在前端实现 此处只是扣除承运方担保额度
                insuranceAccountService.addMoneyLog(insuranceAccount,df.format(new Date()) + "  由于发货方" + cargo.getShipperId() + "对货物" + cargo.getId() + "进行已运货物撤单，货车新增返程订单");
                insuranceAccountService.changeAvailableMoney(insuranceAccount,0 - cargo.getInsurance());
                logger.info("车辆" +cargo.getTruckId() + "的担保额度减少" + cargo.getInsurance());
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
                cargoRepository.save(cargoBack);
                redisTemplate.boundHashOps(cargoKey).delete(cargoBack.getId());

                // 将原来对订单设置为等待验货状态
                cargo.setStatus(4);

                cargoRepository.save(cargo);
                redisTemplate.boundHashOps(cargoKey).delete(cargo.getId());

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
     *  转单
     * @param cargoId
     * @param freightFare
     * @return
     */
    @Transactional
    public Cargo updateCargoInfo(int cargoId, double freightFare) {

        Cargo cargo = findCargoById(cargoId);
        if( cargo.getStatus()!=2) {
            throw new CargoException("当前订单状态无法转单");
        }

        Platform platform = platformRepository.findRecentPltf();
        double exhibitionFee = platform.getExhibitionFee();
        Cargo transferredCargo = new Cargo();
        transferredCargo.setLimitedTime(cargo.getLimitedTime());
        transferredCargo.setType(cargo.getType());
        transferredCargo.setFreightFare(freightFare);
        transferredCargo.setReceiverId(cargo.getReceiverId());
        transferredCargo.setDeparture(cargo.getDeparture());
        transferredCargo.setDestination(cargo.getDestination());
        transferredCargo.setInsurance(cargo.getInsurance());
        transferredCargo.setField(cargo.getField());

        // 转单更新
        transferredCargo.setPreFare(cargo.getFreightFare());

        // 首次转单以及多次转单
        transferredCargo.setPreCargoId(cargo.getId());
        transferredCargo.setShipperId(cargo.getShipperId());
        transferredCargo.setVolume(cargo.getVolume());
        transferredCargo.setWeight(cargo.getWeight());

        // 更新新单状态
        cargo.setStatus(5);
        transferredCargo.setStatus(0);
        cargoRepository.save(cargo);
        cargoRepository.save(transferredCargo);

        // 转手承运方向平台支付展位费

        BankAccount bankAccountTruck = bankAccountService.check(cargo.getTruckId(), "truck");
        BankAccount bankAccountPlatform = bankAccountService.check(1, "platform");
        bankAccountService.addMoneyLog(bankAccountPlatform,df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "转单,平台收取承运方展位费");
        bankAccountService.addMoneyLog(bankAccountTruck,df.format(new Date()) + "  由于承运方" + cargo.getTruckId() + "转单,平台收取承运方展位费");
        bankAccountService.transferMoney(bankAccountTruck,bankAccountPlatform,exhibitionFee);

        logger.info("由于订单转手，承运方" + cargo.getTruckId() + "向平台支付展位费" + exhibitionFee);
//        transferredCargo.setStatus(5);
        cargoRepository.save(transferredCargo);

        logger.info("转单创建成功！");
        return transferredCargo;
    }

    public List<Cargo> findAllCargos() {
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
    @Transactional
    public List<Cargo> refreshPosition(int truckId, String position) {
        Truck truck = truckService.findTruckById(truckId);
        truck.setPosition(position);
        List<Cargo> cargos = findAllByTruckId(truckId);
        List<Cargo> res=new ArrayList<>(cargos.size());
        for (Cargo c :
                cargos) {
            if (c.getStatus() == 3) {
                Cargo cargoRedis=cargoService.findCargoById(c.getId());
                cargoRedis.setPosition(position);
                redisTemplate.boundHashOps(cargoKey).put(c.getId(),cargoRedis);
                cargoRepository.save(cargoRedis);
                res.add(cargoRedis);
            }else {
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
        redisTemplate.boundHashOps(cargoKey).put(cargoId,cargo);
        cargoRepository.save(cargo);
        return cargo;
    }

    // 更新货物状态为收货方未按时验货
    @Transactional
    public Cargo statusChangeTo13(int cargoId) {
        Cargo cargo = cargoService.findCargoById(cargoId);
        cargo.setStatus(13);
        cargoRepository.save(cargo);
        //通知发货方收货方订单验货超时
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()),"4*"+String.valueOf(cargoId));
        webSocketTest4.sendToUser2(String.valueOf(cargo.getShipperId()),"3*"+String.valueOf(cargoId));
        return cargo;
    }

}
