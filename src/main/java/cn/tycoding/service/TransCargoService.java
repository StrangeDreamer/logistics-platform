package cn.tycoding.service;

import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.TransCargo;
import cn.tycoding.exception.CargoException;
import cn.tycoding.repository.CargoRepository;
import cn.tycoding.repository.TransCargoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional

public class TransCargoService {
    private final Logger logger = LoggerFactory.getLogger(BidService.class);
    @Autowired
    private TransCargoRepository transCargoRepository;
    @Autowired
    private CargoRepository cargoRepository;

    public Optional findTransCargoById(int cargoId){
       return transCargoRepository.findByCargoId(cargoId);
    }



}

