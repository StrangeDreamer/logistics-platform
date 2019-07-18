package cn.tycoding.resource;

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
@CrossOrigin(origins = "*")
public class ShipperResource {

    private final Logger logger=LoggerFactory.getLogger(ShipperResource.class);
    private final ShipperService shipperService;

    public ShipperResource(ShipperService shipperService) {
        this.shipperService = shipperService;
    }


    /**
     * 登录
     * @return
     */
    @GetMapping("/login/{name}")
    public Shipper login(@RequestParam("name") String name){
        logger.info("登录");
        return shipperService.login(name);
    }

    @PostMapping
    public Shipper createShipper(@RequestBody Shipper shipper){
        return shipperService.createShipper(shipper);

    }

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
    @GetMapping("/{id}")
    public Shipper getShipperById(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return shipperService.findShipperById(id);
    }

    /**
     * 查询所有发货方
     * @return
     */
    @GetMapping
    public List<Shipper> getAllShipper(){
        logger.info("REST 查询所有货物");
        return shipperService.findAll();
    }

    /**
     * 设置承运方评级
     *
     * @return
     */

    @PutMapping("/setShipperRank/{shipperId}/{rank}")
    public Shipper setShipperRank(@PathVariable("shipperId") int shipperId, @PathVariable("rank") double rank) {
        logger.info("设置承运方评级");
        return shipperService.setShipperRank(shipperId, rank);
    }

    @PutMapping("/active/{id}")
    public Shipper active(@PathVariable("id") int id) {
        logger.info("激活用户");
        return shipperService.active(id);
    }


}
