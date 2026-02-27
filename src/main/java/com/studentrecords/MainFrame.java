package com.studentrecords;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

/**
 * Main application window: student list, filters, add/edit/delete, export and report.
 */
public class MainFrame extends JFrame {
    private final Database db;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final JComboBox<String> programFilter;
    private final JComboBox<String> yearFilter;
    private final JTextField searchField;

    private static final String[] COLUMNS = { "Student ID", "Name", "Email", "Program", "Year", "GPA" };

    public MainFrame() throws SQLException {
        db = new Database();
        db.init();

        setTitle("Student Records Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 520);
        setMinimumSize(new Dimension(700, 400));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(new JButton(new AbstractAction("Add Student") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addStudent();
            }
        }));
        toolBar.add(new JButton(new AbstractAction("Edit") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editStudent();
            }
        }));
        toolBar.add(new JButton(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteStudent();
            }
        }));
        toolBar.addSeparator();

        toolBar.add(new JLabel(" Program: "));
        programFilter = new JComboBox<>(new String[]{""});
        programFilter.setEditable(false);
        programFilter.addActionListener(e -> loadStudents());
        toolBar.add(programFilter);

        toolBar.add(new JLabel(" Year: "));
        yearFilter = new JComboBox<>(new String[]{"", "1", "2", "3", "4", "5", "6"});
        yearFilter.addActionListener(e -> loadStudents());
        toolBar.add(yearFilter);

        toolBar.add(new JLabel(" Search: "));
        searchField = new JTextField(14);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadStudents(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadStudents(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadStudents(); }
        });
        toolBar.add(searchField);

        toolBar.addSeparator();
        toolBar.add(new JButton(new AbstractAction("Export CSV…") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportCsv();
            }
        }));
        toolBar.add(new JButton(new AbstractAction("Summary Report…") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                summaryReport();
            }
        }));

        statusLabel = new JLabel("Ready. Add students or use filters.");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        refreshProgramFilter();
        loadStudents();
    }

    private String[] programSuggestions() {
        try {
            List<String> fromDb = db.getPrograms();
            String[] defaults = {"", "CS", "ECE", "MATH", "ENG", "BIO", "PHYS"};
            java.util.Set<String> set = new java.util.LinkedHashSet<>();
            for (String d : defaults) set.add(d);
            for (String p : fromDb) set.add(p);
            return set.toArray(new String[0]);
        } catch (SQLException e) {
            return new String[]{"", "CS", "ECE", "MATH", "ENG"};
        }
    }

    private void refreshProgramFilter() {
        try {
            List<String> programs = db.getPrograms();
            String[] items = new String[programs.size() + 1];
            items[0] = "";
            for (int i = 0; i < programs.size(); i++) items[i + 1] = programs.get(i);
            programFilter.setModel(new DefaultComboBoxModel<>(items));
        } catch (SQLException ignored) {}
    }

    private void loadStudents() {
        String pf = programFilter.getSelectedItem() != null ? programFilter.getSelectedItem().toString().trim() : "";
        if (pf.isEmpty()) pf = null;
        String yStr = yearFilter.getSelectedItem() != null ? yearFilter.getSelectedItem().toString().trim() : "";
        Integer yf = null;
        if (!yStr.isEmpty()) {
            try { yf = Integer.parseInt(yStr); } catch (NumberFormatException ignored) {}
        }
        String search = searchField.getText() != null ? searchField.getText().trim() : null;
        if (search != null && search.isEmpty()) search = null;

        try {
            List<Student> students = db.listStudents(pf, yf, search);
            tableModel.setRowCount(0);
            for (Student s : students) {
                String gpa = s.getGpa() != null ? String.format("%.2f", s.getGpa()) : "";
                tableModel.addRow(new Object[]{
                    s.getId(), s.getFullName(), s.getEmail(), s.getProgram(), s.getYear(), gpa
                });
            }
            statusLabel.setText("Showing " + students.size() + " student(s).");
        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStudent() {
        StudentFormDialog d = new StudentFormDialog(
            this, "Add Student", false, null, programSuggestions()
        );
        d.setVisible(true);
        Student s = d.getResult();
        if (s != null) {
            try {
                db.addStudent(s);
                refreshProgramFilter();
                loadStudents();
                statusLabel.setText("Added student " + s.getId() + ".");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student to edit.", "Edit", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        try {
            Student s = db.getStudent(id);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StudentFormDialog d = new StudentFormDialog(this, "Edit Student", true, s, programSuggestions());
            d.setVisible(true);
            Student updated = d.getResult();
            if (updated != null) {
                db.updateStudent(id, updated);
                refreshProgramFilter();
                loadStudents();
                statusLabel.setText("Updated student " + updated.getId() + ".");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student to delete.", "Delete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete student '" + id + "'?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            db.deleteStudent(id);
            refreshProgramFilter();
            loadStudents();
            statusLabel.setText("Deleted " + id + ".");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("students_export.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (f == null) return;
        String pf = programFilter.getSelectedItem() != null ? programFilter.getSelectedItem().toString().trim() : null;
        if (pf != null && pf.isEmpty()) pf = null;
        String yStr = yearFilter.getSelectedItem() != null ? yearFilter.getSelectedItem().toString() : "";
        Integer yf = null;
        try { if (!yStr.isEmpty()) yf = Integer.parseInt(yStr); } catch (NumberFormatException ignored) {}
        String search = searchField.getText() != null ? searchField.getText().trim() : null;
        if (search != null && search.isEmpty()) search = null;
        try {
            int n = Reports.exportCsv(Paths.get(f.getAbsolutePath()), pf, yf, search);
            statusLabel.setText("Exported " + n + " record(s) to " + f.getPath());
            JOptionPane.showMessageDialog(this, "Exported " + n + " record(s) to CSV.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void summaryReport() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("student_summary.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (f == null) return;
        try {
            String content = Reports.generateSummaryReport(Paths.get(f.getAbsolutePath()));
            statusLabel.setText("Summary report saved to " + f.getPath());
            JOptionPane.showMessageDialog(this, "Report saved.\n\n" + content);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
