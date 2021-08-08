package com.db.awmd.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

@Override
public  boolean fulfillMoneyTransfer(Account accFrom, Account accTo, Account accFromBck, Account accToBck) {
	
	try {
			accounts.replace(accFrom.getAccountId(), accFrom);
			accounts.replace(accTo.getAccountId(), accTo);	
			
	}catch(Exception ex) {
		log.error("Unexpected error occured during money transfer, transaction will be rollbacked.. : "+ex.getMessage());
		
		accounts.replace(accFrom.getAccountId(), accFromBck);
		accounts.replace(accTo.getAccountId(), accToBck);
		return false;
		}
		return true;
	}
}
