package cn.tycoding.resource;

import cn.tycoding.domain.Inspection;
import cn.tycoding.domain.Receiver;
import cn.tycoding.service.InspectionService;
import cn.tycoding.service.ReceiverService;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/receivers")

public class ReceiverResource {
    private final Logger logger=LoggerFactory.getLogger(ReceiverResource.class);
    private final ReceiverService receiverService;

    @Autowired
    private InspectionService inspectionService;

    public ReceiverResource(ReceiverService receiverService) {
        this.receiverService = receiverService;
    }


    /**
     * 登录
     * @return
     */
    @GetMapping("/login/{name}")
    public Receiver login(@RequestParam("name") String name){
        logger.info("登录");
        return receiverService.login(name);
    }


    @PostMapping
    public Receiver createReceiver(@RequestBody Receiver receiver){
        logger.info("Rest 收货方注册请求");
        return receiverService.createReceiver(receiver);
    }

    @DeleteMapping("/{id}")
    public String deleteReceiver(@PathVariable("id") int id){
        logger.info("Rest 收货方注销请求");
        return receiverService.deleteReceiver(id);
    }

    /**
     * 查询指定收货方
     * @return
     */
    @GetMapping("/{id}")
    public Receiver getReceiversById(@PathVariable("id") int id){
        logger.info("REST 查询所指定收货方");
        return receiverService.findReceiverById(id);
    }

    /**
     * 查询所有发货方
     * @return
     */
    @GetMapping
    public List<Receiver> getAllReceiverr(){
        logger.info("REST 查询所有收货方");
        return receiverService.findAll();
    }

    /**
     * 验货
     * @param inspection
     * @return
     */
    @PostMapping("/inspections")
    public String inspectionCargo(@RequestBody Inspection inspection) {
        return  inspectionService.inspectionCargo(inspection);
    }


    @PutMapping("/active/{id}")
    public Receiver active(@PathVariable("id") int id) {
        logger.info("激活用户");
        return receiverService.active(id);
    }

}


