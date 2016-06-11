package com.mirsaes.topdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
	@Value("{security.user.name:user}")
	private String userName;

	@Value("{security.user.password:password}")
	private String password;

	@Value("${security.enabled:false}")
	private Boolean securityEnabled;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		// enable test end point to be open
		http.antMatcher("/live/test")//
				.csrf().disable()//
				.authorizeRequests().antMatchers("/live/test")//
				.permitAll();

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
			http.authorizeRequests().antMatchers("/").permitAll()//
					.anyRequest().authenticated()//
					.and().httpBasic().realmName("cyao2pdf")//
					.and().logout().permitAll();
		}

	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.inMemoryAuthentication().withUser(userName).password(password).roles("USER");
	}
}
