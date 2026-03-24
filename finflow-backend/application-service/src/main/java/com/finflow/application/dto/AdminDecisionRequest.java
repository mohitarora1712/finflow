package com.finflow.application.dto;

import com.finflow.application.entity.LoanApplicationStatus;

public class AdminDecisionRequest {

	 private LoanApplicationStatus status;
	 private String remark;
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

	 // getters setters
	 

	}
