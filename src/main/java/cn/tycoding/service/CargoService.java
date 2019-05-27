package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.repository.CargoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CargoService  {
    private final Logger logger=LoggerFactory.getLogger(CargoService.class);


    private final CargoRepository cargoRepository;

    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    // TODO 一些逻辑判断
    public Cargo createCargo(Cargo cargo){
        Cargo c=new Cargo();
        c.setShipperId(cargo.getShipperId());
        c.setRecieverId(cargo.getRecieverId());
        c.setStartTime(cargo.getStartTime());
        c.setPrice(cargo.getPrice());
        cargoRepository.save(c);
        logger.info("成功创建一个订单");
        return c;

    }

}
