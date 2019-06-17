package cn.tycoding.repository;

import cn.tycoding.domain.Platform;
import cn.tycoding.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @auther qlXie
 * @date 2019-06-11 11:25
 */

@Repository
public interface PlatformRepository extends JpaRepository<Platform,Integer> {


    @Query(nativeQuery = true,
            value = "select * from platform order by id DESC limit 1")
    Platform findRecentPltf();

}
