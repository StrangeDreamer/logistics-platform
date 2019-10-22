package cn.tycoding.repository;

import cn.tycoding.domain.SysLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends MongoRepository<SysLog,String> {

}
