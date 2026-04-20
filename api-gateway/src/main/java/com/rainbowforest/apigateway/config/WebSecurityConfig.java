package com.rainbowforest.apigateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // BẮT BUỘC PHẢI CÓ ANNOTATION NÀY
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 🔥 BỎ .cors() đi Hiếu nhé.
		// Vì mình đã có CorsConfig chạy ở mức ưu tiên cao nhất rồi.
		http.csrf().disable()
				.authorizeRequests()
				.antMatchers("/**").permitAll()
				.anyRequest().authenticated();
	}
}