package cn.tycoding.repository;

import cn.tycoding.domain.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipperRepository extends JpaRepository<Shipper,Integer> {

    Shipper findShippersById(int shipperId);
    List<Shipper> findAll();

    int countCargosByIdIsNotNull();
    boolean existsShipperByIdgerenshenfenzheng(String id);
    boolean existsShipperByName(String name);
    Optional<Shipper> findShipperByName(String name);
}
