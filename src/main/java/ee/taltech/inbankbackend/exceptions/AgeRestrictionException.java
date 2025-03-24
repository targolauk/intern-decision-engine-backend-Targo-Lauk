package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when age restriction comes as problem
 */

public class AgeRestrictionException extends Exception {
    public AgeRestrictionException(String message) {
        super(message);
    }
}
