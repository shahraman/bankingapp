package com.midou.ben.bankingapp.bdd.stepdefinitions;

import com.midou.ben.bankingapp.model.Account;
import com.midou.ben.bankingapp.repository.AccountRepository;
import com.midou.ben.bankingapp.service.AccountService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class AccountStepDefinitions {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository; // For direct setup/cleanup if needed

    private Account lastCreatedAccount;
    private Account lastRetrievedAccount;
    private Exception lastException;
    private boolean operationSuccessful;


    @Before
    public void setUp() {
        // Clean up database before each scenario to ensure isolation

        accountRepository.deleteAll();
        lastCreatedAccount = null;
        lastRetrievedAccount = null;
        lastException = null;
        operationSuccessful = false; // Default to false
    }

    @After
    public void tearDown() {
    }

    // --- Given Steps ---

    @Given("the account number {string} does not exist")
    public void the_account_number_does_not_exist(String accountNumber) {
        // Ensured by @Before cleanup or we can explicitly check and delete
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        accountOpt.ifPresent(account -> accountRepository.delete(account));
        assertFalse(accountRepository.findByAccountNumber(accountNumber).isPresent(),
                "Account " + accountNumber + " should not exist at this point.");
    }

    @Given("an account with number {string} and owner {string} and balance {double} already exists")
    public void an_account_with_number_and_owner_and_balance_already_exists(String accountNumber, String ownerName, Double balance) {
        // Clean up if it exists from a previous incomplete run, then create
        accountRepository.findByAccountNumber(accountNumber).ifPresent(acc -> accountRepository.delete(acc));
        accountService.createAccount(ownerName, accountNumber, BigDecimal.valueOf(balance));
    }

    // --- When Steps ---

    @When("a new account is created for owner {string} with account number {string} and initial balance {double}")
    public void a_new_account_is_created(String ownerName, String accountNumber, Double initialBalance) {
        try {
            this.lastCreatedAccount = accountService.createAccount(ownerName, accountNumber, BigDecimal.valueOf(initialBalance));
            this.operationSuccessful = true;
        } catch (Exception e) {
            this.lastException = e;
            this.operationSuccessful = false;
        }
    }

    @When("an attempt is made to create a new account for owner {string} with account number {string} and initial balance {double}")
    public void an_attempt_is_made_to_create_new_account(String ownerName, String accountNumber, Double initialBalance) {
        try {
            this.lastCreatedAccount = accountService.createAccount(ownerName, accountNumber, BigDecimal.valueOf(initialBalance));
            this.operationSuccessful = true; // Should not happen if expecting failure
        } catch (Exception e) {
            this.lastException = e;
            this.operationSuccessful = false;
        }
    }

    @When("a deposit of {double} is made to account {string}")
    public void a_deposit_is_made_to_account(Double amount, String accountNumber) {
        try {
            this.lastRetrievedAccount = accountService.deposit(accountNumber, BigDecimal.valueOf(amount));
            this.operationSuccessful = true;
        } catch (Exception e) {
            this.lastException = e;
            this.operationSuccessful = false;
        }
    }

    @When("a withdrawal of {double} is made from account {string}")
    public void a_withdrawal_is_made_from_account(Double amount, String accountNumber) {
        try {
            this.lastRetrievedAccount = accountService.withdraw(accountNumber, BigDecimal.valueOf(amount));
            this.operationSuccessful = true;
        } catch (Exception e) {
            this.lastException = e;
            this.operationSuccessful = false;
        }
    }

    // --- Then Steps ---

    @Then("an account with number {string} should exist")
    public void an_account_with_number_should_exist(String accountNumber) {
        Optional<Account> accountOpt = accountService.getAccountByAccountNumber(accountNumber);
        assertTrue(accountOpt.isPresent(), "Account " + accountNumber + " should exist.");
        this.lastRetrievedAccount = accountOpt.get(); // Store for subsequent assertions
    }

    @Then("the account {string} should have owner {string}")
    public void the_account_should_have_owner(String accountNumber, String expectedOwner) {
        // Retrieve if not already retrieved by a previous step in the same scenario
        if (this.lastRetrievedAccount == null || !this.lastRetrievedAccount.getAccountNumber().equals(accountNumber)) {
            this.lastRetrievedAccount = accountService.getAccountByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AssertionError("Account " + accountNumber + " not found for owner check."));
        }
        assertEquals(expectedOwner, this.lastRetrievedAccount.getOwnerName());
    }

    @Then("the account {string} should have a balance of {double}")
    public void the_account_should_have_a_balance_of(String accountNumber, Double expectedBalance) {
        // Retrieve if not already retrieved
        if (this.lastRetrievedAccount == null || !this.lastRetrievedAccount.getAccountNumber().equals(accountNumber)) {
            this.lastRetrievedAccount = accountService.getAccountByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AssertionError("Account " + accountNumber + " not found for balance check."));
        }
        assertEquals(0, BigDecimal.valueOf(expectedBalance).compareTo(this.lastRetrievedAccount.getBalance()),
                "Balance mismatch for account " + accountNumber);
    }

    @Then("the account {string} should still have a balance of {double}")
    public void the_account_should_still_have_a_balance_of(String accountNumber, Double expectedBalance) {
        // This is essentially the same as the previous "should have a balance of"
        the_account_should_have_a_balance_of(accountNumber, expectedBalance);
    }

    @Then("the account creation should fail with message {string}")
    public void the_account_creation_should_fail_with_message(String expectedMessage) {
        assertFalse(operationSuccessful, "Operation was expected to fail but succeeded.");
        assertNotNull(lastException, "Exception was expected but none was thrown.");
        assertEquals(expectedMessage, lastException.getMessage());
    }

    @Then("the deposit operation should be successful")
    public void the_deposit_operation_should_be_successful() {
        assertTrue(operationSuccessful, "Deposit operation was expected to be successful but failed.");
        assertNull(lastException, "Exception was not expected but was: " + (lastException != null ? lastException.getMessage() : "null"));
    }

    @Then("the deposit operation should fail with message {string}")
    public void the_deposit_operation_should_fail_with_message(String expectedMessage) {
        assertFalse(operationSuccessful, "Deposit operation was expected to fail but succeeded.");
        assertNotNull(lastException, "Exception was expected for deposit but none was thrown.");
        assertEquals(expectedMessage, lastException.getMessage());
    }

    @Then("the withdrawal operation should be successful")
    public void the_withdrawal_operation_should_be_successful() {
        assertTrue(operationSuccessful, "Withdrawal operation was expected to be successful but failed.");
        assertNull(lastException, "Exception was not expected but was: " + (lastException != null ? lastException.getMessage() : "null"));
    }

    @Then("the withdrawal operation should fail with message {string}")
    public void the_withdrawal_operation_should_fail_with_message(String expectedMessage) {
        assertFalse(operationSuccessful, "Withdrawal operation was expected to fail but succeeded.");
        assertNotNull(lastException, "Exception was expected for withdrawal but none was thrown.");
        assertEquals(expectedMessage, lastException.getMessage());
    }
}