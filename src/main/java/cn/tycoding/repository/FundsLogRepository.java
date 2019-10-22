package cn.tycoding.repository;

import cn.tycoding.domain.FundsLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundsLogRepository extends MongoRepository<FundsLog,String> {
}
