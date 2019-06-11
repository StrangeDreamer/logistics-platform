package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.TransferredCargo;
import cn.tycoding.domain.Truck;
import cn.tycoding.dto.CargoInfoChangeDTO;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.*;
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
    @Autowired
    private  CargoRepository cargoRepository;
    @Autowired
    private TransferredCargoRepo transferredCargoRepo;

    @Autowired
    private RedisTemplate redisTemplate;

    private final String cargoKey = "Cargo";
    private final String truckKey = "Truck";
    private final String shipperKey = "Shipper";
    private final String receiverKey = "Receiver";




    // TODO 能否成功创建该订单
    public Cargo createCargo(Cargo cargo) {
        Cargo c = new Cargo();

        c.setShipperId(cargo.getShipperId());
        c.setFreightFare(cargo.getFreightFare());
        c.setOriginFare(cargo.getFreightFare());
        c.setReceiverId(cargo.getReceiverId());
        c.setWeight(cargo.getWeight());
        c.setVolume(cargo.getVolume());
        c.setType(cargo.getType());
        c.setLimitedTime(cargo.getLimitedTime());
        c.setDeparture(cargo.getDeparture());
        c.setDestination(cargo.getDestination());

        cargoRepository.save(c);
        logger.info("A new Cargo is created !");
        return c;
    }


    /**
     * 撤单
     * @param id
     */
    public void deleteCargo(int id) {
        cargoRepository.findById(id).ifPresent(cargo -> {
            cargoRepository.delete(cargo);
            logger.info("货物撤单成功！");
        });
    }


    /**
     * TODO 转单业务逻辑实现
     * @param cargoId
     * @param freightFare
     * @return
     */
    public Cargo updateCargoInfo(int cargoId, double freightFare) {

        Cargo cargo=cargoRepository.findById(cargoId).orElseThrow(()->new CargoException("this cargo is not exist !!!"));

//        TransferredCargo transferredCargo=new TransferredCargo();
//        transferredCargo.setCargoId(cargo.getId());
//        transferredCargo.setFreightFare(freightFare);
//        transferredCargoRepo.save(transferredCargo);
        Cargo transferredCargo=new Cargo();
        transferredCargo.setLimitedTime(cargo.getLimitedTime());
        transferredCargo.setType(cargo.getType());
        transferredCargo.setFreightFare(freightFare);
        transferredCargo.setReceiverId(cargo.getReceiverId());
        transferredCargo.setDeparture(cargo.getDeparture());
        transferredCargo.setDestination(cargo.getDestination());

        transferredCargo.setOriginFare(cargo.getFreightFare());

        transferredCargo.setShipperId(cargo.getShipperId());
        transferredCargo.setVolume(cargo.getVolume());
        transferredCargo.setWeight(cargo.getWeight());
        cargoRepository.save(transferredCargo);

        logger.info("转单创建成功！");
        return transferredCargo;
    }



  public List<Cargo> findAllCargos() {
//
//        List<Cargo> cargosList = redisTemplate.boundHashOps(cargoKey).values();
//        if (cargosList == null || cargosList.size() == 0) {
//            //说明缓存中没有秒杀列表数据
//            //查询数据库中秒杀列表数据，并将列表数据循环放入redis缓存中
//            cargosList = cargoRepository.findAll();
//            for (Cargo cargo : cargosList) {
//                //将秒杀列表数据依次放入redis缓存中，key:秒杀表的ID值；value:秒杀商品数据
//                redisTemplate.boundHashOps(cargoKey).put(cargo.getId(),cargo);
//                logger.info("findAll -> 从Mysql数据库中读取并放入Redis缓存中");
//            }
//            cargosList = redisTemplate.boundHashOps(cargoKey).values();
//        } else {
//            logger.info("findAll -> 从Redis缓存中读取");
//        }

        return cargoRepository.findAll();
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
    public List<Cargo> findAllByTruckId(int truckId) {
        return cargoRepository.findAllByTruckId(truckId);
    }



//    // 查找承运方方的所有订单  TODO：承运方查找的是order而不是cargo
//    public List<Cargo> findAllByShipperId(int shipperId) {
//        return cargoRepository.findAllByShipperId(shipperId);
//    }





}
