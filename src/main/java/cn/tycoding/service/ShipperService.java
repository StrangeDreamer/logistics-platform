package cn.tycoding.service;


import cn.tycoding.domain.Shipper;
import cn.tycoding.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ShipperService {

    private final Logger logger=LoggerFactory.getLogger(ShipperService.class);
    private final ShipperRepository shipperRepository;

    public ShipperService(ShipperRepository shipperRepository) {
        this.shipperRepository = shipperRepository;
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
    public void deleteShipper(int id){
        shipperRepository.findById(id).ifPresent(shipper -> {
            shipperRepository.delete(shipper);
            logger.info("发货发注销成功！");
        });
    }

    // 查询指定id发货方
    public Shipper findShippersById(int shipperId){
        return shipperRepository.findShippersById(shipperId);
    }

    // 查询所有发货方
    public List<Shipper> findAll(){
        return shipperRepository.findAll();
    }


}
