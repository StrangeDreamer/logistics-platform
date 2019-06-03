package cn.tycoding.resource;

import cn.tycoding.domain.Receiver;
import cn.tycoding.service.ReceiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/receivers")
public class ReceiverResource {
    private final Logger logger=LoggerFactory.getLogger(ReceiverResource.class);
    private final ReceiverService receiverService;


    public ReceiverResource(ReceiverService receiverService) {
        this.receiverService = receiverService;
    }


    @PostMapping
    public Receiver createReceiver(@RequestBody Receiver receiver){
        return receiverService.createReceiver(receiver);
    }


    @DeleteMapping("/{id}")
    public String deleteReceiver(@PathVariable("id") int id){
        logger.info("Rest 收货方注销请求");
        receiverService.deleteReceiver(id);
        return "删除receiver"+id+"成功";
    }
}


