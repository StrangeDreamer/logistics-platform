package cn.tycoding.resource;

import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Platform;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.CargoService;
import cn.tycoding.websocket.WebSocketTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Stack;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/cargos")

public class CargoResource {

    private final Logger logger = LoggerFactory.getLogger(CargoResource.class);

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;
    private final PlatformRepository platformRepository;
    private final TruckRepository truckRepository;
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WebSocketTest webSocketTest;


    public CargoResource(CargoService cargoService, CargoRepository cargoRepository,
                         PlatformRepository platformRepository,TruckRepository truckRepository
                         ) {
        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
        this.platformRepository = platformRepository;
        this.truckRepository = truckRepository;
    }

    /**
     * 查询所有货物
     *
     * @return
     */
   // @MyLog(value = "查询所有货物")
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
//    @MyLog(value = "查询指定订单")
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
    @MyLog(value = "提交订单")
    @PostMapping
    public Cargo createCargo(@RequestBody Cargo cargo) {

        return cargoService.createCargo(cargo);
    }


    /**
     * 平台确定某运单
     *
     * @return
     */
    @MyLog(value = "平台确定某运单")
    @PutMapping("/{cargoId}")
    public Cargo startCargo(@PathVariable int cargoId) {
        Platform platform = platformRepository.findRecentPltf();
        //获取时长，统一为小时
        int time = platform.getBidingDuration();
        int speedup=platform.getSpeedup();

        Cargo cargo = cargoService.findCargoById(cargoId);
        Date bidStartTime = new Date();
        //小时
        Date bidEndTime = new Date(bidStartTime.getTime() + time * 1000 * speedup);
        //先更新DB，再删除cache
        cargo.setBidStartTime(bidStartTime);
        cargo.setBidEndTime(bidEndTime);
        cargo.setStatus(1);
        cargoRepository.save(cargo);
        //删除map中的某个对象
//        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        cargoService.delCargoRedis(cargoId);
        logger.info("订单{}*****{}开始抢", cargoId, bidStartTime);

        // 发布订单前检查订单是否是群发给所有承运方
        // 如果是货物提交属性filed为"全体承运方"则群发给所有承运方；否则只发给与订单filed属性相同的承运方
        if (cargo.getField().equals("全体承运方")) {
            webSocketTest.sendAllCode(3);
        } else {
            List<Truck> trucks = truckRepository.findTruckByField(cargo.getField());
            if(trucks.size() == 0) {
                throw new CargoException("平台目前没有任何符合圈子条件的承运方！");
            }
            for (Truck truck:
                    trucks) {
                webSocketTest.sendToUser3(String.valueOf(truck.getId()),3);
            }
        }
        return cargo;
    }

    /**
     * 撤单
     *
     * @param id
     * @return
     */
    @MyLog("撤单")
    @DeleteMapping("/{id}/withdraw")
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
    @MyLog(value = "转单")
    @PutMapping("/transfer")
    public Cargo getCargo(@RequestParam int id, @RequestParam double freightFare, @RequestParam String startingPoint) {
        logger.info("REST 转单");
        return cargoService.updateCargoInfo(id, freightFare, startingPoint);
    }




    /**
     * 查询各方的所有订单
     *
     * @param id
     * @return
     */
  //  @MyLog(value = "查询各方的所有订单")
    @GetMapping("/{part}/{id}")
    public List<Cargo> getTruckAllCargos(@PathVariable("part") String who, @PathVariable("id") int id) {
        if (who.equals("trucks")) {
            int truckId = id;
            logger.info("REST 查询发货方{}所有订单", truckId);
            return cargoService.findAllByTruckId(truckId);
        } else if (who.equals("shippers")) {
            int shipperId = id;
            logger.info("REST 查询发货方{}所有订单", shipperId);
            return cargoService.findAllByShipperId(shipperId);
        } else {
            int receiverId = id;
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
   // @MyLog(value = "查询不同状态的货物")
    @GetMapping("/cargo-status/{status}")
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
        return cargoRepository.findAllByStatus(status);
    }


    /**
     * 货物状态更新13
     *
     * @return
     */
    @MyLog(value = "货物状态更新13")
    @PutMapping("/cargo-status-13/{cargoId}")
    public Cargo statusChangeTo13(@PathVariable int cargoId) {
        return cargoService.statusChangeTo13(cargoId);
    }


    /**
     * 货物状态改为14
     *
     * @return
     */
    @MyLog(value = "货物状态更新14")
    @PutMapping("/cargo-status-14/{cargoId}")
    public Cargo statusChangeTo14(@PathVariable int cargoId) {
        return cargoService.statusChangeTo14(cargoId);
    }


    /**
     * 货物状态改为15
     *
     * @return
     */
    @MyLog(value = "拒单")
    @PutMapping("/cargo-status-15/{cargoId}")
    public Cargo statusChangeTo15(@PathVariable int cargoId) {
        return cargoService.statusChangeTo15(cargoId);
    }



    /**
     * 查找转单中的上一个订单
     * @param preCargoId
     * @return
     */
    @MyLog(value = "上个订单")
    @GetMapping("/transferred-cargos/{preCargoId}")
    public List<Cargo> getAllTransCargos(@PathVariable int preCargoId) {
        logger.info("REST 查询订单转运历史");
        return cargoService.findAllByPreCargoId(preCargoId);
    }


    /**
     * 更新承运方/货物位置信息
     *
     * @return
     */
    @MyLog(value = "更新承运方/货物位置信息")
    @PutMapping("/position/{truckId}/{position}")
    public List<Cargo> refreshPosition(@RequestParam("truckId") int truckId, @RequestParam("position") String position) {
        logger.info("更新承运方/货物位置信息");
        return cargoService.refreshPosition(truckId, position);
    }


    /**
     * 更新完成度
     *
     * @return
     */
    @MyLog(value = "更新完成度")
    @PutMapping("/complete-ratio/{cargoId}/{ratio}")
    public Cargo refreshPosition(@RequestParam("cargoId") int cargoId, @RequestParam("ratio") Double ratio) {
        logger.info("更新承运方/货物位置信息");
        return cargoService.refreshCompleteRatio(cargoId, ratio);
    }



    @MyLog(value = "追踪转单历史")
    @GetMapping("/history/{id}")
    public Stack<Cargo> getTransCargoHistory(@PathVariable("id") int id){
        return cargoService.getTransCargoHistory(id);
    }

    @MyLog(value = "挂起恢复")
    @PutMapping("/recovery/{cargoId}/{status}")
    public Cargo recovery(@PathVariable("cargoId") int cargoId, @PathVariable("status") int status){
        return cargoService.recovery(cargoId,status);
    }



}
