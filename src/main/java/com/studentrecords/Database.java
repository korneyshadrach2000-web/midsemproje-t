package com.studentrecords;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for student records. Enforces unique student ID and required fields.
 */
public class Database {
    private static final String DB_NAME = "student_records.db";
    private final String dbPath;

    public Database() {
        this.dbPath = new File(System.getProperty("user.dir"), DB_NAME).getAbsolutePath();
    }

    public void init() throws SQLException {
        try (Connection c = connect()) {
            c.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS students (" +
                "  student_id TEXT PRIMARY KEY," +
                "  first_name TEXT NOT NULL," +
                "  last_name TEXT NOT NULL," +
                "  email TEXT NOT NULL," +
                "  program TEXT NOT NULL," +
                "  year INTEGER NOT NULL CHECK (year >= 1 AND year <= 6)," +
                "  gpa REAL CHECK (gpa IS NULL OR (gpa >= 0 AND gpa <= 4))," +
                "  notes TEXT DEFAULT ''" +
                ")"
            );
            c.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_students_program ON students(program)"
            );
            c.createStatement().execute(
                "CREATE INDEX IF NOT EXISTS idx_students_year ON students(year)"
            );
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public void addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (student_id, first_name, last_name, email, program, year, gpa, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getProgram());
            ps.setInt(6, s.getYear());
            ps.setObject(7, s.getGpa());
            ps.setString(8, s.getNotes());
            ps.executeUpdate();
        }
    }

    public void updateStudent(String existingId, Student s) throws SQLException {
        String sql = "UPDATE students SET first_name=?, last_name=?, email=?, program=?, year=?, gpa=?, notes=? WHERE student_id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getProgram());
            ps.setInt(5, s.getYear());
            ps.setObject(6, s.getGpa());
            ps.setString(7, s.getNotes());
            ps.setString(8, existingId);
            ps.executeUpdate();
        }
    }

    public void deleteStudent(String studentId) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        }
    }

    /**
     * Delete all student records. Intended for admin/maintenance use.
     */
    public void deleteAllStudents() throws SQLException {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM students");
        }
    }

    public Student getStudent(String studentId) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("SELECT * FROM students WHERE student_id=?")) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rowToStudent(rs) : null;
            }
        }
    }

    public List<Student> listStudents(String programFilter, Integer yearFilter, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (programFilter != null && !programFilter.isEmpty()) {
            sql.append(" AND program = ?");
            params.add(programFilter);
        }
        if (yearFilter != null) {
            sql.append(" AND year = ?");
            params.add(yearFilter);
        }
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (student_id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR email LIKE ?)");
            String q = "%" + search + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append(" ORDER BY last_name, first_name, student_id");

        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object o : params) {
                if (o instanceof String) ps.setString(idx++, (String) o);
                else if (o instanceof Integer) ps.setInt(idx++, (Integer) o);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<>();
                while (rs.next()) list.add(rowToStudent(rs));
                return list;
            }
        }
    }

    public List<String> getPrograms() throws SQLException {
        List<String> out = new ArrayList<>();
        try (Connection c = connect(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT program FROM students ORDER BY program")) {
            while (rs.next()) out.add(rs.getString("program"));
        }
        return out;
    }

    public List<Object[]> countByProgram() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        try (Connection c = connect(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT program, COUNT(*) AS cnt FROM students GROUP BY program ORDER BY program")) {
            while (rs.next()) out.add(new Object[]{ rs.getString("program"), rs.getInt("cnt") });
        }
        return out;
    }

    public List<Object[]> countByYear() throws SQLException {
        List<Object[]> out = new ArrayList<>();
        try (Connection c = connect(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT year, COUNT(*) AS cnt FROM students GROUP BY year ORDER BY year")) {
            while (rs.next()) out.add(new Object[]{ rs.getInt("year"), rs.getInt("cnt") });
        }
        return out;
    }

    private static Student rowToStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getString("student_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("program"),
            rs.getInt("year"),
            (Double) rs.getObject("gpa"),
            rs.getString("notes")
        );
    }
}
