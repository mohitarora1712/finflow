package com.finflow.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.service.LoanApplicationService;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

 private final LoanApplicationService service;

 public LoanApplicationController(LoanApplicationService service){
  this.service = service;
 }

 @PostMapping("/draft")
 public LoanApplication draft(
  @RequestHeader("X-User-Email") String email,
  @RequestBody CreateDraftRequest req){
  return service.createDraft(email,req);
 }

 @PostMapping("/{id}/submit")
 public LoanApplication submit(
  @PathVariable UUID id,
  @RequestHeader("X-User-Email") String email){
  return service.submit(id,email);
 }

 @GetMapping("/my")
 public List<LoanApplication> dashboard(
  @RequestHeader("X-User-Email") String email){
  return service.getUserApps(email);
 }

 @PatchMapping("/{id}/review")
 public LoanApplication review(@PathVariable UUID id){
  return service.moveToReview(id);
 }

 @PatchMapping("/{id}/decision")
 public LoanApplication decision(
  @PathVariable UUID id,
  @RequestBody AdminDecisionRequest req){
  return service.decision(id,req);
 }

}
