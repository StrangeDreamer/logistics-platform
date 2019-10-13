package cn.tycoding.repository;


import cn.tycoding.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsernameAndKind(String username,int kind);
    Optional<User> findUserByUsername(String username);

}
