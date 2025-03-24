package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    @InjectMocks
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12, "Estonia"));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException, InvalidCountryException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12, "Estonia");
        assertEquals(2000, decision.getLoanAmount());
        assertEquals(20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException, InvalidCountryException  {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12, "Estonia");
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException, InvalidCountryException  {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12, "Latvia");
        assertEquals(10000, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12, "Estonia"));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12, "Estonia"));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12, "Estonia"));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod, "Estonia"));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod, "Estonia"));
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException, InvalidCountryException  {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12, "Estonia");
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 60, "Estonia"));
    }

    @Test
    void testInvalidCountry() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, AgeRestrictionException, InvalidCountryException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12, "asdf");
        assertEquals(null, decision.getLoanAmount());
        assertEquals(null, decision.getLoanPeriod());
        assertEquals("We dont offer loans in your country yet!", decision.getErrorMessage());
    }

    @Test
    void testUserFarTooOld() {
        Exception ex = assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.testAgeCheck("10000000000","Estonia"));
        assertEquals("You are too old to be alive!", ex.getMessage());
    }

    @Test
    void userTooYoungEdgeCase() {
        Exception ex = assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.testAgeCheck("50704260021","Estonia"));
        assertEquals("You are too young to take a loan!",ex.getMessage());
    }
    @Test
    void userInEarlyEighteen() throws AgeRestrictionException {
        Assertions.assertDoesNotThrow(() -> decisionEngine.testAgeCheck("60703240021","Estonia"));
    }

    @Test
    void userTooOld() {
        Exception ex = assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.testAgeCheck("34803100045","Estonia"));
        assertEquals("You are too old to take a loan!", ex.getMessage());
    }

    @Test
    void userTooOldEdgeCase() {
        Exception ex = assertThrows(AgeRestrictionException.class,
                () -> decisionEngine.testAgeCheck("45103030051","Estonia"));
        assertEquals("You are too old to take a loan!", ex.getMessage());
    }

    @Test
    void userExactlyOldEnoughToGetLoan() throws AgeRestrictionException {
        Assertions.assertDoesNotThrow(() -> decisionEngine.testAgeCheck("35104250051","Estonia"));
    }
}

