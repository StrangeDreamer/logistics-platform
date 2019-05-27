package cn.tycoding.service;


import cn.tycoding.domain.Shipper;
import cn.tycoding.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ShipperService {

    private final Logger logger=LoggerFactory.getLogger(ShipperService.class);
    private final ShipperRepository shipperRepository;

    public ShipperService(ShipperRepository shipperRepository) {
        this.shipperRepository = shipperRepository;
    }


    //发货方注册
    public Shipper createShipper(Shipper shipper){
        Shipper shipper1=new Shipper();
        shipper1.setName(shipper.getName());
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
}
