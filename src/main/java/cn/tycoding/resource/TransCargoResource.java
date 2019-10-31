package cn.tycoding.resource;

import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.BankAccount;
import cn.tycoding.domain.Cargo;
import cn.tycoding.domain.TransCargo;
import cn.tycoding.repository.BankAccountRepository;
import cn.tycoding.service.BankAccountService;
import cn.tycoding.service.ShipperService;
import cn.tycoding.service.TransCargoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

@RestController
@RequestMapping("/transCargo")
public class TransCargoResource {
    private final Logger logger=LoggerFactory.getLogger(ShipperResource.class);
    private final TransCargoService transCargoService;

    public TransCargoResource(TransCargoService transCargoService) {
        this.transCargoService = transCargoService;
    }

    @MyLog(value = "查询转单信息")
    @GetMapping("/{id}/")
    public Optional<TransCargo> findTransCargoById(@PathVariable("id") int id){
       return transCargoService.findTransCargoById(id);

    }


    @MyLog(value = "追踪转单历史")
    @GetMapping("/history/{id}/")
    public Stack<Cargo> getTransCargoHistory(@PathVariable("id") int id){
        return transCargoService.getTransCargoHistory(id);
    }

}
