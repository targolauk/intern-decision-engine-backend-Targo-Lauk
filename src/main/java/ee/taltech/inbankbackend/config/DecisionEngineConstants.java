package ee.taltech.inbankbackend.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all necessary constants for the decision engine.
 */
public class DecisionEngineConstants {
    public static final Integer MINIMUM_LOAN_AMOUNT = 2000;
    public static final Integer MAXIMUM_LOAN_AMOUNT = 10000;
    public static final Integer MAXIMUM_LOAN_PERIOD = 60;
    public static final Integer MINIMUM_LOAN_PERIOD = 12;
    public static final Integer SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final Integer SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final Integer SEGMENT_3_CREDIT_MODIFIER = 1000;
    public static final Integer MINIMUM_AGE = 18;
    public static final Map<String, Integer> COUNTRY_LIFE_EXCPECTANCY = Map.of(
            "Estonia", 78,
            "Latvia", 90,
            "Lithuania", 76
    );
}
