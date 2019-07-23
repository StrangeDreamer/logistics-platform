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

    boolean existsReceiverByIdgerenshenfenzheng(String id);
    boolean existsReceiverByName(String name);
    Optional<Receiver> findReceiverByName(String name);
}


