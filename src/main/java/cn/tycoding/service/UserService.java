package cn.tycoding.service;

import cn.tycoding.domain.Truck;
import cn.tycoding.domain.User;
import cn.tycoding.exception.TruckException;
import cn.tycoding.repository.TruckRepository;
import cn.tycoding.security.CustomUserDetailsService;
import cn.tycoding.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    TruckRepository truckRepository;

    public Map login(String name, String password, int kind) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, password));
        User user = (User) this.userDetailsService.loadUserByUsernameAndKind(name, kind);
        String token = jwtTokenProvider.createToken(name, user.getRoles());
        Map<Object, Object> model = new HashMap<>();
        model.put("id", user.getOwnId());
        model.put("name", name);
        model.put("token", token);
        return model;
    }

    public Map platformLogin(String name, String password,int kind) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, password));
        User user = (User) this.userDetailsService.loadUserByUsernameAndKind(name,kind);
        String token = jwtTokenProvider.createToken(name, user.getRoles());
        Map<Object, Object> model = new HashMap<>();
        model.put("name", name);
        model.put("token", token);
        return model;
    }

    public Object truckLogin(String name, String password, int kind) {
        User user = (User) this.userDetailsService.loadUserByUsernameAndKind(name, kind);
        String token = jwtTokenProvider.createToken(name, user.getRoles());
        Map<Object, Object> model = new HashMap<>();
        model.put("id", user.getOwnId());
        Truck truck=truckRepository.findTruckById(user.getOwnId());
        model.put("weight",truck.getAvailableWeight());
        model.put("volume",truck.getAvailableVolume());
        model.put("filed",truck.getField());
        model.put("name", name);
        model.put("token", token);
        return model;
    }

    public String getCurrentPwd(String name,int kind){
        User user= (User) this.userDetailsService.loadUserByUsernameAndKind(name,kind);
        return user.getPassword();
    }
}
