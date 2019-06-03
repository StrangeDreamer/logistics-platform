package cn.tycoding.resource;

import cn.tycoding.domain.Shipper;
import cn.tycoding.service.ShipperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/shippers")
public class ShipperResource {

    private final Logger logger=LoggerFactory.getLogger(ShipperResource.class);
    private final ShipperService shipperService;

    public ShipperResource(ShipperService shipperService) {
        this.shipperService = shipperService;
    }


    @PostMapping
    public Shipper createShipper(@RequestBody Shipper shipper){
        return shipperService.createShipper(shipper);

    }

    @DeleteMapping("/{id}")
    public String deleteShipper(@PathVariable("id") int id){
        logger.info("Rest 发货方注销请求");
        shipperService.deleteShipper(id);
        return "删除shipper"+id+"成功";
    }

}
