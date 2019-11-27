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

        if(!users.findUserByUsernameAndKind("manager",4).isPresent()){
            this.users.save(User.builder()
                    .username("manager")
                    .password(this.passwordEncoder.encode("123456"))
                    .kind(4)
                    .roles(Arrays.asList("ROLE_ADMIN"))
                    .build());
        }




    }
}
