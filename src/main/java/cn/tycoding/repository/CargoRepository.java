package cn.tycoding.repository;

import cn.tycoding.domain.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo,Integer> {

    List<Cargo> findAllByShipperId(int id);
}
