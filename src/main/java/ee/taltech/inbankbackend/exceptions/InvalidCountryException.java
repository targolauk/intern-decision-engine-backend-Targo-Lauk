package ee.taltech.inbankbackend.exceptions;

public class InvalidCountryException extends Exception {
    public InvalidCountryException(String message) {
        super(message);
    }
}
