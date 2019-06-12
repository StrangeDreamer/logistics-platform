package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Receiver;

import cn.tycoding.repository.ReceiverRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        receiver1.setName(receiver.getName());
        receiverRepository.save(receiver1);
        return receiver1;
    }


    // 收货方注销
    public void deleteReceiver(int id){
        receiverRepository.findById(id).ifPresent(receiver -> {
            receiverRepository.delete(receiver);
            logger.info("发货发注销成功！");
        });
    }

    // 查询指定id收货方
    public Receiver findReceiversById(int shipperId){
        return receiverRepository.findShippersById(shipperId);
    }

    // 查询所有发货方
    public List<Receiver> findAll(){
        return receiverRepository.findAll();
    }

    public Cargo inspectCargo(int cargoId) {
    }
}
