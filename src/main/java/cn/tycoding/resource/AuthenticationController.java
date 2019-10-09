package cn.tycoding.resource;


import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.Truck;
import cn.tycoding.security.jwt.JwtTokenProvider;
import cn.tycoding.service.ReceiverService;
import cn.tycoding.service.ShipperService;
import cn.tycoding.service.TruckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    TruckService truckService;
    @Autowired
    ShipperService shipperService;
    @Autowired
    ReceiverService receiverService;

    /**
     * 承运方登录
     *
     * @param data
     * @return
     */

    @GetMapping("/login/trucks")
    public ResponseEntity truckLogin(@RequestBody AuthenticationRequest data) {
        log.info("承运方登录");
        return ok(truckService.login(data.getName(), data.getPassword()));
    }

    /**
     * 发货方登录
     *
     * @param data
     * @return
     */
    @GetMapping("/login/shippers")
    public ResponseEntity shipperLogin(@RequestBody AuthenticationRequest data) {
        log.info("发货方方登录");
        return ok(shipperService.login(data.getName(), data.getPassword()));
    }

    /**
     * 收货方登录
     *
     * @param data
     * @return
     */
    @GetMapping("/login/receivers")
    public ResponseEntity ReceiverLogin(@RequestBody AuthenticationRequest data) {
        log.info("收货方登录");
        return ok(receiverService.login(data.getName(), data.getPassword()));
    }

    /**
     * 承运方注册
     *
     * @param truck
     * @return
     */
    @PostMapping("/signin/trucks")
    public Truck truckSignin(@RequestBody Truck truck) {
        log.info("承运方注册请求");
        return truckService.createTruck(truck);

    }

    /**
     * 发货方注册
     *
     * @param shipper
     * @return
     */
    @PostMapping("/signin/shippers")
    public Shipper shipperSignin(@RequestBody Shipper shipper) {
        log.info("发货方注册请求");
        return shipperService.createShipper(shipper);

    }

    /**
     * 收货方注册
     *
     * @param receiver
     * @return
     */
    @PostMapping("/signin/receivers")
    public Receiver receiverSignin(@RequestBody Receiver receiver) {
        log.info("收货方注册请求");
        return receiverService.createReceiver(receiver);
    }
}
