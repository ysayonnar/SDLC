package by.bsuir.morse.model;

public final class InvalidMorseCodeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidMorseCodeException(String message) {
        super(message);
    }
}
