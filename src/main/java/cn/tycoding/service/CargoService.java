package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.repository.CargoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CargoService {
    private final Logger logger = LoggerFactory.getLogger(CargoService.class);

    private final CargoRepository cargoRepository;

    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }


    // TODO 一些逻辑判断
    public Cargo createCargo(Cargo cargo) {
        Cargo c = new Cargo();

        c.setShipperId(cargo.getShipperId());
        c.setFreightFare(cargo.getFreightFare());
        c.setReceiverId(cargo.getReceiverId());
        c.setWeight(cargo.getWeight());
        c.setVolume(cargo.getVolume());
        c.setDeparture(cargo.getDeparture());
        c.setDestination(cargo.getDestination());

        cargoRepository.save(c);
        logger.info("成功创建一个货物");
        return c;
    }
}
