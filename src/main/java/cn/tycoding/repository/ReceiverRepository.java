package cn.tycoding.repository;

import cn.tycoding.domain.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ReceiverRepository extends JpaRepository<Receiver,Integer> {


}


