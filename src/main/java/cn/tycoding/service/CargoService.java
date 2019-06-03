package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
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
        c.setStartTime(cargo.getStartTime());
        c.setEndTime(cargo.getEndTime());
        cargoRepository.save(c);
        logger.info("A new Cargo is created !");
        return c;
    }


    // 撤单
    public void deleteCargo(int id) {
        cargoRepository.findById(id).ifPresent(shipper -> {
            cargoRepository.delete(shipper);
            logger.info("发货发注销成功！");
        });
    }




    public Cargo updateCargoInfo(int id, CargoInfoChangeDTO cargoInfoChangeDTO) {
        Cargo cargo=cargoRepository.findById(id).orElseThrow(()->new CargoException("this cargo is not exist !!!"));
        cargo.setReceiverId(cargoInfoChangeDTO.getReceiverId());
        cargo.setFreightFare(cargoInfoChangeDTO.getFreightFare());
        cargo.setStartTime(cargoInfoChangeDTO.getStartTime());
        cargo.setEndTime(cargoInfoChangeDTO.getEndTime());
        cargoRepository.save(cargo);
        logger.info("Cargo information is updated !");
        return cargo;
    }

}
