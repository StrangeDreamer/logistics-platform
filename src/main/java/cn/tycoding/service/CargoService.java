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
    private CargoService cargoService;

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
        c.setInsurance(cargo.getInsurance());
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
  /*  public void deleteCargo(int id) {
        cargoRepository.findById(id).ifPresent(cargo -> {
            cargoRepository.delete(cargo);
            logger.info("货物撤单成功！");
        });
    }*/

    public Cargo withdrawalCargo(int id) {
        Cargo cargo = cargoService.findCargoById(id);
        try {
            //8 发布时无人接单撤单  --撤单
            if (cargo.getStatus() == 0 || cargo.getStatus() ==  1){
                cargo.setStatus(8);
                logger.info("由于订单未被接单，直接撤单");
            }
            //已接未运撤单  --撤单
            else if (cargo.getStatus() == 2){
                // 资金流动 TODO： 发货方向平台支付赔偿，平台将赔偿付给车辆。赔偿金的计算。
                logger.info("由于货物已接未运，发货方" + cargo.getShipperId() +
                        " 支付给承运方" + cargo.getTruckId() + "赔偿，" +
                        "赔偿为运费" + cargo.getFreightFare() + " 乘 撤单赔偿比例");
                // 车辆的担保额度的恢复
                logger.info("车辆" +cargo.getTruckId() + "的担保额度恢复" + cargo.getInsurance());
                cargo.setStatus(9);
                // TODO：更进一步的判断，撤单前判断发货方是否有足够的资金进行撤单。或者放在客户端
            }
            else {
                logger.info("订单当前状态不允许撤单" );
            }
            cargoRepository.save(cargo);
            return cargo;
        }catch (Exception e){
            e.printStackTrace();
        }
        return cargo;
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
        transferredCargo.setInsurance(cargo.getInsurance());

        //转单更新
        transferredCargo.setOriginFare(cargo.getFreightFare());

        if (cargo.getOriginCargoId()==-1){
            transferredCargo.setOriginCargoId(cargo.getId());
        }else {
            transferredCargo.setOriginCargoId(cargo.getOriginCargoId());
        }

        transferredCargo.setShipperId(cargo.getShipperId());
        transferredCargo.setVolume(cargo.getVolume());
        transferredCargo.setWeight(cargo.getWeight());
        transferredCargo.setStatus(5);
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
        if (cargo == null){
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


    //查询订单流通历史
    public List<Cargo> findAllByOriginCargoId(int originCargoId){
        return cargoRepository.findAllByOriginCargoId(originCargoId);
    }


}
