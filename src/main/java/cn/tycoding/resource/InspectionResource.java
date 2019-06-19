package cn.tycoding.resource;
import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Cargo;
import cn.tycoding.exception.BidException;
import cn.tycoding.exception.InspectionException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.InspectionService;
import cn.tycoding.service.CargoService;
import cn.tycoding.service.PlatformService;
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
    @Autowired
    private PlatformService platformService;


    /**验货
     * @param inspection
     * @return
     */
    @PostMapping
    public String inspectionCargo(@RequestBody Inspection inspection) {

        return  inspectionService.inspectionCargo(inspection);

    }
}
