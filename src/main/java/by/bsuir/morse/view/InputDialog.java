package by.bsuir.morse.view;

import by.bsuir.morse.model.Alphabet;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

public final class InputDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JTextArea inputArea = new JTextArea(7, 44);
    private final JComboBox<Alphabet> alphabetBox = new JComboBox<>(Alphabet.values());
    private boolean accepted;

    public InputDialog(Frame owner, InputData initialData) {
        super(owner, "Ввод сигналов", Dialog.ModalityType.APPLICATION_MODAL);
        buildInterface(initialData);
    }

    public InputData showDialog() {
        setVisible(true);
        if (!accepted) {
            return null;
        }
        return new InputData(inputArea.getText(), (Alphabet) alphabetBox.getSelectedItem());
    }

    private void buildInterface(InputData initialData) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        inputArea.setText(initialData.getEncodedText());
        inputArea.selectAll();
        alphabetBox.setSelectedItem(initialData.getAlphabet());

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 5));
        header.add(new JLabel("Введите код Морзе:"));
        header.add(new JLabel("Один пробел — между буквами, / или несколько пробелов — между словами."));
        content.add(header, BorderLayout.NORTH);
        content.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel alphabetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alphabetPanel.add(new JLabel("Алфавит:  "));
        alphabetPanel.add(alphabetBox);
        bottom.add(alphabetPanel, BorderLayout.WEST);

        JButton okButton = new JButton("Декодировать");
        JButton cancelButton = new JButton("Отмена");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancelButton);
        buttons.add(okButton);
        bottom.add(buttons, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);

        okButton.addActionListener(event -> {
            accepted = true;
            dispose();
        });
        cancelButton.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(okButton);

        setContentPane(content);
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }
}
