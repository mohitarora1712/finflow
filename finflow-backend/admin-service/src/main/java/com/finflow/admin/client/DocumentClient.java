package com.finflow.admin.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.finflow.admin.config.FeignIdentityConfig;
import com.finflow.admin.dto.DocumentResponse;

@FeignClient(name = "document-service", configuration = FeignIdentityConfig.class)
public interface DocumentClient {

 @PatchMapping("/{id}/verify")
 DocumentResponse verify(@PathVariable UUID id);
}