package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ShipperService {

    private final Logger logger=LoggerFactory.getLogger(ShipperService.class);
    private final ShipperRepository shipperRepository;
    private final CargoRepository cargoRepository;

    public ShipperService(ShipperRepository shipperRepository,CargoRepository cargoRepository) {
        this.shipperRepository = shipperRepository;
        this.cargoRepository = cargoRepository;
    }


    //发货方注册
    public Shipper createShipper(Shipper shipper){
        Shipper shipper1 = new Shipper();
        shipper1.setName(shipper.getName());
        shipper1.setBankId(shipper.getBankId());
        shipperRepository.save(shipper1);
        return shipper1;
    }

    //发货方注销
    public String deleteShipper(int id){
        List<Cargo> list = cargoRepository.findAllByShipperId(id);
        // 1.如果该发货⽅方有尚未完成的订单，返回订单提醒⽤用户并拒绝注销。
        for (Cargo cargo:list) {
            if (cargo.getStatus() < 6) {
                throw new ShipperException("注销失败！当前发货方还有订单未完成订单！");
            }
            if (cargo.getStatus() == 10) {
                throw new ShipperException("注销失败！当前发货方存在异常订单！");
            }
        }
        shipperRepository.findById(id).ifPresent(shipper -> {
            shipperRepository.delete(shipper);
            logger.info("发货发注销成功！");

        });
        return  "发货方" + id + "注销成功！";
    }

    // 查询指定id发货方
    public Shipper findShipperById(int shipperId){
        Shipper shipper = shipperRepository.findById(shipperId).orElseThrow(()->new ShipperException("该发货方不存在"));
        return shipper;
    }

    // 查询所有发货方
    public List<Shipper> findAll(){
        return shipperRepository.findAll();
    }


}
