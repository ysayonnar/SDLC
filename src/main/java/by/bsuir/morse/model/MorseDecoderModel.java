package by.bsuir.morse.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Активная модель: хранит состояние, выполняет декодирование и уведомляет
 * подписанные представления после успешного изменения данных.
 */
public final class MorseDecoderModel {
    public static final String STATE_PROPERTY = "state";

    private static final Map<String, String> LATIN_CODES = createLatinCodes();
    private static final Map<String, String> RUSSIAN_CODES = createRussianCodes();

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private DecoderState state = DecoderState.empty();

    public DecoderState getState() {
        return state;
    }

    public void decode(String source, Alphabet alphabet) throws InvalidMorseCodeException {
        if (source == null || source.trim().isEmpty()) {
            throw new InvalidMorseCodeException("Введите хотя бы один сигнал азбуки Морзе.");
        }
        if (alphabet == null) {
            throw new InvalidMorseCodeException("Выберите алфавит.");
        }

        String normalized = normalize(source);
        if (!normalized.matches("[.\\- /\\t\\r\\n]+")) {
            throw new InvalidMorseCodeException(
                    "Допустимы только точки, дефисы, пробелы, переносы строк и символ ‘/’."
            );
        }

        Map<String, String> codes = alphabet == Alphabet.RUSSIAN ? RUSSIAN_CODES : LATIN_CODES;
        String decoded = decodeNormalized(normalized, codes);
        DecoderState oldState = state;
        state = new DecoderState(source, alphabet, decoded);
        changes.firePropertyChange(STATE_PROPERTY, oldState, state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    private static String normalize(String source) {
        return source.trim()
                .replace('\u2212', '-')
                .replace('\u2013', '-')
                .replace('\u2014', '-');
    }

    private static String decodeNormalized(String source, Map<String, String> codes)
            throws InvalidMorseCodeException {
        String withWordSeparators = source
                .replaceAll("\\s*\\R\\s*", " / ")
                .replaceAll("[ \\t]{2,}", " / ");
        String[] words = withWordSeparators.split("/", -1);
        StringBuilder result = new StringBuilder();

        for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String word = words[wordIndex].trim();
            if (word.isEmpty()) {
                throw new InvalidMorseCodeException(
                        "Между разделителями слов отсутствуют сигналы."
                );
            }
            if (wordIndex > 0) {
                result.append(' ');
            }

            String[] signals = word.split("[ \\t]+");
            for (int signalIndex = 0; signalIndex < signals.length; signalIndex++) {
                String symbol = codes.get(signals[signalIndex]);
                if (symbol == null) {
                    throw new InvalidMorseCodeException(String.format(
                            "Неизвестный сигнал «%s» (слово %d, символ %d).",
                            signals[signalIndex], wordIndex + 1, signalIndex + 1
                    ));
                }
                result.append(symbol);
            }
        }
        return result.toString();
    }

    private static Map<String, String> createLatinCodes() {
        Map<String, String> codes = createCommonCodes();
        add(codes, "A", ".-"); add(codes, "B", "-..."); add(codes, "C", "-.-.");
        add(codes, "D", "-.."); add(codes, "E", "."); add(codes, "F", "..-.");
        add(codes, "G", "--."); add(codes, "H", "...."); add(codes, "I", "..");
        add(codes, "J", ".---"); add(codes, "K", "-.-"); add(codes, "L", ".-..");
        add(codes, "M", "--"); add(codes, "N", "-."); add(codes, "O", "---");
        add(codes, "P", ".--."); add(codes, "Q", "--.-"); add(codes, "R", ".-.");
        add(codes, "S", "..."); add(codes, "T", "-"); add(codes, "U", "..-");
        add(codes, "V", "...-"); add(codes, "W", ".--"); add(codes, "X", "-..-");
        add(codes, "Y", "-.--"); add(codes, "Z", "--..");
        return Collections.unmodifiableMap(codes);
    }

    private static Map<String, String> createRussianCodes() {
        Map<String, String> codes = createCommonCodes();
        add(codes, "А", ".-"); add(codes, "Б", "-..."); add(codes, "В", ".--");
        add(codes, "Г", "--."); add(codes, "Д", "-.."); add(codes, "Е", ".");
        add(codes, "Ж", "...-"); add(codes, "З", "--.."); add(codes, "И", "..");
        add(codes, "Й", ".---"); add(codes, "К", "-.-"); add(codes, "Л", ".-..");
        add(codes, "М", "--"); add(codes, "Н", "-."); add(codes, "О", "---");
        add(codes, "П", ".--."); add(codes, "Р", ".-."); add(codes, "С", "...");
        add(codes, "Т", "-"); add(codes, "У", "..-"); add(codes, "Ф", "..-.");
        add(codes, "Х", "...."); add(codes, "Ц", "-.-."); add(codes, "Ч", "---.");
        add(codes, "Ш", "----"); add(codes, "Щ", "--.-"); add(codes, "Ъ", "--.--");
        add(codes, "Ы", "-.--"); add(codes, "Ь", "-..-"); add(codes, "Э", "..-..");
        add(codes, "Ю", "..--"); add(codes, "Я", ".-.-");
        return Collections.unmodifiableMap(codes);
    }

    private static Map<String, String> createCommonCodes() {
        Map<String, String> codes = new HashMap<>();
        add(codes, "0", "-----"); add(codes, "1", ".----"); add(codes, "2", "..---");
        add(codes, "3", "...--"); add(codes, "4", "....-"); add(codes, "5", ".....");
        add(codes, "6", "-...."); add(codes, "7", "--..."); add(codes, "8", "---..");
        add(codes, "9", "----."); add(codes, ".", ".-.-.-"); add(codes, ",", "--..--");
        add(codes, "?", "..--.."); add(codes, "!", "-.-.--"); add(codes, ":", "---...");
        add(codes, ";", "-.-.-."); add(codes, "(", "-.--."); add(codes, ")", "-.--.-");
        add(codes, "'", ".----."); add(codes, "\"", ".-..-."); add(codes, "-", "-....-");
        add(codes, "/", "-..-."); add(codes, "=", "-...-"); add(codes, "+", ".-.-.");
        add(codes, "@", ".--.-.");
        return codes;
    }

    private static void add(Map<String, String> codes, String symbol, String signal) {
        codes.put(signal, symbol);
    }
}
