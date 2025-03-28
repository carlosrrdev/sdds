package dev.carlosrr.sdds.util;

import javax.swing.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseCreator {

    // Pattern to match "lastname_firstname_dob" format
    private static final Pattern DIRECTORY_PATTERN = Pattern.compile("([^_]+)_([^_]+)_([^_]+)", Pattern.CASE_INSENSITIVE);

    // Record to hold extracted directory information
    public static class DirectoryInfo {
        String lastName;
        String firstName;
        String dob;
        String dirPath;

        public DirectoryInfo(String lastName, String firstName, String dob, String dirPath) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.dob = dob;
            this.dirPath = dirPath;
        }
    }

    /**
     * Creates a SQLite database by scanning subdirectories in the specified directory path.
     *
     * @param rootDirectory The root directory to scan for subdirectories
     * @param progressBar   The progress bar to update during operation
     * @return The path to the created database file
     */
    public static String createDatabase(String rootDirectory, JProgressBar progressBar) throws Exception {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateString = dateFormat.format(currentDate);
        // Update progress bar status
        progressBar.setString("Scanning directories...");
        progressBar.setValue(10);

        // Step 1: Scan directory and collect information
        List<DirectoryInfo> directoryInfoList = scanDirectories(rootDirectory);

        if (directoryInfoList.isEmpty()) {
            throw new Exception("No valid subdirectories found. Subdirectories must follow the pattern 'lastname_firstname_dob'");
        }

        // Update progress
        progressBar.setString("Creating database...");
        progressBar.setValue(40);

        // Step 2: Create SQLite database
        String dbName = "sdds_" + currentDateString + ".db";
        String dbProjectRoot = new File("").getAbsolutePath();
        String dbPath = dbProjectRoot + File.separator + dbName;
        createSqliteDatabase(dbPath, directoryInfoList);

        // Complete progress
        progressBar.setString("Database created successfully");
        progressBar.setValue(100);

        return dbPath;
    }

    /**
     * Scans the given directory for subdirectories matching the naming pattern.
     */
    private static List<DirectoryInfo> scanDirectories(String rootDirectory) {
        List<DirectoryInfo> result = new ArrayList<>();
        File root = new File(rootDirectory);

        if (root.exists() && root.isDirectory()) {
            File[] subDirectories = root.listFiles(File::isDirectory);

            if (subDirectories != null) {
                for (File dir : subDirectories) {
                    String dirName = dir.getName();
                    Matcher matcher = DIRECTORY_PATTERN.matcher(dirName);

                    if (matcher.matches()) {
                        String lastName = matcher.group(1);
                        String firstName = matcher.group(2);
                        String dob = matcher.group(3);
                        String dirPath = dir.getAbsolutePath();

                        result.add(new DirectoryInfo(lastName, firstName, dob, dirPath));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Creates a SQLite database and populates it with the directory information.
     */
    private static void createSqliteDatabase(String dbPath, List<DirectoryInfo> directoryInfoList) throws SQLException {
        // Delete existing database if present
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // JDBC URL for SQLite
        String url = "jdbc:sqlite:" + dbPath;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Create table
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS directories (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "lastname TEXT NOT NULL, " +
                            "firstname TEXT NOT NULL, " +
                            "dob TEXT NOT NULL, " +
                            "dir_path TEXT NOT NULL UNIQUE" +
                            ")";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Insert data
            String insertSQL =
                    "INSERT INTO directories (lastname, firstname, dob, dir_path) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (DirectoryInfo info : directoryInfoList) {
                    pstmt.setString(1, info.lastName);
                    pstmt.setString(2, info.firstName);
                    pstmt.setString(3, info.dob);
                    pstmt.setString(4, info.dirPath);
                    pstmt.executeUpdate();
                }
            }
        }
    }
}