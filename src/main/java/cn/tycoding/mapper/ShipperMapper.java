package cn.tycoding.mapper;

import cn.tycoding.entity.Shipper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ShipperMapper {
    List<Shipper> findAll();
    int insertShipper(@Param("id") String id,@Param("isActivated") boolean isActivated);

}
