package com.finflow.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.application.entity.LoanApplication;
import com.finflow.application.entity.LoanApplicationStatus;

public interface LoanApplicationRepository
extends JpaRepository<LoanApplication, UUID> {

List<LoanApplication> findByUserEmail(String email);
List<LoanApplication> findByStatus(LoanApplicationStatus status);

}