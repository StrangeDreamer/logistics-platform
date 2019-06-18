package cn.tycoding.repository;

import cn.tycoding.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid,Integer> {

    List<Bid> findAllByCargoId(int cargoId);
}
