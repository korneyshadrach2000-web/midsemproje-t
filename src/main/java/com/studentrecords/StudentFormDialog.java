package com.studentrecords;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for adding or editing a student.
 */
public class StudentFormDialog extends JDialog {
    private final JTextField idField;
    private final JTextField firstNameField;
    private final JTextField lastNameField;
    private final JTextField emailField;
    private final JComboBox<String> programCombo;
    private final JSpinner yearSpinner;
    private final JTextField gpaField;
    private final JTextArea notesArea;
    private final boolean isEdit;
    private final String currentId;
    private Student result;

    public StudentFormDialog(Frame owner, String title, boolean isEdit, Student initial, String[] programSuggestions) {
        super(owner, title, true);
        this.isEdit = isEdit;
        this.currentId = initial != null ? initial.getId() : null;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        p.add(new JLabel("Student ID *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        idField = new JTextField(20);
        if (isEdit && initial != null) idField.setEditable(false);
        p.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        p.add(new JLabel("First name *"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        p.add(firstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        p.add(new JLabel("Last name *"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        p.add(lastNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        p.add(new JLabel("Email *"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        p.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        p.add(new JLabel("Program *"), gbc);
        gbc.gridx = 1;
        programCombo = new JComboBox<>(programSuggestions);
        programCombo.setEditable(true);
        p.add(programCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        p.add(new JLabel("Year (1–6) *"), gbc);
        gbc.gridx = 1;
        yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        p.add(yearSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        p.add(new JLabel("GPA (0–4)"), gbc);
        gbc.gridx = 1;
        gpaField = new JTextField(8);
        p.add(gpaField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Notes"), gbc);
        gbc.gridx = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        p.add(new JScrollPane(notesArea), gbc);

        if (initial != null) {
            idField.setText(initial.getId());
            firstNameField.setText(initial.getFirstName());
            lastNameField.setText(initial.getLastName());
            emailField.setText(initial.getEmail());
            programCombo.setSelectedItem(initial.getProgram());
            yearSpinner.setValue(initial.getYear());
            gpaField.setText(initial.getGpa() != null ? String.valueOf(initial.getGpa()) : "");
            notesArea.setText(initial.getNotes());
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> onSave());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        try {
            Object prog = programCombo.getSelectedItem();
            String program = prog != null ? prog.toString().trim() : "";
            result = Validation.validateStudent(
                idField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                program,
                yearSpinner.getValue().toString(),
                gpaField.getText(),
                notesArea.getText(),
                isEdit,
                currentId
            );
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Student getResult() {
        return result;
    }
}
