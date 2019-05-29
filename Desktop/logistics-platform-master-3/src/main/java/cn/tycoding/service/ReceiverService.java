package cn.tycoding.service;


import cn.tycoding.domain.Receiver;

import cn.tycoding.repository.ReceiverRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReceiverService {

    private final Logger logger = LoggerFactory.getLogger(ReceiverService.class);
    private final ReceiverRepository receiverRepository;

    public ReceiverService(ReceiverRepository receiverRepository) {
        this.receiverRepository = receiverRepository;
    }


    // 收货方注册
    public Receiver createReceiver(Receiver receiver){
        Receiver receiver1 = new Receiver();

        receiver.setName(receiver1.getName());
        receiverRepository.save(receiver1);
        return receiver1;
    }
    // 收货方注销
    public void deleteShipper(int id){
        receiverRepository.findById(id).ifPresent(shipper -> {
            receiverRepository.delete(shipper);
            logger.info("发货发注销成功！");
        });
    }
}
