package cn.tycoding.resource;

import cn.tycoding.domain.Truck;
import cn.tycoding.domain.Truck;
import cn.tycoding.service.TruckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/trucks")
public class TruckResource {

    private final Logger logger=LoggerFactory.getLogger(TruckResource.class);
    private final TruckService truckService;

    public TruckResource(TruckService truckService) {
        this.truckService = truckService;
    }


    @PostMapping
    public Truck createTruck(@RequestBody Truck truck){
        return truckService.createTruck(truck);

    }

    @DeleteMapping("/{id}")
    public String deleteTruck(@PathVariable("id") int id){
        logger.info("Rest 发货方注销请求");
        truckService.deleteTruck(id);
        return "删除Truck"+id+"成功";
    }

}
