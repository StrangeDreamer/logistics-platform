package cn.tycoding.service;
import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShipperService {

    private final Logger logger=LoggerFactory.getLogger(ShipperService.class);
    private final ShipperRepository shipperRepository;
    private final CargoRepository cargoRepository;
    @Autowired
    private BankAccountService bankAccountService;

    public ShipperService(ShipperRepository shipperRepository,CargoRepository cargoRepository) {
        this.shipperRepository = shipperRepository;
        this.cargoRepository = cargoRepository;
    }

    // 登录
    public Shipper login(String name){
        // 是否查无此人
        return shipperRepository.findShipperByName(name).orElseThrow(()->new ShipperException("该用户未注册！"));
    }

    //发货方注册
    @Transactional
    public Shipper createShipper(Shipper shipper){

        // 先检查该注册人的身份证是否已经用于该项的注册
        if(shipperRepository.existsShipperByIdgerenshenfenzheng(shipper.getIdgerenshenfenzheng())) {
            throw new TruckException("该个人身份证已经被用于注册发货方！");
        }

        // 检查用户名是否已经被占用
        if(shipperRepository.existsShipperByName(shipper.getName())) {
            throw new ShipperException("该用户名已被占用！");
        }
        Shipper shipper1 = new Shipper();
        shipper1.setName(shipper.getName());
        shipper1.setBankId(shipper.getBankId());

        shipper1.setIdgerenshenfenzheng(shipper.getIdgerenshenfenzheng());
        shipper1.setId_gongsitongyidaima(shipper.getId_gongsitongyidaima());
        shipper1.setOccupation(shipper.getOccupation());
        shipper1.setTelNumber(shipper.getTelNumber());
        shipper1.setAddress(shipper.getAddress());
        shipperRepository.save(shipper1);

        // 获得银行账号和保险
        bankAccountService.check(shipper1.getId(),"shipper");
        return shipper1;
    }

    //发货方注销
    public String deleteShipper(int id){
        shipperRepository.findById(id).orElseThrow(()->new ShipperException("该发货方不存在"));

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


    public Shipper setShipperRank(int id, double rank) {
        Shipper shipper = shipperRepository.findById(id).orElseThrow(()->new ShipperException("该发货方不存在"));
        shipper.setRanking(rank);
        return shipper;
    }

    public Shipper active(int id) {
        Shipper shipper = shipperRepository.findById(id).orElseThrow(()->new ShipperException("该发货方不存在"));
        shipper.setActivated(true);
        shipperRepository.save(shipper);
        logger.info("激活成功！");
        return shipper;
    }
}
