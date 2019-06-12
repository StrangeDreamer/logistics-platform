package cn.tycoding.service;

import cn.tycoding.domain.Inspection;
import cn.tycoding.repository.InspectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @auther qlXie
 * @date 2019-06-12 11:00
 */

@Service
@Transactional
public class InspectionService {
    private final Logger logger = LoggerFactory.getLogger(InspectionService.class);
    @Autowired
    private InspectionRepository inspectionRepository;

    public String saveInspection(Inspection inspection){
        Inspection inspection1=new Inspection();
        inspection1.setCargoId(inspection.getCargoId());
        inspection1.setInspectionResult(inspection.getInspectionResult());
        logger.info("保存验货请求");
        inspectionRepository.save(inspection1);
        return "保存验货请求成功";
    }


}
