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

    @DeleteMapping("/deleteTruck/{id}")
    public String deleteTruck(@PathVariable("id") int id){
        logger.info("Rest 发货方注销请求");
        truckService.deleteTruck(id);
        return "删除Truck"+id+"成功";
    }

    /**
     * 查询指定发货方
     * @return
     */
    @GetMapping("/findTruckById/{id}")
    public Truck getTrucksById(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return truckService.findTrucksById(id);
    }

    /**
     * 查询所有发货方
     * @return
     */
    @GetMapping("/allTrucks")
    public List<Truck> getAllTruck(){
        logger.info("REST 查询所有货物");
        return truckService.findAll();
    }


}
