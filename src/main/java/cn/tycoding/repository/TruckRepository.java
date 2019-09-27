package cn.tycoding.repository;

import cn.tycoding.domain.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TruckRepository extends JpaRepository<Truck,Integer> {

    int countCargosByIdIsNotNull();
    Truck findTruckById(int truckId);
    boolean existsTruckByIdgerenshenfenzheng(String id);
    boolean existsTruckByName(String id);

    Optional<Truck> findTruckByName(String name);
    List<Truck> findTruckByField(String field);

}
