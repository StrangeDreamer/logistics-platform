package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.Truck;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.service.TruckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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


    /**
     * 登录
     * @return
     */
    @GetMapping("/login/{name}")
    public Truck login(@RequestParam("name") String name){
        logger.info("登录");
        return truckService.login(name);
    }

    /**
     * 注册
     * @param truck
     * @return
     */
    @PostMapping
    public Truck createTruck(@RequestBody Truck truck){
        return truckService.createTruck(truck);

    }

    /**
     * 注销
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    // 1.如果注册承运⽅方 有正在执⾏行行的订单，则提示⽤用户该订单并拒绝注销。
    // 2.如果承运⽅方仍然有责任纠纷未解决，则提示⽤用户该问题并拒绝注销。

    public String deleteTruck(@PathVariable("id") int id){

        logger.info("Rest 承运方注销请求");
        return truckService.deleteTruck(id);

    }

    /**
     * 查询所有承运方
     * @return
     */
    @GetMapping
    public List<Truck> getAllTruck(){
        logger.info("REST 查询所有货物");
        return truckService.findAll();
    }

    /**
     * 查询指定承运方
     * @return
     */
    @GetMapping("/{id}")
    public Truck findTruckById(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return truckService.findTruckById(id);
    }

    @GetMapping("/cargoinfo/{id}")
    public String findTrucksCargoNum(@PathVariable("id") int id){
        logger.info("REST 查询所有货物");
        return truckService.findTrucksCargoNum(id);
    }

    /**
     * truck 请求开始运货
     * @param cargoId 货号
     * @return
     */
    @PutMapping("/departure/{cargoId}")
    public Cargo startShip(@PathVariable int cargoId){

        logger.info("truck开始运货，货单号{}",cargoId);
        return truckService.startShip(cargoId);
    }

    /**
     * truck 结束运货,请求验货
     * @param cargoId
     * @return
     */
    @PutMapping("/destination/{cargoId}")
    public Cargo endShip(@PathVariable int cargoId){
        logger.info("truck已经送达，请求验货");
        return truckService.endShip(cargoId);
    }

    /**
     * 设置承运方评级
     *
     * @return
     */

    @PutMapping("/setTruckRank/{truckId}/{rank}")
    public Truck setTruckRank(@PathVariable("truckId") int truckId, @PathVariable("rank") double rank) {
        logger.info("设置承运方评级");
        return truckService.setTruckRank(truckId, rank);
    }

    @PutMapping("/active/{id}")
    public Truck active(@PathVariable("id") int id) {
        logger.info("激活用户");
        return truckService.active(id);
    }

    @PostMapping("/upload")
    //上传的文件会转换成MultipartFile对象，file名字对应html中上传控件的name
    public String test(MultipartFile file) throws IllegalStateException, IOException {
        //transferTo是保存文件，参数就是要保存到的目录和名字
        //windows格式
        //file.transferTo(new File("e:\\image\\"+file.getOriginalFilename()));
        //Linux格式
        file.transferTo(new File("/home/wangjin/uploadFiles/"+file.getOriginalFilename()));
        return "上传完毕";
    }
}
