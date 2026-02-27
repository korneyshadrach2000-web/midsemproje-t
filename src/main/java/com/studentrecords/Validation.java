package com.studentrecords;

import java.util.regex.Pattern;

/**
 * Input validation for student records. Prevents missing fields, invalid formats, duplicate IDs.
 */
public final class Validation {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9_\\-]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\w\\s\\-' ]{1,80}$");

    private Validation() {}

    public static Student validateStudent(String studentId, String firstName, String lastName,
                                          String email, String program, String yearStr,
                                          String gpaStr, String notes,
                                          boolean isEdit, String currentId) throws ValidationException {
        validateId(studentId, isEdit ? currentId : null);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateEmail(email);
        validateRequired(program, "Program");
        int year = validateYear(yearStr);
        Double gpa = validateGpa(gpaStr);
        String notesVal = notes != null ? notes.trim() : "";
        if (notesVal.length() > 2000) notesVal = notesVal.substring(0, 2000);

        return new Student(
            studentId.trim(),
            firstName.trim(),
            lastName.trim(),
            email.trim(),
            program.trim(),
            year,
            gpa,
            notesVal
        );
    }

    public static void validateId(String value, String excludeCurrentId) throws ValidationException {
        value = value != null ? value.trim() : "";
        if (value.isEmpty()) throw new ValidationException("Student ID is required.", "student_id");
        if (!ID_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                "Student ID must be 3–20 characters (letters, numbers, hyphens, underscores).",
                "student_id"
            );
        }
        try {
            Student existing = new Database().getStudent(value);
            if (existing != null && !value.equals(excludeCurrentId)) {
                throw new ValidationException(
                    "A student with ID '" + value + "' already exists. Use a unique ID.",
                    "student_id"
                );
            }
        } catch (Exception e) {
            if (e instanceof ValidationException) throw (ValidationException) e;
            throw new ValidationException("Could not check duplicate ID: " + e.getMessage());
        }
    }

    private static void validateRequired(String value, String label) throws ValidationException {
        if (value == null || value.trim().isEmpty())
            throw new ValidationException(label + " is required.");
    }

    private static void validateName(String value, String label) throws ValidationException {
        validateRequired(value, label);
        if (!NAME_PATTERN.matcher(value.trim()).matches()) {
            throw new ValidationException(
                label + " can only contain letters, spaces, hyphens, and apostrophes (max 80 characters)."
            );
        }
    }

    private static void validateEmail(String value) throws ValidationException {
        validateRequired(value, "Email");
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address.", "email");
        }
    }

    private static int validateYear(String value) throws ValidationException {
        validateRequired(value, "Year");
        try {
            int y = Integer.parseInt(value.trim());
            if (y < 1 || y > 6) throw new ValidationException("Year must be between 1 and 6.", "year");
            return y;
        } catch (NumberFormatException e) {
            throw new ValidationException("Year must be a number (1–6).", "year");
        }
    }

    private static Double validateGpa(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            double g = Double.parseDouble(value.trim());
            if (g < 0 || g > 4) throw new ValidationException("GPA must be between 0 and 4.", "gpa");
            return g;
        } catch (NumberFormatException e) {
            throw new ValidationException("GPA must be a number between 0 and 4.", "gpa");
        }
    }
}
