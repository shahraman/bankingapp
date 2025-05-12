Feature: Account Transactions
  As a bank system user
  I want to perform deposits and withdrawals on accounts
  So that customers can manage their funds

  Background:
    Given an account with number "TXACC123" and owner "Peter Pan" and balance 1000.00 already exists

  Scenario: Successfully deposit funds into an existing account
    When a deposit of 500.50 is made to account "TXACC123"
    Then the account "TXACC123" should have a balance of 1500.50
    And the deposit operation should be successful

  Scenario: Attempt to deposit a zero amount
    When a deposit of 0.00 is made to account "TXACC123"
    Then the deposit operation should fail with message "Deposit amount must be positive."
    And the account "TXACC123" should still have a balance of 1000.00

  Scenario: Attempt to deposit a negative amount
    When a deposit of -50.00 is made to account "TXACC123"
    Then the deposit operation should fail with message "Deposit amount must be positive."
    And the account "TXACC123" should still have a balance of 1000.00

  Scenario: Successfully withdraw funds from an existing account with sufficient balance
    When a withdrawal of 200.25 is made from account "TXACC123"
    Then the account "TXACC123" should have a balance of 799.75
    And the withdrawal operation should be successful

  Scenario: Attempt to withdraw funds from an account with insufficient balance
    When a withdrawal of 1500.00 is made from account "TXACC123"
    Then the withdrawal operation should fail with message "Insufficient funds in account TXACC123"
    And the account "TXACC123" should still have a balance of 1000.00

  Scenario: Attempt to withdraw a zero amount
    When a withdrawal of 0.00 is made from account "TXACC123"
    Then the withdrawal operation should fail with message "Withdrawal amount must be positive."
    And the account "TXACC123" should still have a balance of 1000.00

  Scenario: Attempt to withdraw a negative amount
    When a withdrawal of -75.00 is made from account "TXACC123"
    Then the withdrawal operation should fail with message "Withdrawal amount must be positive."
    And the account "TXACC123" should still have a balance of 1000.00

  Scenario: Attempt to deposit into a non-existent account
    When a deposit of 100.00 is made to account "NONEXISTENT999"
    Then the deposit operation should fail with message "Account not found with number: NONEXISTENT999"

  Scenario: Attempt to withdraw from a non-existent account
    When a withdrawal of 100.00 is made from account "NONEXISTENT999"
    Then the withdrawal operation should fail with message "Account not found with number: NONEXISTENT999"