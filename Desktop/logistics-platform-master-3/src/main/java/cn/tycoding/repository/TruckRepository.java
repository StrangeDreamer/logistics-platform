package cn.tycoding.repository;

import cn.tycoding.domain.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck,Integer> {


}
