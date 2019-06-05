package cn.tycoding.resource;

import cn.tycoding.domain.Truck;
import cn.tycoding.domain.Truck;
import cn.tycoding.service.TruckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/trucks")
public class TruckResource {

    private final Logger logger=LoggerFactory.getLogger(TruckResource.class);
    private final TruckService truckService;

    public TruckResource(TruckService truckService) {
        this.truckService = truckService;
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
    @GetMapping("/findShipperById/{id}")
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
