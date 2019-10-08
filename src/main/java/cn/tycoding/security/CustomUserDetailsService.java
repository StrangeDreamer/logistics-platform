package cn.tycoding.security;

import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.TruckRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

//    private UserRepository users;
    private TruckRepository truckRepository;

    public CustomUserDetailsService(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String truckName) throws UsernameNotFoundException {
        return this.truckRepository.findTruckByName(truckName).orElseThrow(() -> new UsernameNotFoundException("Username: " + truckName + " not found"));

    }
}
