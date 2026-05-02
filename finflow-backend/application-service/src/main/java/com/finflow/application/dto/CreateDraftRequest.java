package com.finflow.application.dto;

import jakarta.validation.constraints.*;

public class CreateDraftRequest {

    // ✅ MANDATORY
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Tenure is required")
    private Integer tenureMonths;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    // 👤 PERSONAL (optional)
    private String fullName;
    private String phone;

    // 💼 EMPLOYMENT (optional)
    private String employmentType;
    private String companyName;

    // 💰 FINANCIAL (optional)
    private Double income;

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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Double getIncome() {
		return income;
	}

	public void setIncome(Double income) {
		this.income = income;
	}
    
    
    
    // ===== getters & setters =====
}