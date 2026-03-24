package com.finflow.application.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_applications")
public class LoanApplication implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	private UUID id;

	private String userEmail;
	private Double amount;
	private Integer tenureMonths;
	private String purpose;

	@Enumerated(EnumType.STRING)
	private LoanApplicationStatus status;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime submittedAt;
	private LocalDateTime decisionAt;

	public LoanApplication() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Integer getTenureMonths() {
		return tenureMonths;
	}

	public void setTenureMonths(Integer tenureMonths) {
		this.tenureMonths = tenureMonths;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public LoanApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(LoanApplicationStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public LocalDateTime getDecisionAt() {
		return decisionAt;
	}

	public void setDecisionAt(LocalDateTime decisionAt) {
		this.decisionAt = decisionAt;
	}

	// ALL getters setters manually

}
