package cn.tycoding.repository;

import cn.tycoding.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @auther qlXie
 * @date 2019-06-11 11:25
 */
public interface PlatformRepository {

    Platform findCargoById(int id);
}
