package cn.tycoding.repository;

import cn.tycoding.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BidRepository extends JpaRepository<Bid,Integer> {

    List<Bid> findAllByCargoId(int cargoId);
    List<Bid> findAll();

}
