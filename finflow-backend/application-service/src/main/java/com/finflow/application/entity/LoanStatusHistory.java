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
@Table(name = "loan_status_history")
public class LoanStatusHistory implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private UUID id;

	private UUID applicationId;

	@Enumerated(EnumType.STRING)
	private LoanApplicationStatus status;

	private String remark;
	private LocalDateTime timestamp;

	public LoanStatusHistory() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(UUID applicationId) {
		this.applicationId = applicationId;
	}

	public LoanApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(LoanApplicationStatus status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	// getters setters

}
