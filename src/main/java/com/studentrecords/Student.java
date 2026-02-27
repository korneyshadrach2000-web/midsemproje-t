package com.studentrecords;

/**
 * Student record model.
 */
public class Student {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String program;
    private final int year;
    private final Double gpa;
    private final String notes;

    public Student(String id, String firstName, String lastName, String email,
                   String program, int year, Double gpa, String notes) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.program = program;
        this.year = year;
        this.gpa = gpa;
        this.notes = notes != null ? notes : "";
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getProgram() { return program; }
    public int getYear() { return year; }
    public Double getGpa() { return gpa; }
    public String getNotes() { return notes; }

    public String getFullName() {
        return lastName + ", " + firstName;
    }
}
