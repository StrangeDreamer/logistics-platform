package cn.tycoding.repository;

import cn.tycoding.domain.Bid;
import cn.tycoding.domain.TransCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransCargoRepository extends JpaRepository<TransCargo,Integer>{
    Optional<TransCargo> findByCargoId(int cargoId);

}
