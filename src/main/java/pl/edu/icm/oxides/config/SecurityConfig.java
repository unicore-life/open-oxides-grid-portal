package pl.edu.icm.oxides.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/assets/*");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().deny()
                .and()
                .csrf().requireCsrfProtectionMatcher(csrfRequestMatcher)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login").permitAll()
                .and()
                .logout().permitAll();
//        .authorizeRequests()
//                .antMatchers("/", "/home").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .loginPage("/login").permitAll()
//                .and()
//                .logout().permitAll();
    }

    private RequestMatcher csrfRequestMatcher = new RequestMatcher() {
        private Pattern allowedMethods = Pattern.compile("^GET$");
        private AntPathRequestMatcher[] disabledCsfrRequestMatchers = {
                new AntPathRequestMatcher("/oxides/authn")
        };

        @Override
        public boolean matches(HttpServletRequest request) {
            // Skip allowed methods:
            if (allowedMethods.matcher(request.getMethod()).matches()) {
                return false;
            }
            // If the request match one url the CSFR protection will be disabled:
            for (AntPathRequestMatcher rm : disabledCsfrRequestMatchers) {
                if (rm.matches(request)) {
                    return false;
                }
            }
            return true;
        }
    };
}
