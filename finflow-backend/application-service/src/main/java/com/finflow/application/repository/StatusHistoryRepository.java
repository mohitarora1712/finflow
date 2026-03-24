package com.finflow.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.application.entity.LoanStatusHistory;

public interface StatusHistoryRepository
extends JpaRepository<LoanStatusHistory, UUID> {}
