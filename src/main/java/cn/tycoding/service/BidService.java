package cn.tycoding.service;

import cn.tycoding.domain.Bid;
import cn.tycoding.repository.BidRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional

public class BidService {
    private final Logger logger = LoggerFactory.getLogger(BidService.class);
    @Autowired
    private BidRepository bidRepository;
    //设置秒杀redis缓存的key
    private final String bidsKey = "bids";
    private final String cargoKey = "Cargo";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 判断缓存里面是否为空，以确定该订单是否有人抢
     * @return
     */
    public Bid checkRedis(int cargoId){
        Bid redisbid = (Bid) redisTemplate.boundHashOps(bidsKey).get(cargoId);
        if (redisbid==null){
            return null;
        }
        return redisbid;
    }
}
