package com.finflow.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.finflow.application.client.DocumentServiceClient;
import com.finflow.application.config.DecisionEventPublisher;
import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.entity.LoanApplicationStatus;
import com.finflow.application.entity.LoanStatusHistory;
import com.finflow.application.repository.LoanApplicationRepository;
import com.finflow.application.repository.StatusHistoryRepository;


@Service
public class LoanApplicationService {

 private final LoanApplicationRepository repo;
 private final StatusHistoryRepository historyRepo;
 private final DocumentServiceClient docClient;
 private final DecisionEventPublisher publisher;

 public LoanApplicationService(
  LoanApplicationRepository repo,
  StatusHistoryRepository historyRepo,
  DocumentServiceClient docClient,
  DecisionEventPublisher publisher) {

  this.repo = repo;
  this.historyRepo = historyRepo;
  this.docClient = docClient;
  this.publisher = publisher;
 }

 @Cacheable(value="dashboard", key="#email")
 public List<LoanApplication> getUserApps(String email){
  return repo.findByUserEmail(email);
 }

 public LoanApplication createDraft(String email, CreateDraftRequest req){
  LoanApplication app = new LoanApplication();
  app.setUserEmail(email);
  app.setAmount(req.getAmount());
  app.setTenureMonths(req.getTenureMonths());
  app.setPurpose(req.getPurpose());
  app.setStatus(LoanApplicationStatus.DRAFT);
  app.setCreatedAt(LocalDateTime.now());

  repo.save(app);
  saveHistory(app.getId(), LoanApplicationStatus.DRAFT,"Draft created");
  return app;
 }

 public LoanApplication submit(UUID id,String email){

  LoanApplication app = repo.findById(id).orElseThrow();

  if(!app.getUserEmail().equals(email))
   throw new RuntimeException("Unauthorized");

  if(app.getStatus()!=LoanApplicationStatus.DRAFT)
   throw new RuntimeException("Invalid state");

  app.setStatus(LoanApplicationStatus.DOCS_PENDING);
  app.setSubmittedAt(LocalDateTime.now());

  repo.save(app);
  saveHistory(id,LoanApplicationStatus.DOCS_PENDING,"Submitted");

  return app;
 }

 public LoanApplication moveToReview(UUID id){
	 
	 boolean docsUploaded = true; // TEMP MOCK

	 if(!docsUploaded)
	  throw new RuntimeException("Docs missing");
//  if(!docClient.documentsUploaded(id))
//   throw new RuntimeException("Docs missing");

  LoanApplication app = repo.findById(id).orElseThrow();

  if(app.getStatus()!=LoanApplicationStatus.DOCS_PENDING)
   throw new RuntimeException("Invalid state");

  app.setStatus(LoanApplicationStatus.UNDER_REVIEW);
  repo.save(app);

  saveHistory(id,LoanApplicationStatus.UNDER_REVIEW,"Admin verified docs");

  return app;
 }

 public LoanApplication decision(UUID id, AdminDecisionRequest req){

  LoanApplication app = repo.findById(id).orElseThrow();

  if(app.getStatus()!=LoanApplicationStatus.UNDER_REVIEW)
   throw new RuntimeException("Invalid state");

  app.setStatus(req.getStatus());
  app.setDecisionAt(LocalDateTime.now());

  repo.save(app);

  saveHistory(id,req.getStatus(),req.getRemark());
  publisher.publishDecision(app,req.getRemark());

  return app;
 }

 private void saveHistory(UUID id, LoanApplicationStatus status, String remark){

  LoanStatusHistory h = new LoanStatusHistory();
  h.setApplicationId(id);
  h.setStatus(status);
  h.setRemark(remark);
  h.setTimestamp(LocalDateTime.now());

  historyRepo.save(h);
 }

}
