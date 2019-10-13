package cn.tycoding;

import cn.tycoding.domain.User;
import cn.tycoding.repository.UserRepository;
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
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if(!users.findUserByUsernameAndKind("admin",4).isPresent()){
            this.users.save(User.builder()
                    .username("admin")
                    .password(this.passwordEncoder.encode("admin"))
                    .kind(4)
                    .roles(Arrays.asList("ROLE_ADMIN"))
                    .build());
        }




    }
}
