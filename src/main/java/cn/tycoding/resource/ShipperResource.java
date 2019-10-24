package cn.tycoding.resource;

import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.Truck;
import cn.tycoding.service.ShipperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shippers")

public class ShipperResource {

    private final Logger logger=LoggerFactory.getLogger(ShipperResource.class);
    private final ShipperService shipperService;

    public ShipperResource(ShipperService shipperService) {
        this.shipperService = shipperService;
    }



    @MyLog(value = "发货方注销请求 ")
    @DeleteMapping("/{id}")
    // 1.如果该发货⽅方有尚未完成的订单，返回订单提醒⽤用户并拒绝注销。
    public String deleteShipper(@PathVariable("id") int id){
        logger.info("Rest 发货方注销请求");
        return shipperService.deleteShipper(id);
    }

    /**
     * 查询指定发货方
     * @return
     */
    @MyLog(value = "查询指定发货方")
    @GetMapping("/{id}")
    public Shipper getShipperById(@PathVariable("id") int id){
        return shipperService.findShipperById(id);
    }

    /**
     * 查询所有发货方
     * @return
     */
    @MyLog(value = " 查询所有发货方")
    @GetMapping
    public List<Shipper> getAllShipper(){
        return shipperService.findAll();
    }

    /**
     * 设置发货方评级
     *
     * @return
     */

    @MyLog(value = "设置发货方评级 ")
    @PutMapping("/ranking/{shipperId}/{rank}")
    public Shipper setShipperRank(@PathVariable("shipperId") int shipperId, @PathVariable("rank") double rank) {
        logger.info("设置发货方评级");
        return shipperService.setShipperRank(shipperId, rank);
    }



    @MyLog(value = "发货方激活")
    @PutMapping("/{id}/activate")
    public Shipper active(@PathVariable("id") int id) {
        return shipperService.active(id);
    }


}
