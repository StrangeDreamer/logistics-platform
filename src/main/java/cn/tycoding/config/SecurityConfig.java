package cn.tycoding.config;

import cn.tycoding.security.jwt.JwtSecurityConfigurer;
import cn.tycoding.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
            .httpBasic().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers("/bankAccounts/**").authenticated()
                .antMatchers("/bids/**").authenticated()
                .antMatchers("/cargos/**").authenticated()
                .antMatchers("/insuranceAccounts/**").authenticated()
                .antMatchers("/platform/**").authenticated()
                .antMatchers("/receivers/**").authenticated()
                .antMatchers("/shippers/**").authenticated()
                .antMatchers("/trucks/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .apply(new JwtSecurityConfigurer(jwtTokenProvider));
        //@formatter:on
    }


}

