package kroryi.his.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.his.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.Map;

@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class CustomerSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Autowired
    private CustomErrorHandlerConfig.HandlerExceptionResolver customErrorHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("--보안환경설정--");
        http
                .csrf(csrf -> csrf.disable())
                .rememberMe(me -> me
                        .key("12345")
                        .tokenRepository(persistentTokenRepository())
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/member/login/**").permitAll()
                        .requestMatchers("/member/login-proc").permitAll()
//                        .requestMatchers("/home").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .loginProcessingUrl("/member/login-proc")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler(accessDeniedHandler())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션 생성 정책
                        .maximumSessions(1) // 최대 세션 수
                        .expiredUrl("/login?expired") // 세션 만료 시 리다이렉트 URL
                )
                .userDetailsService(userDetailsService)
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("--웹환경설정--");
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ResponseEntity<Map<String, String>> responseEntity =
                    customErrorHandler.handle403(accessDeniedException);
            response.setStatus(responseEntity.getStatusCodeValue());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper()
                    .writeValueAsString(responseEntity.getBody()));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build(); // Directly return the builder's result
    }
}
