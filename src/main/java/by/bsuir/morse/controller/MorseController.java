package by.bsuir.morse.controller;

import by.bsuir.morse.model.DecoderState;
import by.bsuir.morse.model.InvalidMorseCodeException;
import by.bsuir.morse.model.MorseDecoderModel;
import by.bsuir.morse.view.InputData;
import by.bsuir.morse.view.MainView;

/** Связывает действия пользователя с активной моделью. */
public final class MorseController {
    private final MorseDecoderModel model;
    private final MainView view;

    public MorseController(MorseDecoderModel model, MainView view) {
        this.model = model;
        this.view = view;
        model.addPropertyChangeListener(view);
        view.getInputButton().addActionListener(event -> enterData());
    }

    private void enterData() {
        DecoderState savedState = model.getState();
        InputData draft = new InputData(savedState.getEncodedText(), savedState.getAlphabet());

        while (true) {
            InputData input = view.requestInput(
                    new DecoderState(draft.getEncodedText(), draft.getAlphabet(), "")
            );
            if (input == null) {
                return;
            }

            draft = input;
            try {
                model.decode(input.getEncodedText(), input.getAlphabet());
                return;
            } catch (InvalidMorseCodeException exception) {
                view.showError(exception.getMessage());
            }
        }
    }
}
