package cn.tycoding.service;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.domain.User;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.repository.UserRepository;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TruckService {

    private final Logger logger = LoggerFactory.getLogger(TruckService.class);
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private TruckService truckService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private WebSocketTest3 webSocketTest3;
    @Autowired
    private WebSocketTest4 webSocketTest4;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    private final String cargoKey = "Cargo";
    private final String truck_rolesKey = "TruckRoles";

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式


    @Transactional
    public void delTruckRedis(int truckId) {
        String key = "truck_" + truckId;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            redisTemplate.delete(key);
            logger.info("从缓存中删除承运方！");

        }
    }


    /**
     * 查询指定承运方，并存入缓存
     *
     * @param id
     * @return
     */
    @Transactional
    public Truck findTruckById(int id) {
//        Object truck = redisTemplate.boundHashOps(truckKey).get(id);
        String key = "truck_" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        ValueOperations<String, Truck> operations = redisTemplate.opsForValue();
        //redis中没有缓存该运单
        if (hasKey) {
            //不支持热部署，否则会类型无法转换
            Truck truck = operations.get(key);
            logger.info("从缓存中获取Truck");
            return truck;
        }
        //从数据库中获取
        Truck truckDb = truckRepository.findById(id).orElseThrow(() -> new TruckException("该承运方不存在"));
        //插入缓存
        operations.set(key, truckDb, 30, TimeUnit.SECONDS); //缓存的时间仅有30秒钟
        logger.info("Truck插入缓存 >> ");
        return truckDb;
    }

    // 承运方注册
    @Transactional
    public Truck createTruck(Truck truck) {

        // 先检查该注册人的身份证是否已经用于该项的注册
        if (truckRepository.existsTruckByIdgerenshenfenzheng(truck.getIdgerenshenfenzheng())) {
            throw new TruckException("该个人身份证已经被用于注册承运方！");
        }

        // 检查用户名是否已经被占用
        if (truckRepository.existsTruckByName(truck.getName())) {
            throw new TruckException("该用户名已被占用！");
        }


        this.truckRepository.save(Truck.builder()
                .name(truck.getName())
                .availableWeight(truck.getAvailableWeight())
                .availableVolume(truck.getAvailableVolume())
                .type(truck.getType())
                .bankId(truck.getBankId())
                .insuranceId(truck.getInsuranceId())
                .power(truck.getPower())
                .idgerenshenfenzheng(truck.getIdgerenshenfenzheng())
                .id_gongsitongyidaima(truck.getId_gongsitongyidaima())
                .id_xingshizheng(truck.getId_xingshizheng())
                .id_jiashizheng(truck.getId_jiashizheng())
                .telNumber(truck.getTelNumber())
                .address(truck.getAddress())
                .field(truck.getField())
                .build()
        );

        logger.info("A new truck is created !");
        Truck truck1 = truckRepository.findTruckByName(truck.getName()).get();

        this.userRepository.save(User.builder()
                .username(truck.getName())
                .kind(1)
                .ownId(truck1.getId())
                .password(this.passwordEncoder.encode(truck.getPassword()))
                .roles(Arrays.asList("ROLE_USER"))
                .build()

        );

        // 获得银行账号和保险
        bankAccountService.check(truck1.getId(), "truck");
        insuranceAccountService.check(truck1.getId(), "truck");
        return truck1;
    }


    /**
     * 承运方注销
     */

    public String deleteTruck(int id) {
        findTruckById(id);
//        truckRepository.findById(id).orElseThrow(() -> new TruckException("该承运方不存在"));
        List<Cargo> list = cargoRepository.findAllByTruckId(id);
        // 1.如果注册承运⽅方 有正在执⾏行行的订单，则提示⽤用户该订单并拒绝注销。
        // 2.如果承运⽅方仍然有责任纠纷未解决，则提示⽤用户该问题并拒绝注销。
        for (Cargo cargo : list) {

            if (cargo.getStatus() == 2 || cargo.getStatus() == 3) {
                throw new TruckException("注销失败！当前货车还有尚未完成的订单！");
            }
            if (cargo.getStatus() == 10) {
                throw new TruckException("注销失败！当前货车存在异常订单！！");
            }
        }
        truckRepository.deleteById(id);
        delTruckRedis(id);
        logger.info("发货发注销成功！");
        return "删除Truck" + id + "成功";
    }


    // 查询指定id承运方的订单数量
    public String findTrucksCargoNum(int truckId) {
        // 所有该货车具有的订单
        List<Cargo> n1 = cargoService.findAllByTruckId(truckId);
        int n2 = 0, n3 = 0;
        for (Cargo ca : n1) {
            if (ca.getStatus() == 2) {
                n2++;
            }
            if (ca.getStatus() == 3) {
                n3++;
            }
        }
        return "货车" + truckId
                + "当前有已接未运订单" + n2 + "个;正在运输订单" + n3 + "个";
    }

    // 查询所有承运方
    public List<Truck> findAll() {
        return truckRepository.findAll();
    }


    /**
     * 开始装货运输
     *
     * @param cargoId
     * @return
     */
    @Transactional
    public Cargo startShip(int cargoId) {
        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 2 && cargo.getStatus() != 12) {
            throw new TruckException("当前货物状态不正确，无法开始运货");
        }
        cargo.setStatus(3);
        cargoRepository.save(cargo);
        cargoService.delCargoRedis(cargoId);

        //向发货方推送装货运输的通知
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()), "1*" + String.valueOf(cargo.getId()));

        //向收货方推送装货运输的通知,格式为1+后面订单号
        webSocketTest4.sendToUser2(String.valueOf(cargo.getReceiverId()), "1*" + String.valueOf(cargo.getId()));


        return cargoService.findCargoById(cargoId);
    }

    /**
     * 确认交货
     *
     * @param cargoId
     * @return
     */
    @Transactional
    public Cargo endShip(int cargoId) {
        //df.format(new Date()) + "货物，请求验货
        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);
        System.out.println("******************" + cargo.getPosition());

        // 前提条件的检查
        if (cargo.getStatus() != 3) {
            throw new TruckException("当前货物状态不正确，无法转入运达状态");
        }
        cargo.setStatus(4);

        cargo.setCargoStatusLog(df.format(new Date()) + " 承运方" + truckRepository.findTruckById(cargo.getTruckId()).getName()
                + "将订单" + cargo.getId() + "运达至" + cargo.getDestination() + ",等待收货方验货");

        cargoRepository.save(cargo);
        // 每交一单，同步truck缓存与数据库。truck会一直存在缓存中，不会消失
        truckRepository.save(truckService.findTruckById(cargo.getTruckId()));

        cargoService.delCargoRedis(cargoId);
//        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        //向发货方推送确认交货的通知
        webSocketTest3.sendToUser2(String.valueOf(cargo.getShipperId()), "2*" + String.valueOf(cargo.getId()));

        //向收货方推送确认交货的通知,格式为1+后面订单号
        webSocketTest4.sendToUser2(String.valueOf(cargo.getReceiverId()), "2*" + String.valueOf(cargo.getId()));
        return cargoService.findCargoById(cargoId);
    }

    public Truck setTruckRank(int id, double rank) {

//        Truck truck = truckRepository.findById(id).orElseThrow(() -> new TruckException("该承运方不存在"));
        Truck truck=findTruckById(id);
        truck.setRanking(rank);
        truckRepository.save(truck);
        delTruckRedis(id);
        return truck;
    }

    public Truck active(int id) {
//        Truck truck = truckRepository.findById(id).orElseThrow(() -> new TruckException("该承运方不存在"));
        Truck truck=findTruckById(id);
        truck.setActivated(true);
        truckRepository.save(truck);
        logger.info("激活成功！");
        delTruckRedis(id);
        return truck;
    }
}
