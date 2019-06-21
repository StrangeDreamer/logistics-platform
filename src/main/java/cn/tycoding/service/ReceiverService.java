package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Receiver;

import cn.tycoding.domain.Shipper;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.exception.ShipperException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.ReceiverRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReceiverService {

    private final Logger logger = LoggerFactory.getLogger(ReceiverService.class);
    private final ReceiverRepository receiverRepository;
    private final CargoRepository cargoRepository;


    public ReceiverService(ReceiverRepository receiverRepository, CargoRepository cargoRepository) {
        this.receiverRepository = receiverRepository;
        this.cargoRepository = cargoRepository;

    }


    // 收货方注册
    public Receiver createReceiver(Receiver receiver){
        Receiver receiver1 = new Receiver();
        receiver1.setName(receiver.getName());
        receiverRepository.save(receiver1);
        return receiver1;
    }


    // 收货方注销
    public String  deleteReceiver(int id){
        receiverRepository.findById(id).orElseThrow(()->new ReceiverException("该收货方不存在"));
        List<Cargo> list = cargoRepository.findAllByReceiverId(id);
        // 1.如果该发货⽅方有尚未完成的订单，返回订单提醒⽤用户并拒绝注销。
        for (Cargo cargo:list) {
            if (cargo.getStatus() < 6) {
                throw new ReceiverException("注销失败！当前收货方还有订单未完成的订单！");
            }
            if (cargo.getStatus() == 10) {
                throw new ReceiverException("注销失败！当前收货方存在异常订单！");
            }
        }
        receiverRepository.findById(id).ifPresent(receiver -> {
            receiverRepository.delete(receiver);
            logger.info("收货发注销成功！");
        });
        return "删除receiver"+id+"成功";
    }

    // 查询指定id收货方
    public Receiver findReceiverById(int receiverId){
        Receiver receiver = receiverRepository.findById(receiverId).orElseThrow(()->new ReceiverException("该收货方不存在"));
        return receiver;
    }

    // 查询所有发货方
    public List<Receiver> findAll(){
        return receiverRepository.findAll();
    }


}
