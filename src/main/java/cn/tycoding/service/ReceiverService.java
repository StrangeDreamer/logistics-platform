package cn.tycoding.service;


import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.Receiver;

import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.Truck;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.exception.ReceiverException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.ReceiverRepository;

import cn.tycoding.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReceiverService {

    private final Logger logger = LoggerFactory.getLogger(ReceiverService.class);
    private final ReceiverRepository receiverRepository;
    private final CargoRepository cargoRepository;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    public ReceiverService(ReceiverRepository receiverRepository, CargoRepository cargoRepository) {
        this.receiverRepository = receiverRepository;
        this.cargoRepository = cargoRepository;
    }

    // 登录
    public Map login(String name, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, password));
        Receiver receiver = this.receiverRepository.findReceiverByName(name).orElseThrow(() -> new UsernameNotFoundException("Username " + name + "not found"));
        String token = jwtTokenProvider.createToken(name, receiver.getRoles());
        Map<Object, Object> model = new HashMap<>();
        model.put("id", receiver.getId());
        model.put("name", name);
        model.put("token", token);
        return model;
    }

    // 收货方注册
    @Transactional
    public Receiver createReceiver(Receiver receiver) {
        // 检查该注册人的身份证是否已经用于该项的注册
        if (receiverRepository.existsReceiverByIdgerenshenfenzheng(receiver.getIdgerenshenfenzheng())) {
            throw new ReceiverException("该个人身份证已经被用于注册收货方！");
        }

        // 检查用户名是否已经被占用
        if (receiverRepository.existsReceiverByName(receiver.getName())) {
            throw new ReceiverException("该用户名已被占用！");
        }
        receiverRepository.save(Receiver.builder()
                .name(receiver.getName())
                .idgerenshenfenzheng(receiver.getIdgerenshenfenzheng())
                .id_gongsitongyidaima(receiver.getId_gongsitongyidaima())
                .occupation(receiver.getOccupation())
                .telNumber(receiver.getTelNumber())
                .address(receiver.getAddress())
                .activated(false)
                .roles(Arrays.asList("ROLE_USER"))
                .build());
        Receiver receiver1 = receiverRepository.findReceiverByName(receiver.getName()).get();
        // 获得银行账号和保险
        bankAccountService.check(receiver1.getId(), "receiver");
        return receiver1;
    }

    // 收货方注销
    public String deleteReceiver(int id) {
        receiverRepository.findById(id).orElseThrow(() -> new ReceiverException("该收货方不存在"));
        List<Cargo> list = cargoRepository.findAllByReceiverId(id);
        // 1.如果该发货⽅方有尚未完成的订单，返回订单提醒⽤用户并拒绝注销。
        for (Cargo cargo : list) {
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
        return "删除receiver" + id + "成功";
    }

    // 查询指定id收货方
    public Receiver findReceiverById(int receiverId) {
        Receiver receiver = receiverRepository.findById(receiverId).orElseThrow(() -> new ReceiverException("该收货方不存在"));
        return receiver;
    }

    // 查询所有发货方
    public List<Receiver> findAll() {
        return receiverRepository.findAll();
    }

    public Receiver active(int id) {
        Receiver receiver = receiverRepository.findById(id).orElseThrow(() -> new RuntimeException("该发货方不存在"));
        receiver.setActivated(true);
        receiverRepository.save(receiver);
        logger.info("激活成功！");
        return receiver;
    }

}
