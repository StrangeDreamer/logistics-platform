package cn.tycoding.service.lpimpl;

import cn.tycoding.mapper.ShipperMapper;
import cn.tycoding.service.lp.ShipperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipperServiceImpl implements ShipperService {
    @Autowired
    private ShipperMapper shipperMapper;


    @Override
    @Transactional
    public int insertShipper(String id, boolean isActivated) {
        return shipperMapper.insertShipper(id,isActivated);
    }
}
