Feature: Account Creation
  As a bank system user
  I want to create new bank accounts
  So that customers can store their money

  Scenario: Successfully create a new account with initial balance
    Given the account number "NEWACC001" does not exist
    When a new account is created for owner "Midou Ben" with account number "NEWACC001" and initial balance 1000.00
    Then an account with number "NEWACC001" should exist
    And the account "NEWACC001" should have owner "Midou Ben"
    And the account "NEWACC001" should have a balance of 1000.00

  Scenario: Successfully create a new account with zero initial balance
    Given the account number "NEWACC002" does not exist
    When a new account is created for owner "Bob Dylan" with account number "NEWACC002" and initial balance 0.00
    Then an account with number "NEWACC002" should exist
    And the account "NEWACC002" should have a balance of 0.00

  Scenario: Attempt to create an account with an existing account number
    Given an account with number "EXISTING001" and owner "Bob Marley" and balance 500.00 already exists
    When an attempt is made to create a new account for owner "David Copperfield" with account number "EXISTING001" and initial balance 200.00
    Then the account creation should fail with message "Account number 'EXISTING001' already exists."

  Scenario: Attempt to create an account with a negative initial balance
    Given the account number "NEGACC003" does not exist
    When an attempt is made to create a new account for owner "Bob Sinclar" with account number "NEGACC003" and initial balance -100.00
    Then the account creation should fail with message "Initial balance cannot be negative."