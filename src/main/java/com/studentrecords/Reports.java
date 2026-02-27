package com.studentrecords;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report generation: CSV export and summary report.
 */
public final class Reports {
    private static final Database DB = new Database();

    private Reports() {}

    public static int exportCsv(Path filepath, String programFilter, Integer yearFilter, String search) throws SQLException, IOException {
        List<Student> students = DB.listStudents(programFilter, yearFilter, search);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(filepath.toFile()), "UTF-8");
             BufferedWriter bw = new BufferedWriter(w)) {
            bw.write("Student ID,First Name,Last Name,Email,Program,Year,GPA,Notes");
            bw.newLine();
            for (Student s : students) {
                String gpa = s.getGpa() != null ? String.valueOf(s.getGpa()) : "";
                String notes = escapeCsv(s.getNotes());
                bw.write(String.join(",", escapeCsv(s.getId()), escapeCsv(s.getFirstName()),
                    escapeCsv(s.getLastName()), escapeCsv(s.getEmail()), escapeCsv(s.getProgram()),
                    String.valueOf(s.getYear()), gpa, notes));
                bw.newLine();
            }
        }
        return students.size();
    }

    public static String generateSummaryReport(Path filepath) throws SQLException, IOException {
        List<Object[]> byProgram = DB.countByProgram();
        List<Object[]> byYear = DB.countByYear();
        int total = byProgram.stream().mapToInt(row -> (Integer) row[1]).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Student Records – Summary Report\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        sb.append("Total students: ").append(total).append("\n\n");
        sb.append("By program:\n");
        for (Object[] row : byProgram) {
            sb.append("  ").append(row[0]).append(": ").append(row[1]).append("\n");
        }
        sb.append("\nBy year:\n");
        for (Object[] row : byYear) {
            sb.append("  Year ").append(row[0]).append(": ").append(row[1]).append("\n");
        }
        sb.append("\n");

        String content = sb.toString();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(filepath.toFile()), "UTF-8")) {
            w.write(content);
        }
        return content;
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
