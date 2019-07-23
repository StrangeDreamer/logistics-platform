package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Platform;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.service.CargoService;
import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cargos")

public class CargoResource {

    private final Logger logger = LoggerFactory.getLogger(CargoResource.class);

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;
    private final PlatformRepository platformRepository;
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WebSocketTest webSocketTest;

    public CargoResource(CargoService cargoService, CargoRepository cargoRepository, PlatformRepository platformRepository) {
        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
        this.platformRepository = platformRepository;
    }

    /**
     * 查询所有货物
     *
     * @return
     */
    @GetMapping
    public List<Cargo> getAllCargos() {
        logger.info("REST 查询所有货物");
        return cargoService.findAllCargos();
    }



    /**
     * 查询指定ID订单
     *
     * @return Cargo
     */
    @GetMapping("/{cargoId}")
    public Cargo getCargoById(@PathVariable("cargoId") int cargoId) {
        logger.info("REST 查询所有货物");
        return cargoService.findCargoById(cargoId);
    }

    /**
     * 提交订单
     * <p>
     * 使用@RequestParam时，URL是这样的：http://host:port/path?参数名=参数值
     * <p>
     * 使用@PathVariable时，URL是这样的：http://host:port/path/参数值
     *
     * @param cargo
     * @return
     */
    @PostMapping
    public Cargo createCargo(@RequestBody Cargo cargo) {

        return cargoService.createCargo(cargo);
    }




    /**
     *
     * 平台确定某运单
     *
     * @return
     */
    @PutMapping("/{cargoId}")
    public Cargo startCargo(@PathVariable int cargoId) {
        Platform platform = platformRepository.findRecentPltf();
        int time = platform.getBidingDuration();
        Cargo cargo = cargoService.findCargoById(cargoId);
        Date bidStartTime = new Date();
        Date bidEndTime = new Date(bidStartTime.getTime() + time * 1000);
        //先更新DB，再删除cache
        cargo.setBidStartTime(bidStartTime);
        cargo.setBidEndTime(bidEndTime);
        cargo.setStatus(1);
        cargoRepository.save(cargo);
        //删除map中的某个对象
        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        logger.info("订单{}*****{}开始抢", cargoId, bidStartTime);
        webSocketTest.sendAllCode(3);
        return cargo;
    }

    /**
     * 撤单
     *
     * @param id
     * @return
     */
    @DeleteMapping("/withdrawCargo/{id}")
    public Cargo withdrawalCargo(@PathVariable("id") int id) {
        logger.info("Rest 撤单请求{}" + id);
        return cargoService.withdrawCargo(id);
    }


    /**
     * 转单（更新订单）
     *
     * @param id
     * @return
     */

    @PutMapping("/{cargoId}/{freightFare}")
    public Cargo getCargo(@PathVariable("cargoId") int id, @PathVariable("freightFare") double freightFare) {
        logger.info("REST 转单");
        //return cargoService.updateCargoInfo(id,cargoInfoChangeDTO);
        return cargoService.updateCargoInfo(id, freightFare);
    }


    /**
     * 查询承运方的所有订单
     *
     * @param id
     * @return
     */
    @GetMapping("/{who}/{id}")
    public List<Cargo> getTruckAllCargos(@PathVariable("who") String who,@PathVariable("id") int id) {
        if (who.equals("trucks")){
            int truckId=id;
            logger.info("REST 查询发货方{}所有订单", truckId);
            return cargoService.findAllByTruckId(truckId);
        }
        else if (who.equals("shippers")){
            int shipperId=id;
            logger.info("REST 查询发货方{}所有订单", shipperId);
            return cargoService.findAllByShipperId(shipperId);
        }
        else {
            int receiverId=id;
            logger.info("REST 查询发货方{}所有订单", receiverId);
            return cargoService.findAllByReceiverId(receiverId);
        }
    }

    /**
     * 查询不同状态的货物
     *
     * @param status
     * @return
     */

    @GetMapping("/cargoStatus/{status}")
    public List<Cargo> getAllNormalCargos(@PathVariable int status) {
        if (status == 9) {

            logger.info("REST 查询所有超时完成的货物");
            return cargoService.findAllTimeOutCargos();
        }
        if (status == 10) {

            logger.info("REST 查询所有订单异常的货物");
            return cargoService.findAbnormalCargos();
        }
        logger.info("REST 查询所有正常完成货物");
        return cargoService.findAllNormalCargos();

    }

    @GetMapping("/history/{preCargoId}")
    public List<Cargo> getAllTransCargos(@PathVariable int preCargoId) {
        logger.info("REST 查询订单转运历史");
        return cargoService.findAllByPreCargoId(preCargoId);
    }



    /**
     * 更新承运方/货物位置信息
     *
     * @return
     */
    @PutMapping("/refreshPosition/{truckId}/{position}")
    public List<Cargo> refreshPosition(@RequestParam("truckId") int truckId, @RequestParam("position") String position) {
        logger.info("更新承运方/货物位置信息");
        return cargoService.refreshPosition(truckId, position);
    }

}
