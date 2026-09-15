package by.bsuir.morse.view;

import by.bsuir.morse.model.DecoderState;
import by.bsuir.morse.model.MorseDecoderModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class MainView implements PropertyChangeListener {
    private final JFrame frame = new JFrame("Декодер азбуки Морзе");
    private final JButton inputButton = new JButton("Ввести данные");
    private final JLabel alphabetValue = new JLabel("—");
    private final JTextArea sourceValue = createOutputArea();
    private final JTextArea resultValue = createOutputArea();

    public MainView() {
        buildInterface();
    }

    public void showWindow() {
        frame.setVisible(true);
    }

    public JButton getInputButton() {
        return inputButton;
    }

    public InputData requestInput(DecoderState initialState) {
        return new InputDialog(
                frame,
                new InputData(initialState.getEncodedText(), initialState.getAlphabet())
        ).showDialog();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!MorseDecoderModel.STATE_PROPERTY.equals(event.getPropertyName())) {
            return;
        }
        DecoderState state = (DecoderState) event.getNewValue();
        alphabetValue.setText(state.getAlphabet().toString());
        sourceValue.setText(state.getEncodedText());
        resultValue.setText(state.getDecodedText());
        resultValue.setCaretPosition(0);
    }

    private void buildInterface() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(620, 430));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Декодер азбуки Морзе");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.add(title, BorderLayout.WEST);
        top.add(inputButton, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        JPanel values = new JPanel(new BorderLayout(0, 12));
        JPanel alphabetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alphabetPanel.add(new JLabel("Выбранный алфавит:  "));
        alphabetPanel.add(alphabetValue);
        values.add(alphabetPanel, BorderLayout.NORTH);

        JPanel textPanels = new JPanel(new GridLayout(2, 1, 0, 12));
        textPanels.add(createLabeledArea("Последние введенные сигналы:", sourceValue));
        resultValue.setFont(resultValue.getFont().deriveFont(Font.BOLD, 17f));
        textPanels.add(createLabeledArea("Результат декодирования:", resultValue));
        values.add(textPanels, BorderLayout.CENTER);
        root.add(values, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.pack();
        frame.setSize(Math.max(frame.getWidth(), 680), Math.max(frame.getHeight(), 480));
        frame.setLocationRelativeTo(null);
    }

    private static JTextArea createOutputArea() {
        JTextArea area = new JTextArea(4, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(248, 248, 248));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private static JPanel createLabeledArea(String label, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }
}
