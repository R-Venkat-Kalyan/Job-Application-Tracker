package com.example.Job_Application_Tracker.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf().disable()
	        .authorizeHttpRequests(auth -> auth
	            .anyRequest().permitAll()
	        )
	        .formLogin(form -> form
	            .loginPage("/sign-in")
	            .loginProcessingUrl("/userLogin")             // ✅ Login POST target
	            .defaultSuccessUrl("/dashboard")        // ✅ After login, go to /dashboard
	            .failureUrl("/sign-in?error=true")            // Better failure redirect
	            .permitAll()
	        )
	        .logout(logout -> logout
		            .logoutUrl("/log-out")  // URL to trigger the logout process
		            .logoutSuccessUrl("/")  // Redirect to login page after successful logout
		            .invalidateHttpSession(true)  // Invalidate the HTTP session to clear user data
		            .deleteCookies("JSESSIONID")  // Remove the JSESSIONID cookie
		        );

	    return http.build();
	}



}
