package cn.tycoding.service;

import cn.tycoding.domain.Bid;
import cn.tycoding.domain.TransCargo;
import cn.tycoding.repository.BidRepository;
import cn.tycoding.repository.TransCargoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional

public class TransCargoService {
    private final Logger logger = LoggerFactory.getLogger(BidService.class);
    @Autowired
    private TransCargoRepository transCargoRepository;

    /**
     * 判断缓存里面是否为空，以确定该订单是否有人抢
     * @return
     */
    public Optional findTransCargoById(int cargoId){
       return transCargoRepository.findByCargoId(cargoId);
    }
}

