package cn.tycoding.repository;

import cn.tycoding.domain.TransferredCargo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferredCargoRepo extends JpaRepository<TransferredCargo,Integer> {
}
