package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Shipper;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.service.CargoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cargos")
public class CargoResource {

    private final Logger logger=LoggerFactory.getLogger(CargoResource.class);

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;

    public CargoResource(CargoService cargoService, CargoRepository cargoRepository) {
        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
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

        return cargoService.createCargo(cargo);
    }


    /**
    * 撤单
    *
    * 使用@RequestParam时，URL是这样的：http://host:port/path?参数名=参数值
    *
    * 使用@PathVariable时，URL是这样的：http://host:port/path/参数值
    * @param id
    * @return
    */


    @DeleteMapping("/deleteCargo/{id}")
    public String deleteCargo(@PathVariable("id") int id){
        logger.info("Rest 发货方注销请求");
        cargoService.deleteCargo(id);
        return "删除shipper"+id+"成功";
    }


    /**
     * 更新订单
     * @param id
     * @return
     */

    @PutMapping("/{cargoId}")
    public Cargo getCargo(@PathVariable("cargoId") int id, @RequestBody CargoInfoChangeDTO cargoInfoChangeDTO)
    {
        logger.info("REST 更新订单");
        return cargoService.updateCargoInfo(id,cargoInfoChangeDTO);
    }



    /**
     * 查询发货方的所有订单
     * @param shipperId
     * @return
     */

    @GetMapping("/shippers/{shipperId}")
    public List<Cargo> getShipperAllCargos(@PathVariable int shipperId){
        logger.info("REST 查询发货方{}所有订单"+shipperId);
        return cargoService.findAllByShipperId(shipperId);
    }


    /**
     * 查询收货方的所有订单
     * @param receiverId
     * @return
     */
    @GetMapping("/receivers/{receiverId}")
    public List<Cargo> getReceiverAllCargos(@PathVariable("receiverId") int receiverId){
        logger.info("REST 查询发货方{}所有订单"+receiverId);
        return cargoService.findAllByReceiverId(receiverId);
    }

    /**
     * 查询承运方的所有订单
     * @param truckId
     * @return
     */
    @GetMapping("/trucks/{truckId}")
    public List<Cargo> getTruckAllCargos(@PathVariable("truckId") int truckId){
        logger.info("REST 查询发货方{}所有订单"+truckId);
        return cargoService.findAllByReceiverId(truckId);
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




}
