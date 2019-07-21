package cn.tycoding.repository;
import cn.tycoding.domain.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @auther qlXie
 * @date 2019-06-12 10:54
 */
@Repository
public interface InspectionRepository extends JpaRepository<Inspection,Integer> {

}
