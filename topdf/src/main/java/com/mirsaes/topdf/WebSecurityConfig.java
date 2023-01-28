package com.mirsaes.topdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
	{
		// if security is not enabled, bypass security
		// otherwise use basic auth
		if (Boolean.FALSE == securityEnabled)
		{
			http.antMatcher("/live/topdf")//
					.csrf().disable()//
					.authorizeRequests().antMatchers("/live/topdf")//
					.permitAll();
		} else
		{
			// protect all except previously allowed with username/password
			http.csrf().disable()
				.authorizeRequests()
				.antMatchers("/live/test").permitAll()
				.anyRequest().authenticated()//
				.and().httpBasic().realmName("cyao2pdf")//
				.and().logout().permitAll();
		}
		return http.build();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.inMemoryAuthentication().withUser(userName).password("{noop}"+password).roles("USER");
	}
}
