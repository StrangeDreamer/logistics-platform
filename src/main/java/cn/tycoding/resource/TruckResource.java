package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.service.TruckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/trucks")
public class TruckResource {

    private final Logger logger=LoggerFactory.getLogger(TruckResource.class);
    private final TruckService truckService;
    private final CargoRepository cargoRepository;

    public TruckResource(TruckService truckService, CargoRepository cargoRepository) {
        this.truckService = truckService;
        this.cargoRepository = cargoRepository;
    }


    @PostMapping("/createTruck")
    public Truck createTruck(@RequestBody Truck truck){
        return truckService.createTruck(truck);

    }

    // 1.如果注册承运⽅方 有正在执⾏行行的订单，则提示⽤用户该订单并拒绝注销。
    // 2.如果承运⽅方仍然有责任纠纷未解决，则提示⽤用户该问题并拒绝注销。
    @DeleteMapping("/deleteTruck/{id}")
    public String deleteTruck(@PathVariable("id") int id){

        logger.info("Rest 承运方注销请求");
        return truckService.deleteTruck(id);

    }

    /**
     * 查询指定承运方
     * @return
     */
    @GetMapping("/findTruckById/{id}")
    public Truck getTrucksById(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return truckService.findTrucksById(id);
    }


    /**
     * 查询指定承运方
     * @return
     */
    @GetMapping("/findTrucksCargoNum/{id}")
    public String findTrucksCargoNum(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return truckService.findTrucksCargoNum(id);
    }


    /**
     * 查询所有承运方
     * @return
     */
    @GetMapping("/allTrucks")
    public List<Truck> getAllTruck(){
        logger.info("REST 查询所有货物");
        return truckService.findAll();
    }

    /**
     * truck 请求开始运货
     * @param cargoId 货号
     * @return
     */
    @PutMapping("/startShip/{cargoId}")
    public Cargo startShip(@PathVariable int cargoId){


        logger.info("truck开始运货，货单号{}",cargoId);
        return truckService.startShip(cargoId);
    }

    @PutMapping("/endShip/{cargoId}")
    public Cargo endShip(@PathVariable int cargoId){
        logger.info("truck已经送达，请求验货");
        return truckService.endShip(cargoId);
    }

}
