package com.db.awmd.challenge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.BadRequestException;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  @Getter
  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }	

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  
  public synchronized ResponseEntity<String> moneyTransfer(MoneyTransferRequest moneyTransferReq) {
	  
	  Account accFrom = this.accountsRepository.getAccount(moneyTransferReq.getAccountFromId());
	  
	  validateMoneyTransferRequest(moneyTransferReq, accFrom);
	  
	  Account accTo = this.accountsRepository.getAccount(moneyTransferReq.getAccountToId());
	  
	  Account accFromBck = createBachupAccount(accFrom);
	  Account accToBck = createBachupAccount(accTo);
	  
	  accTo.setBalance(accTo.getBalance().add(moneyTransferReq.getAmount()));
	  accFrom.setBalance(accFrom.getBalance().subtract(moneyTransferReq.getAmount()));
	  
	  if(this.accountsRepository.fulfillMoneyTransfer(accFrom, accTo, accFromBck, accToBck)) {  
		  
		  sendTransferNotification(accFromBck, accToBck, moneyTransferReq);
		  return new ResponseEntity<>("Congratulation!! Money transferred successfully..", HttpStatus.OK);
	  }
	  else
		  return new ResponseEntity<>("Money transfer failed due to unexpected error. Please try again..", HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  private Account createBachupAccount(Account account) {
	  
	  return new Account(account.getAccountId(), account.getBalance());
  }
  
  
  private void sendTransferNotification(Account accFrom, Account accTo, MoneyTransferRequest moneyTransferReq) {
	  try {
		  // Logging notification message for review purpose only
		  
		  log.info("INR "+moneyTransferReq.getAmount()+" has been transferred to account id "+moneyTransferReq.getAccountToId());
		  log.info("INR "+moneyTransferReq.getAmount()+" has been received from account id "+moneyTransferReq.getAccountFromId());
		  
		  notificationService.notifyAboutTransfer(accFrom, "INR "+moneyTransferReq.getAmount()+" has been transferred to account id "+moneyTransferReq.getAccountToId());
		  notificationService.notifyAboutTransfer(accTo, "INR "+moneyTransferReq.getAmount()+" has been received from account id "+moneyTransferReq.getAccountFromId());
	  }catch(Exception e) {
		  log.info("unimplemented notification service handling");
	  }
  }
  
  
  private void validateMoneyTransferRequest(MoneyTransferRequest moneyTransferReq, Account accFrom) {
	  	  
	  if(moneyTransferReq.getAccountFromId().equals(moneyTransferReq.getAccountToId()))
		  throw new BadRequestException("AccountIdFrom and AccountIdTo must not be same");
		  
	  if(accFrom.getBalance().compareTo(moneyTransferReq.getAmount()) < 0)	
		  throw new BadRequestException("Account id " + accFrom.getAccountId() + " doesn't have sufficient balance for this transfer request.");
	  
  }
}
