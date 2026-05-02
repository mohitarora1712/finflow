package com.finflow.admin.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.finflow.admin.config.FeignIdentityConfig;
import com.finflow.admin.dto.AdminDecisionRequest;
import com.finflow.admin.dto.LoanApplicationResponse;

@FeignClient(name = "application-service", configuration = FeignIdentityConfig.class)
public interface ApplicationClient {

 @GetMapping("/admin")
 List<LoanApplicationResponse> getAll();

 @PatchMapping("/{id}/decision")
 LoanApplicationResponse decide(
     @PathVariable UUID id,
     @RequestBody AdminDecisionRequest req
 );
}