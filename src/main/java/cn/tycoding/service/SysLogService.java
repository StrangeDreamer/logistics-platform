package cn.tycoding.service;

import cn.tycoding.domain.SysLog;
import cn.tycoding.repository.SysLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysLogService {
    @Autowired
    private SysLogRepository sysLogRepository;
    public void save(SysLog sysLog) {
        sysLogRepository.save(sysLog);
    }

}
