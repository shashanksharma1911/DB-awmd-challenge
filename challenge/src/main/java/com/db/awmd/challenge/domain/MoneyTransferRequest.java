package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MoneyTransferRequest {
	
	@NotEmpty
	private String accountFromId;
	
	@NotEmpty
	private String accountToId;
	
	@NotNull
	@Min(value = 0, message = "Amount must be positive number")
	private BigDecimal amount;

}
