package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.MoneyTransferRequest;
import com.db.awmd.challenge.exception.BadRequestException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  
  @Mock
  private AccountsRepository accountRepository;
  
  @Mock
  private NotificationService notificationService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  @Test
  public void moneyTransferSuccessTest() {
	  this.accountsService.createAccount(new Account("af02", new BigDecimal("20.20")));
	  this.accountsService.createAccount(new Account("af01", new BigDecimal("10.10")));
	  
	  MoneyTransferRequest mtr = new MoneyTransferRequest();
	  mtr.setAccountFromId("af02");
	  mtr.setAccountToId("af01");
	  mtr.setAmount(new BigDecimal("5.10"));
	  
	  when(accountRepository.fulfillMoneyTransfer(Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class))).thenReturn(true);
	  
	  doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Mockito.anyString());
	  ResponseEntity<String> response = accountsService.moneyTransfer(mtr);
	  Assert.assertTrue(response.getStatusCode() == HttpStatus.OK);
  }
  
  @Test(expected = BadRequestException.class)
  public void moneyTransferBadRequestTest() {
	  this.accountsService.createAccount(new Account("afBad02", new BigDecimal("20.20")));
	  this.accountsService.createAccount(new Account("afBad01", new BigDecimal("10.10")));
	  
	  MoneyTransferRequest mtr = new MoneyTransferRequest();
	  mtr.setAccountFromId("afBad02");
	  mtr.setAccountToId("afBad01");
	  mtr.setAmount(new BigDecimal("50.10"));
	  
	  when(accountRepository.fulfillMoneyTransfer(Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class))).thenReturn(true);
	  
	  doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Mockito.anyString());
	  ResponseEntity<String> response = accountsService.moneyTransfer(mtr);
	  Assert.assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST);
  }
  
  @Test(expected = Exception.class)
  public void moneyTransferFailedTest() {
	  this.accountsService.createAccount(new Account("aff02", new BigDecimal("20.20")));
	  this.accountsService.createAccount(new Account("aff01", new BigDecimal("10.10")));
	  
	  MoneyTransferRequest mtr = new MoneyTransferRequest();
	  mtr.setAccountFromId("aff02");
	  mtr.setAccountToId("aff01");
	  mtr.setAmount(new BigDecimal("5.10"));
	  
	  when(accountRepository.fulfillMoneyTransfer(Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class), Mockito.any(Account.class))).thenThrow(Exception.class);
	  
	  doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Mockito.anyString());
	  ResponseEntity<String> response = accountsService.moneyTransfer(mtr);
	  Assert.assertTrue(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
