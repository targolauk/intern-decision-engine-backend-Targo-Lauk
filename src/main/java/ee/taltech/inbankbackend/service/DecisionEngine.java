package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod, String country)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, AgeRestrictionException, InvalidCountryException {
        try {
            verifyInputs(personalCode, loanAmount, loanPeriod, country);
            ageCheck(personalCode, country);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        int outputLoanAmount;
        creditModifier = getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        while (highestValidLoanAmount(loanPeriod) < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
            loanPeriod++;
        }

        if (loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, highestValidLoanAmount(loanPeriod));
        } else {
            throw new NoValidLoanException("No valid loan found!");
        }


        return new Decision(outputLoanAmount, loanPeriod, null);
    }

    /**
     * Method that makes all the age restriction checks.
     *
     *
     * @param personalCode Personal ID code.
     * @param country Country the user is residence of.
     * @throws AgeRestrictionException If age of the user is too old or too young!
     */
    private void ageCheck(String personalCode, String country) throws AgeRestrictionException {
        int userAge = getAge(personalCode);

        int lifeExpectancy = DecisionEngineConstants.COUNTRY_LIFE_EXCPECTANCY.get(country);

        int upperCheckForAge = lifeExpectancy - (DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12);

        if (userAge < DecisionEngineConstants.MINIMUM_AGE) {
            throw new AgeRestrictionException("You are too young to take a loan!");
        }
        if (userAge > upperCheckForAge) {
            throw new AgeRestrictionException("You are too old to take a loan!");
        }
    }

    /**
     * Test method for age check, so no need to use real ID codes for the tests.
     *
     * @param personalCode Personal ID code.
     * @param country Country the user is residence of.
     * @throws AgeRestrictionException If age of the user is too old or too young!
     */
    public void testAgeCheck(String personalCode, String country) throws AgeRestrictionException {
        ageCheck(personalCode, country);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private int highestValidLoanAmount(int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws InvalidCountryException If the country is invalid.
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod, String country)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException, InvalidCountryException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }
        if (country == null || !(country.equals("Estonia") || country.equals("Latvia") || country.equals("Lithuania"))) {
            throw new InvalidCountryException("We dont offer loans in your country yet!");
        }


    }

    /**
     * Return age of the user in years.
     * If ID code starts with 1 or 2, throws exception
     *
     * @param personalCode Provided personal ID code
     * @return Age of the user.
     * @throws AgeRestrictionException If the ID code starts with 1 or 2.
     */
    private Integer getAge(String personalCode) throws AgeRestrictionException {

        int year = Integer.parseInt(personalCode.substring(1,3));
        int month = Integer.parseInt(personalCode.substring(3,5));
        int day = Integer.parseInt(personalCode.substring(5,7));
        String firstNumber = personalCode.substring(0, 1);


        if (firstNumber.equals("1") || firstNumber.equals("2")) {
            throw new AgeRestrictionException("You are too old to be alive!");
        }
        else if (firstNumber.equals("3") || firstNumber.equals("4")) {
            year += 1900;
        } else if (firstNumber.equals("5") || firstNumber.equals("6")) {
            year += 2000;
        }
        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate todayDate = LocalDate.now();

        return Period.between(birthDate, todayDate).getYears();
    }
}
