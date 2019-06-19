package cn.tycoding.resource;

import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Platform;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.PlatformRepository;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.service.CargoService;
import cn.tycoding.service.InspectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-06-17 20:41
 * 4
 */

@RestController
@RequestMapping("/platform")
public class PlatformResource {


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
    private cn.tycoding.service.PlatformService platformService;
    @Autowired
    private PlatformRepository platformRepository;

    /**参数设置
     * @param platform
     * @return
     */
    @PostMapping("/setPlatformPara")
    public Platform setPlatformElem(@RequestBody Platform platform) {
        String result = "参数设置";
        platformService.savePlatform(platform);
        logger.info(result);
        return platformService.savePlatform(platform);
    }



    /**平台属性展示
     * @param
     * @return
     */
    @GetMapping("/showPlatformPara")
    public Platform showPlatformPara() {
        String result = "平台属性展示";

        logger.info(result);
        return platformService.showPlatformPara();
    }


    /**平台信息一览
     * @param
     * @return
     */
    @GetMapping("/showPlatformList")
    public String showPlatformList() {
        String result = "平台信息一览";
        logger.info(result);
        return platformService.showPlatformList();
    }






}
