package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.ReceiverRepository;
import cn.tycoding.repository.ShipperRepository;
import cn.tycoding.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CargoService {
    private final Logger logger = LoggerFactory.getLogger(CargoService.class);

    private final CargoRepository cargoRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    private final String cargoKey = "Cargo";
    private final String truckKey = "Truck";
    private final String shipperKey = "Shipper";
    private final String receiverKey = "Receiver";

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
        c.setType(cargo.getType());
        c.setLimitedTime(cargo.getLimitedTime());
        c.setDeparture(cargo.getDeparture());
        c.setDestination(cargo.getDestination());
        c.setBidStartTime(cargo.getBidStartTime());
        c.setBidEndTime(cargo.getBidEndTime());
        c.setAbnormal(false);
        c.setOvertime(false);
        c.setStatus(0);
        c.setOrderPrice(-1);
        c.setTruckId(-1);

        cargoRepository.save(c);
        logger.info("A new Cargo is created !");
        return c;
    }



    // 撤单
    public void deleteCargo(int id) {

        cargoRepository.findById(id).ifPresent(cargo -> {
            cargoRepository.delete(cargo);
            logger.info("货物撤单成功！");
        });
    }




    public Cargo updateCargoInfo(int id, CargoInfoChangeDTO cargoInfoChangeDTO) {
        Cargo cargo=cargoRepository.findById(id).orElseThrow(()->new CargoException("this cargo is not exist !!!"));
        cargo.setReceiverId(cargoInfoChangeDTO.getReceiverId());
        cargo.setFreightFare(cargoInfoChangeDTO.getFreightFare());
        cargo.setBidStartTime(cargoInfoChangeDTO.getBidStartTime());
        cargo.setBidEndTime(cargoInfoChangeDTO.getBidEndTime());
        cargoRepository.save(cargo);
        logger.info("Cargo information is updated !");
        return cargo;
    }

    public List<Cargo> findAllCargos() {

        List<Cargo> cargosList = redisTemplate.boundHashOps(cargoKey).values();
        if (cargosList == null || cargosList.size() == 0) {
            //说明缓存中没有秒杀列表数据
            //查询数据库中秒杀列表数据，并将列表数据循环放入redis缓存中
            cargosList = cargoRepository.findAll();
            for (Cargo cargo : cargosList) {
                //将秒杀列表数据依次放入redis缓存中，key:秒杀表的ID值；value:秒杀商品数据
                redisTemplate.boundHashOps(cargoKey).put(cargo.getId(),cargo);
                logger.info("findAll -> 从Mysql数据库中读取并放入Redis缓存中");
            }
            cargosList = redisTemplate.boundHashOps(cargoKey).values();
        } else {
            logger.info("findAll -> 从Redis缓存中读取");
        }
        return cargosList;
    }

    public Cargo findCargoById(int id){
        Cargo cargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(id);
        //redis中没有缓存该运单
        if (cargo==null){
            Cargo cargoDb=cargoRepository.findById(id).orElseThrow(()->new CargoException("this cargo is not exist!"));
            redisTemplate.boundHashOps(cargoKey).put(id, cargoDb);
            logger.info("RedisTemplate -> 从数据库中读取并放入缓存中");
            cargo= (Cargo) redisTemplate.boundHashOps(cargoKey).get(id);
        }
        logger.info("*****************"+cargo.getBidStartTime()+"**************************************");

        return cargo;
    }




    // 查找发货方的所有订单
    public List<Cargo> findAllByShipperId(int shipperId) {
        return cargoRepository.findAllByShipperId(shipperId);
    }


    // 查找收货方的所有订单
    public List<Cargo> findAllByReceiverId(int receiverId) {
        return cargoRepository.findAllByReceiverId(receiverId);
    }

    // 查找承运方的所有订单
  /*  public List<Cargo> findAllByTruckId(int truckId) {
        return cargoRepository.findAllByTruckId(truckId);
    }*/



//    // 查找承运方方的所有订单  TODO：承运方查找的是order而不是cargo
//    public List<Cargo> findAllByShipperId(int shipperId) {
//        return cargoRepository.findAllByShipperId(shipperId);
//    }





}
