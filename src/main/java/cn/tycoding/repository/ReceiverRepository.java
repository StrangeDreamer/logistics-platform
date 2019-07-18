package cn.tycoding.repository;

import cn.tycoding.domain.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ReceiverRepository extends JpaRepository<Receiver,Integer> {

    Receiver findReceiverById(int Id);
    List<Receiver> findAll();

    int countCargosByIdIsNotNull();

    boolean existsReceiverByIdgerenshenfenzheng(String id);
    boolean existsReceiverByName(String name);
    Optional<Receiver> findReceiverByName(String name);
}


