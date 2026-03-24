package com.finflow.application.dto;

public class CreateDraftRequest {

	 private Double amount;
	 private Integer tenureMonths;
	 private String purpose;
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

	 // getters setters
	 

	}
