package cn.tycoding.service;
import cn.tycoding.domain.BankAccount;
import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final String truckKey = "Truck";
    private final String cargoKey="Cargo";

    @Autowired
    private InsuranceAccountService insuranceAccountService;
    @Autowired
    private BankAccountService bankAccountService;

    // 登录
    public Truck login(String name){
        // 是否查无此人
        return truckRepository.findTruckByName(name).orElseThrow(()->new TruckException("该用户未注册！"));
    }

    
    // 承运方注册
    public Truck createTruck(Truck truck){

        // 先检查该注册人的身份证是否已经用于该项的注册
        if(truckRepository.existsTruckByIdgerenshenfenzheng(truck.getIdgerenshenfenzheng())) {
            throw new TruckException("该个人身份证已经被用于注册承运方！");
        }

        // 检查用户名是否已经被占用
        if(truckRepository.existsTruckByName(truck.getName())) {
            throw new TruckException("该用户名已被占用！");
        }


        Truck truck1 = new Truck();
        truck1.setName(truck.getName());
        truck1.setAvailableWeight(truck.getAvailableWeight());
        truck1.setAvailableVolume(truck.getAvailableVolume());
        truck1.setType(truck.getType());
        truck1.setBankId(truck.getBankId());
        truck1.setInsuranceId(truck.getInsuranceId());
        truck1.setPower(truck.getPower());
        truck1.setIdgerenshenfenzheng(truck.getIdgerenshenfenzheng());
        truck1.setId_gongsitongyidaima(truck.getId_gongsitongyidaima());
        truck1.setId_xingshizheng(truck.getId_xingshizheng());
        truck1.setId_jiashizheng(truck.getId_jiashizheng());
        truck1.setTelNumber(truck.getTelNumber());
        truck1.setAddress(truck.getAddress());
        truckRepository.save(truck1);
        logger.info("A new truck is created !");



        // 获得银行账号和保险
        bankAccountService.check(truck1.getId(),"truck");
        insuranceAccountService.check(truck1.getId(), "truck");


        return truck1;
    }
    // 承运方注销
    public String deleteTruck(int id){
        truckRepository.findById(id).orElseThrow(()->new TruckException("该承运方不存在"));
        List<Cargo> list = cargoRepository.findAllByTruckId(id);
        // 1.如果注册承运⽅方 有正在执⾏行行的订单，则提示⽤用户该订单并拒绝注销。
        // 2.如果承运⽅方仍然有责任纠纷未解决，则提示⽤用户该问题并拒绝注销。
        for (Cargo cargo:list) {

            if (cargo.getStatus() == 2 || cargo.getStatus() == 3) {
                throw new TruckException("注销失败！当前货车还有尚未完成的订单！");
            }
            if (cargo.getStatus() == 10) {
                throw new TruckException("注销失败！当前货车存在异常订单！！");
            }
        }
        truckRepository.findById(id).ifPresent(truck -> {
            truckRepository.delete(truck);
            logger.info("发货发注销成功！");
        });
        return "删除Truck"+id+"成功";
    }

    /**
     * 查询指定承运方，并存入缓存
     * @param id
     * @return
     */
    public Truck findTruckById(int id){
        Truck truck= (Truck) redisTemplate.boundHashOps(truckKey).get(id);
        //redis中没有缓存该运单
        if (truck == null){
            Truck truckDb=truckRepository.findById(id).orElseThrow(()->new TruckException("该承运方不存在"));
            redisTemplate.boundHashOps(truckKey).put(id, truckDb);
            logger.info("RedisTemplate -> 从数据库中读取并放入缓存中");
            truck= (Truck) redisTemplate.boundHashOps(truckKey).get(id);
        }

        return truck;
    }

    // 查询指定id承运方的订单数量
    public String findTrucksCargoNum(int truckId){
        // 所有该货车具有的订单
        List<Cargo> n1=cargoService.findAllByTruckId(truckId);
        int n2=0,n3=0;
        for (Cargo ca :n1) {
            if (ca.getStatus()==2){
                n2++;
            }
            if (ca.getStatus()==3){
                n3++;
            }
        }
        return "货车"+ truckId
                +  "当前有已接未运订单" + n2+ "个;正在运输订单" + n3+ "个";
    }

    // 查询所有承运方
    public List<Truck> findAll(){
        return truckRepository.findAll();
    }

    public Cargo startShip(int cargoId) {
        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 2 && cargo.getStatus() != 12){
            throw new TruckException("当前货物状态不正确，无法开始运货");
        }
        cargo.setStatus(3);
        cargoRepository.save(cargo);
        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        return cargoService.findCargoById(cargoId);
    }

    public Cargo endShip(int cargoId) {
        //truck已经送达货物，请求验货
        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);
        System.out.println("******************"+cargo.getPosition());

        // 前提条件的检查
        if (cargo.getStatus() != 3){
            throw new TruckException("当前货物状态不正确，无法转入运达状态");
        }
        cargo.setStatus(4);
        cargoRepository.save(cargo);
        //没交一单，同步truck缓存与数据库。truck会一直存在缓存中，不会消失
        truckRepository.save(truckService.findTruckById(cargo.getTruckId()));
        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        return cargoService.findCargoById(cargoId);
    }

    public Truck setTruckRank(int id, double rank) {
        Truck truck = truckRepository.findById(id).orElseThrow(()->new TruckException("该承运方不存在"));
        truck.setRanking(rank);
        return truck;
    }

    public Truck active(int id) {
        Truck truck = truckRepository.findById(id).orElseThrow(()->new TruckException("该承运方不存在"));
        truck.setActivated(true);
        truckRepository.save(truck);
        logger.info("激活成功！");
        return truck;
    }
}
