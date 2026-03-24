package com.finflow.application.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "document-service")
public interface DocumentServiceClient {

 @GetMapping("/documents/{id}/exists")
 boolean documentsUploaded(@PathVariable UUID id);

}
