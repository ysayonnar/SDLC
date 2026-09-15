package by.bsuir.morse.model;

public enum Alphabet {
    RUSSIAN("Русский"),
    LATIN("Латинский");

    private final String displayName;

    Alphabet(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
