package cn.tycoding.service;


import cn.tycoding.domain.Truck;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TruckService {

    private final Logger logger = LoggerFactory.getLogger(TruckService.class);
    private final TruckRepository truckRepository;

    public TruckService(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    //发货方注册
    public Truck createTruck(Truck truck){
        Truck truck1 = new Truck();
        truck1.setName(truck.getName());
        truckRepository.save(truck1);
        return truck1;
    }
    //发货方注销
    public void deleteTruck(int id){
        truckRepository.findById(id).ifPresent(truck -> {
            truckRepository.delete(truck);
            logger.info("发货发注销成功！");
        });
    }
}
