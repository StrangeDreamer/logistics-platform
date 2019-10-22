package cn.tycoding.service;

import cn.tycoding.domain.FundsLog;
import cn.tycoding.repository.FundsLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FundsLogService {
    @Autowired
    private FundsLogRepository fundsLogRepository;
    public void save(FundsLog fundsLog) {
        fundsLogRepository.save(fundsLog);
    }
}
