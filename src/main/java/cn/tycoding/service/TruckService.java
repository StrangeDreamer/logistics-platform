package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TruckService {

    private final Logger logger = LoggerFactory.getLogger(TruckService.class);
    private final TruckRepository truckRepository;
    private final CargoRepository cargoRepository;

    public TruckService(TruckRepository truckRepository,CargoRepository cargoRepository) {
        this.truckRepository = truckRepository;
        this.cargoRepository = cargoRepository;
    }

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
    public void deleteTruck(int id){
        truckRepository.findById(id).ifPresent(truck -> {
            truckRepository.delete(truck);
            logger.info("发货发注销成功！");
        });
    }


    // 查询指定id承运方
    public Truck findTrucksById(int truckId){
        return truckRepository.findTruckById(truckId);
    }

    // 查询指定id承运方的订单数量
    public String findTrucksCargoNum(int truckId){
        // TODO 具体实现

        // 所有该货车具有的订单

        List<Cargo> n1=cargoRepository.findAllByTruckId(truckId);
        List<Cargo> n2 = cargoRepository.findAllByTruckIdAndStatus(truckId,2);
        List<Cargo> n3= cargoRepository.findAllByTruckIdAndStatus(truckId,3);


        return "货车"+ truckRepository.findTruckById(truckId).getName()
                + "目前共有订单" + n1.size() + "个, 其中已接未运订单有" + n2.size()+ "个;正在运输订单有" + n3.size()+ "个";
    }


    // 查询所有承运方
    public List<Truck> findAll(){
        return truckRepository.findAll();
    }








    public Cargo startShip(int cargoId) {
        // 开始运货请求
        Cargo cargo = cargoRepository.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 2){
            throw new TruckException("当前货物状态不争取，无法开始运货");
        }
        cargo.setStatus(3);
        cargoRepository.save(cargo);
        return cargoRepository.findCargoById(cargoId);
    }

    public Cargo endShip(int cargoId) {
        //truck已经送达货物，请求验货

        // 开始运货请求
        Cargo cargo = cargoRepository.findCargoById(cargoId);

        // 前提条件的检查
        if (cargo.getStatus() != 3){
            throw new TruckException("当前货物状态不正确，无法转入运达状态");
        }
        cargoRepository.findCargoById(cargoId).setStatus(4);
        cargoRepository.save(cargo);
        return cargoRepository.findCargoById(cargoId);
    }
}
