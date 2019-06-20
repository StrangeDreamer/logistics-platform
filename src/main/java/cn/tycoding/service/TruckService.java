package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TruckService {

    private final Logger logger = LoggerFactory.getLogger(TruckService.class);
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private CargoRepository cargoRepository;

    private final String truckKey = "Truck";

    @Autowired
    private RedisTemplate redisTemplate;


    // 承运方注册
    public Truck createTruck(Truck truck){
        Truck truck1 = new Truck();
        truck1.setName(truck.getName());
        truck1.setAvailableWeight(truck.getAvailableWeight());
        truck1.setAvailableVolume(truck.getAvailableVolume());
        truck1.setType(truck.getType());
        truck1.setBankId(truck.getBankId());
        truck1.setInsuranceId(truck.getInsuranceId());
        truckRepository.save(truck1);
        logger.info("A new truck is created !");
        return truck1;
    }
    // 承运方注销
    public String deleteTruck(int id){
        List<Cargo> list = cargoRepository.findAllByTruckId(id);
        // 1.如果注册承运⽅方 有正在执⾏行行的订单，则提示⽤用户该订单并拒绝注销。
        // 2.如果承运⽅方仍然有责任纠纷未解决，则提示⽤用户该问题并拒绝注销。
        for (Cargo cargo:list) {
            if (cargo.getStatus() == 2 || cargo.getStatus() == 3) {
                return "注销失败！当前货车还有尚未完成的订单！";
            }
            if (cargo.getStatus() == 10) {
                return "注销失败！当前货车存在异常订单！";
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
        Truck truckDb=truckRepository.findById(truckId).orElseThrow(()->new TruckException("该承运方不存在"));
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


        return "货车id"+ truckId
                + "目前共有订单" + n1.size() + "个, 其中已接未运订单有" + n2+ "个;正在运输订单有" + n3+ "个";
    }


    // 查询所有承运方
    public List<Truck> findAll(){
        return truckRepository.findAll();
    }








    public Cargo startShip(int cargoId) {
        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 2){
            throw new TruckException("当前货物状态不正确，无法开始运货");
        }
        cargo.setStatus(3);
        cargoRepository.save(cargo);
        return cargo;
    }

    public Cargo endShip(int cargoId) {
        //truck已经送达货物，请求验货

        // 开始运货请求
        Cargo cargo = cargoService.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 3){
            throw new TruckException("当前货物状态不正确，无法转入运达状态");
        }

        cargo.setStatus(4);
        cargoRepository.save(cargo);
        return cargo;
    }
}
