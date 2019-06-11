package cn.tycoding.service;


import cn.tycoding.domain.Truck;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TruckService {

    private final Logger logger = LoggerFactory.getLogger(TruckService.class);
    private final TruckRepository truckRepository;

    public TruckService(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
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
        return truckRepository.findShippersById(truckId);
    }

    // 查询所有承运方
    public List<Truck> findAll(){
        return truckRepository.findAll();
    }


}
