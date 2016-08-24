package pl.edu.icm.oxides.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import pl.edu.icm.oxides.authn.SingleLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public SingleLogoutHandler singleLogoutHandler() {
        return new SingleLogoutHandler();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/assets/**")
                .antMatchers("/fonts/**")
                .antMatchers("/img/**")
                .antMatchers("/favicon.ico");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        protectFromClickjacking(http);

        http
                .csrf()
                .requireCsrfProtectionMatcher(csrfRequestMatcher);

        http
                .sessionManagement()
                .sessionFixation()
                .newSession();

        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login").permitAll();

        http
                .logout()
                .addLogoutHandler(singleLogoutHandler())
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutUrl("/logout");
    }

    private void protectFromClickjacking(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions()
                .deny();
    }

    private RequestMatcher csrfRequestMatcher = new RequestMatcher() {
        private Pattern allowedMethods = Pattern.compile("^GET$");
        private AntPathRequestMatcher[] disabledCsrfRequestMatchers = {
                new AntPathRequestMatcher("/authn/slo"),
                new AntPathRequestMatcher("/authn/signout"),
                // ^^^ - both needed to handle Unity's POST request.
                new AntPathRequestMatcher("/authn/sign-in")
        };

        @Override
        public boolean matches(HttpServletRequest request) {
            // Skip allowed methods:
            if (allowedMethods.matcher(request.getMethod()).matches()) {
                return false;
            }
            // If the request match one url the CSRF protection will be disabled:
            for (AntPathRequestMatcher rm : disabledCsrfRequestMatchers) {
                if (rm.matches(request)) {
                    return false;
                }
            }
            return true;
        }
    };
}
