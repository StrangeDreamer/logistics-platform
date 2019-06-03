package cn.tycoding.repository;

import cn.tycoding.domain.Receiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ReceiverMapper {
    List<Receiver> findAll();
    int insertReceiver(@Param("id") String id, @Param("isActivated") boolean isActivated);

}
