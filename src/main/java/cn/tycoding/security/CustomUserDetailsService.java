package cn.tycoding.security;


import cn.tycoding.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.users.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));
    }

    public UserDetails loadUserByUsernameAndKind(String username,int kind) throws UsernameNotFoundException{
        return this.users.findUserByUsernameAndKind(username,kind).orElseThrow(()->new UsernameNotFoundException("Username "+username+" not found"));
    }
}
