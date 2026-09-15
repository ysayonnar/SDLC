package by.bsuir.morse.model;

import java.util.Objects;

public final class DecoderState {
    private final String encodedText;
    private final Alphabet alphabet;
    private final String decodedText;

    public DecoderState(String encodedText, Alphabet alphabet, String decodedText) {
        this.encodedText = Objects.requireNonNull(encodedText);
        this.alphabet = Objects.requireNonNull(alphabet);
        this.decodedText = Objects.requireNonNull(decodedText);
    }

    public static DecoderState empty() {
        return new DecoderState("", Alphabet.RUSSIAN, "");
    }

    public String getEncodedText() {
        return encodedText;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public String getDecodedText() {
        return decodedText;
    }
}
