package cn.tycoding.repository;

import cn.tycoding.domain.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiverRepository extends JpaRepository<Receiver,Integer> {

    Receiver findReceiverById(int Id);
    List<Receiver> findAll();

    int countCargosByIdIsNotNull();


    boolean existsReceiverByName(String name);
    Optional<Receiver> findReceiverByName(String name);

    // 查询该身份证的收货人是否存在
    boolean existsReceiverByIdgerenshenfenzheng(String id);
    // 根据身份证查找收货人
    Receiver findReceiverByIdgerenshenfenzheng(String name);
}


