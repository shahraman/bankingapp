package com.midou.ben.bankingapp;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;


@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/cucumber-reports/Cucumber.json")
// Specifies the package where step definitions are located
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.midou.ben.bankingapp.bdd.stepdefinitions")
public class BankingApplicationCucumberTest {
}