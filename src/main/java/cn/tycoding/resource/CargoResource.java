package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Platform;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.TransferredCargo;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.service.CargoService;
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

    private final Logger logger=LoggerFactory.getLogger(CargoResource.class);

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;
    private final PlatformRepository platformRepository;
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;

    public CargoResource(CargoService cargoService, CargoRepository cargoRepository,PlatformRepository platformRepository) {
        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
        this.platformRepository = platformRepository;
    }


    /**
     * 提交订单
     *
     * 使用@RequestParam时，URL是这样的：http://host:port/path?参数名=参数值
     *
     * 使用@PathVariable时，URL是这样的：http://host:port/path/参数值
     * @param cargo
     * @return
     */
    @PostMapping("/createCargo")
    public Cargo createCargo(@RequestBody Cargo cargo) {
        // TODO：发货方资金检查，资金充足才可以发货
        logger.info("发货方资金检查，资金充足才可以发货");

        Platform platform = platformRepository.findRecentPltf();

        logger.info("发货方冻结资金" + cargo.getFreightFare());
        double exhibitionFee = platform.getExhibitionFee();
        logger.info("发货方支付展位费" + exhibitionFee);
        return cargoService.createCargo(cargo);
    }



    /**
     * 查询指定ID订单
     * @return Cargo
     */
    @GetMapping("/findCargoById/{cargoId}")
    public Cargo getCargoById(@PathVariable("cargoId") int cargoId){
        logger.info("REST 查询所有货物");
        return cargoService.findCargoById(cargoId);
    }



    /** TODO 每个订单只能开启一次，在set之前需要判断之前是否已经set过或者是否为空（简单点）
     * 平台确定某运单开抢时间和结束时间默认2分钟
     * @return
     */
    @PutMapping("/startBidTime/{cargoId}")
    public Cargo startCargo(@PathVariable int cargoId){
        Cargo cargo=cargoService.findCargoById(cargoId);
        Date bidStartTime = new Date();
        Date bidEndTime = new Date(bidStartTime.getTime() + 60000);
        //先更新DB，再删除cache
        cargo.setBidStartTime(bidStartTime);
        cargo.setBidEndTime(bidEndTime);
        cargo.setStatus(1);
        cargoRepository.save(cargo);
        //删除map中的某个对象
        redisTemplate.boundHashOps(cargoKey).delete(cargoId);
        logger.info("订单{}*****{}开始抢",cargoId,bidStartTime);
        return cargo;
    }

    /**
    * 撤单
    *
    * @param id
    * @return
    */
    @DeleteMapping("/withdrawalCargo/{id}")
    public Cargo withdrawalCargo(@PathVariable("id") int id){
        logger.info("Rest 撤单请求{}"+id);
        return cargoService.withdrawalCargo(id);
    }


    /**
     * 转单（更新订单）
     * @param id
     * @return
     */

    @PutMapping("/{cargoId}/{freightFare}")
    public Cargo getCargo(@PathVariable("cargoId") int id, @PathVariable("freightFare") double freightFare)
    {
        logger.info("REST 转单");
        //return cargoService.updateCargoInfo(id,cargoInfoChangeDTO);
        return  cargoService.updateCargoInfo(id,freightFare);
    }


    /**
     * 查询承运方的所有订单
     * @param truckId
     * @return
     */
    @GetMapping("/findAllByTruckId/{truckId}")
    public List<Cargo> getTruckAllCargos(@PathVariable("truckId") int truckId){
        logger.info("REST 查询发货方{}所有订单",truckId);
        return cargoService.findAllByTruckId(truckId);
    }


    /**
     * 查询发货方的所有订单
     * @param shipperId
     * @return
     */

    @GetMapping("/findAllByShipperId/{shipperId}")
    public List<Cargo> getShipperAllCargos(@PathVariable int shipperId){
        logger.info("REST 查询发货方{}所有订单",shipperId);
        return cargoService.findAllByShipperId(shipperId);
    }


    /**
     * 查询收货方的所有订单
     * @param receiverId
     * @return
     */
    @GetMapping("/findAllByReceiverId/{receiverId}")
    public List<Cargo> getReceiverAllCargos(@PathVariable("receiverId") int receiverId){
        logger.info("REST 查询发货方{}所有订单",receiverId);
        return cargoService.findAllByReceiverId(receiverId);
    }



    /**
     * 查询所有货物
     * @return
     */
    @GetMapping("/allCargos")
    public List<Cargo> getAllCargos(){
        logger.info("REST 查询所有货物");
        return cargoService.findAllCargos();
    }




    /**
     * 查询所有正常完成货物
     * @return
     */
    @GetMapping("/allNormalCargos")
    public List<Cargo> getAllNormalCargos(){
        logger.info("REST 查询所有货物");
        return cargoService.findAllNormalCargos();
    }


    /**
     * 查询所有超时完成的货物
     * @return
     */
    @GetMapping("/allTimeOutCargos")
    public List<Cargo> getAllTimeOutCargos(){
        logger.info("REST 查询所有超时完成的货物");
        return cargoService.findAllTimeOutCargos();
    }


    /**
     * 查询所有订单异常的货物
     * @return
     */
    @GetMapping("/allAbnormalCargos")
    public List<Cargo> getAllAbnormalCargos(){
        logger.info("REST 查询所有订单异常的货物");
        return cargoService.findAbnormalCargos();
    }



    @GetMapping("/allTransCargos/{originCargoId}")
    public List<Cargo> getAllTransCargos(@PathVariable int originCargoId){
        logger.info("REST 查询订单转运历史");
        return cargoService.findAllByOriginCargoId(originCargoId);
    }


}
