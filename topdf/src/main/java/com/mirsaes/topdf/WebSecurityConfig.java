package com.mirsaes.topdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig
{
	@Value("${security.user.name:user}")
	private String userName;

	@Value("${security.user.password:password}")
	private String password;

	@Value("${security.enabled:false}")
	private Boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
	{
		// if security is not enabled, bypass security
		// otherwise use basic auth
		httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		if (Boolean.FALSE == securityEnabled)
		{
			httpSecurity.csrf().disable().cors().disable().authorizeHttpRequests().requestMatchers("/live/**")
					.permitAll();
		} else
		{
			// protect all except previously allowed with username/password
			httpSecurity.csrf().disable().cors().disable()
				.authorizeHttpRequests()
				.requestMatchers("/live/test").permitAll()
				.anyRequest().authenticated()//
				.and().httpBasic().realmName("cyao2pdf")//
				.and().logout().permitAll(); // don't have sessions for logout : )
		}
		return httpSecurity.build();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.inMemoryAuthentication().withUser(userName).password("{noop}"+password).roles("USER");
	}
}
