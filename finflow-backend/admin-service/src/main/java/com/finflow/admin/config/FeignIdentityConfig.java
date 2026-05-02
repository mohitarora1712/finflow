package com.finflow.admin.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.finflow.admin.context.IdentityContext;

@Configuration
public class FeignIdentityConfig {

 @Bean
 public RequestInterceptor identityPropagationInterceptor() {
  return template -> {

   String email = IdentityContext.getEmail();
   String role = IdentityContext.getRole();

   if (email != null)
    template.header("X-User-Email", email);

   if (role != null)
    template.header("X-User-Role", role);
  };
 }
}