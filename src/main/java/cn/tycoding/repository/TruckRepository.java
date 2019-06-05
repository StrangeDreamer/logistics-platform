package cn.tycoding.repository;

import cn.tycoding.domain.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck,Integer> {

    Truck findShippersById(int id);
    List<Truck> findAll();


}
