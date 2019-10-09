/*
package cn.tycoding;

import cn.tycoding.domain.Truck;
import cn.tycoding.repository.TruckRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {


    @Autowired
    TruckRepository users;

//    @Autowired
//    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

//        this.users.save(Truck.builder()
//                        .name("user")
////            .password(this.passwordEncoder.encode("password"))
//                        .password("password")
//                        .roles(Arrays.asList("ROLE_USER"))
//                        .build()
//        );

        this.users.save(Truck.builder()
                        .name("admin")
//            .password(this.passwordEncoder.encode("password"))
                        .password("password")
                        .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                        .build()
        );

        log.debug("printing all users...");
        this.users.findAll().forEach(v -> log.debug(" User :" + v.toString()));
    }
}
*/
