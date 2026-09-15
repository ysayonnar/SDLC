package by.bsuir.morse;

import by.bsuir.morse.controller.MorseController;
import by.bsuir.morse.model.MorseDecoderModel;
import by.bsuir.morse.view.MainView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();

            MorseDecoderModel model = new MorseDecoderModel();
            MainView view = new MainView();
            new MorseController(model, view);
            view.showWindow();
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Стандартная тема Swing остается полностью работоспособной.
        }
    }
}
