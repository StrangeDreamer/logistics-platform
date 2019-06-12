package cn.tycoding.repository;
import cn.tycoding.domain.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @auther qlXie
 * @date 2019-06-12 10:54
 */
public interface InspectionRepository extends JpaRepository<Inspection,Integer> {
}
