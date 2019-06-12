package cn.tycoding.resource;
import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Cargo;
import cn.tycoding.exception.BidException;
import cn.tycoding.exception.InspectionException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.InspectionService;
import cn.tycoding.service.CargoService;
import cn.tycoding.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther qlXie
 * @date 2019-06-12 10:56
 */
@RestController
@RequestMapping("/inspections")
public class InspectionResource {

    private final Logger logger= LoggerFactory.getLogger(InspectionResource.class);

    //设置秒杀redis缓存的key
    private final String inspectionsKey = "inspections";
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private InspectionService inspectionService;
    /**验货
     * @param inspection
     * @return
     */
    @PostMapping
    public String inspectionCargo(@RequestBody Inspection inspection) {
        String result = "验货结果：";

        //获取系统时间
//        Date nowTime = new Date();
//        Cargo cargo=cargoService.findCargoById(inspection.getCargoId());
//
//        if (nowTime.getTime()>cargo.getInspectionEndTime().getTime()){
//            logger.info("错过抢单时间");
//            throw new InspectionException("错过抢单截止时间："+cargo.getInspectionEndTime());
//        }
//        if (nowTime.getTime()<cargo.getInspectionStartTime().getTime()){
//            logger.info("还未开抢");
//            throw new InspectionException("还未开抢，开抢时间："+cargo.getInspectionStartTime());
//        }



//     * 6 订单异常 --确定收单时
//     * 7 订单完结 --确定收单时
//     * 10 订单超时 --确定收单时
        Cargo cargo = cargoRepository.findCargoById(inspection.getCargoId());
        if (inspection.getInspectionResult() == 6) {
            result = "验货通过，订单正常完结！\n" +
                    "发货方向平台支付运费" + cargo.getFreightFare() +
                    "平台向承运方支付酬劳" + cargo.getOrderPrice() +
                    "平台向发货方和承运方支付分享利润 x，y\n" +
                    "承运方" + cargo.getTruckId() + "的担保额度恢复" + cargo.getInsurance();
        } else if (inspection.getInspectionResult() == 10) {
            result = "验货正常但出现超时！\n" +
                    "超时时长为：" + inspection.getTimeoutPeriod() + "承运方需要支付赔偿金： N" +
                    "发货方向平台支付运费" + cargo.getFreightFare() +
                    "平台向承运方支付酬劳" + cargo.getOrderPrice() +
                    "平台向发货方和承运方支付分享利润 x，y\n" +
                    "承运方" + cargo.getTruckId() + "的担保额度恢复仅当超时赔偿金支付完成后恢复！" ;
        } else if (inspection.getInspectionResult() == 7) {
            result = "货物出现异常，交给第三方处理";
        } else {
            throw new InspectionException("验货结果设置错误！");
        }

        logger.info(result);
        return result;
    }




}
