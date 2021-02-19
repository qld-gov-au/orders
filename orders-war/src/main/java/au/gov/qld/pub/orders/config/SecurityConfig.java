package au.gov.qld.pub.orders.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        //allow one session, and changeSessionId when logging in, since we have data prior to login.
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .maximumSessions(1).and().sessionFixation().changeSessionId();

        httpSecurity.authorizeRequests()
            .antMatchers("/", "/favicon.ico").permitAll()
            .antMatchers("*").permitAll(); //auth goes here
    }
}
