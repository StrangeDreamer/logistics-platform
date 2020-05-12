package cn.tycoding.resource;


import cn.tycoding.aop.MyLog;
import cn.tycoding.domain.Receiver;
import cn.tycoding.domain.Shipper;
import cn.tycoding.domain.Truck;
import cn.tycoding.security.jwt.JwtTokenProvider;
import cn.tycoding.service.ReceiverService;
import cn.tycoding.service.ShipperService;
import cn.tycoding.service.TruckService;
import cn.tycoding.service.UserService;
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

    @Autowired
    UserService userService;

    /**
     * 承运方登录
     */

    @MyLog(value = "承运方登录")
    @GetMapping("/login/trucks")
    public ResponseEntity truckLogin(@RequestParam String name,@RequestParam String password) {
        log.info("承运方登录");
        log.info(userService.getCurrentPwd(name,1));
        return ok(userService.truckLogin(name, password,1));
    }

    /**
     * 发货方登录
     */
    @MyLog(value = "发货方方登录")
    @GetMapping("/login/shippers")
    public ResponseEntity shipperLogin(@RequestParam String name,@RequestParam String password) {
        log.info("发货方方登录");
        //   return ok(userService.login(name, password,2));
        return ok(userService.shipperLogin(name, password,2));
    }

    /**
     * 收货方登录
     */
    @MyLog(value = "收货方登录")
    @GetMapping("/login/receivers")
    public ResponseEntity ReceiverLogin(@RequestParam String name,@RequestParam String password) {
        log.info("收货方登录");
        return ok(userService.receiverLogin(name, password,3));
    }

    @MyLog(value = "平台登录")
    @GetMapping("/login/platform")
    public ResponseEntity PlatformLogin(@RequestParam String name,@RequestParam String password) {
        log.info("平台方登录");
        return ok(userService.platformLogin(name, password,4));
    }
    /**
     * 承运方注册
     *
     * @param truck
     * @return
     */
    @MyLog(value = "承运方注册")
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
    @MyLog(value = "发货方注册")
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
    @MyLog(value = "收货方注册")
    @PostMapping("/signin/receivers")
    public Receiver receiverSignin(@RequestBody Receiver receiver) {
        log.info("收货方注册请求");
        return receiverService.createReceiver(receiver);
    }
}
