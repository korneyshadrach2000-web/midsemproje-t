# Student Records Manager (Java)

Offline desktop application for departments to manage student records on Windows without internet access. Built with **Java 11**, **Swing**, and **SQLite**.

## Features

- **Student CRUD**: Add, edit, and delete student records
- **Validation**: Required fields, unique student ID, valid email, year 1–6, GPA 0–4
- **No duplicate IDs**: Database primary key and validation prevent duplicate student IDs
- **Safe storage**: SQLite database (`student_records.db`) in the application directory
- **Filtering**: By program, year, and text search
- **Reports**: Export current list to CSV; generate text summary (counts by program and year)
- **Offline**: Runs entirely on the local machine; no network required

## Requirements

- **Windows** (or any OS with Java 11+)
- **Java 11 or later** (JDK or JRE)
- No internet connection required to run

## Build (Maven)

From the project directory:

```bat
mvn clean package
```

This produces:

- `target/student-records-manager-1.0.0.jar` – application JAR
- `target/lib/` – dependencies (including SQLite JDBC)

## Run

**Option A – with Maven:**

```bat
mvn exec:java -Dexec.mainClass="com.studentrecords.StudentRecordsApp"
```

**Option B – with Java (after `mvn package`):**

```bat
java -cp "target/student-records-manager-1.0.0.jar;target/lib/*" com.studentrecords.StudentRecordsApp
```

On Linux/macOS use `:` instead of `;` in the classpath.

**Option C – single runnable JAR (includes all dependencies):**

The build already creates a fat JAR. After `mvn clean package`:

```bat
java -jar target/student-records-manager-1.0.0-all.jar
```

Copy this single JAR to any Windows machine with Java 11+ installed; no internet needed to run.

## Version control (GitHub)

```bat
git init
git add .
git commit -m "Initial commit: Student Records Manager (Java)"
git remote add origin https://github.com/YOUR_USERNAME/student-records-java.git
git branch -M main
git push -u origin main
```

## Data and backup

- The database file `student_records.db` is created in the **current working directory** when you run the application (e.g. the folder from which you run `java -jar ...`).
- Back up by copying `student_records.db`.
- Use **Export CSV** and **Summary Report** for additional copies of your data.

## License

Use and modify as needed for your department.
