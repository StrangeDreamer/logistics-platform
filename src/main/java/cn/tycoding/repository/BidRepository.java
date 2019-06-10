package cn.tycoding.repository;

import cn.tycoding.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid,Integer> {
}
