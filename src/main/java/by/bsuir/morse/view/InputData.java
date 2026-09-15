package by.bsuir.morse.view;

import by.bsuir.morse.model.Alphabet;

public final class InputData {
    private final String encodedText;
    private final Alphabet alphabet;

    public InputData(String encodedText, Alphabet alphabet) {
        this.encodedText = encodedText;
        this.alphabet = alphabet;
    }

    public String getEncodedText() {
        return encodedText;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }
}
