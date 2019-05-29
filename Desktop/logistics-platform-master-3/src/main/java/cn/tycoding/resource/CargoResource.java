package cn.tycoding.resource;

import cn.tycoding.domain.Cargo;
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
     * @param cargo
     * @return
     */
    @PostMapping()
    public Cargo createCargo(@RequestBody Cargo cargo) {

        return cargoService.createCargo(cargo);
    }



    @GetMapping("/{id}")
    public Cargo getCargo(@PathVariable("id") int id)
    {
        logger.info("REST 获得订单详情");
       return cargoRepository.findById(id).get();
    }


    @GetMapping("/shippers/{shipperId}")
    public List<Cargo> getShipperAllCargos(@PathVariable int shipperId){
        logger.info("REST 查询发货方{}所有订单"+shipperId);
        return cargoRepository.findAllByShipperId(shipperId);
    }


    @GetMapping()
    public List<Cargo> getAllCargos(){
        logger.info("REST 查询所有订单");
        return cargoRepository.findAll();
    }


}
