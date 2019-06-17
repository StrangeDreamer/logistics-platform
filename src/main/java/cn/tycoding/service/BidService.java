package cn.tycoding.service;

import cn.tycoding.domain.Bid;
import cn.tycoding.repository.BidRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional

public class BidService {
    private final Logger logger = LoggerFactory.getLogger(BidService.class);
    @Autowired
    private BidRepository bidRepository;

    public String saveBid(Bid bid){
        Bid bid1=new Bid();
        bid1.setCargoId(bid.getCargoId());
        bid1.setTruckId(bid.getTruckId());
        bid1.setBidPrice(bid.getBidPrice());
        logger.info("保存竞价请求");
        bidRepository.save(bid1);
        return "保存竞价请求成功";
    }
}
