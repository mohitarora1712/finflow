package com.finflow.admin.dto;

import java.util.UUID;

public class LoanApplicationResponse {

	 private UUID id;
	 private String userEmail;
	 private String status;
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
	 public String getStatus() {
		 return status;
	 }
	 public void setStatus(String status) {
		 this.status = status;
	 }

	 // getters setters
	 
	}
