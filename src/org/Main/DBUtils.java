package org.Main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtils {
    private static final Logger log = Logger.getLogger(DBUtils.class.getName());
        static {
            FileHandler fh = null;
            try {
                fh = new FileHandler("LogFile.log", 1000000, 10, true);
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Exception of FileHandler using", ex);
            }
            log.addHandler(fh);
        }

    public static int appendToDb(List<Valute> valutes, Connection connection) throws SQLException {
        String sqlInsert = """
                INSERT OR REPLACE INTO Currency (Date, NumCode, CharCode, Nominal, Name, Value, VunitRate)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sqlInsert)) {
            for (Valute value : valutes) {
                final String dateStr = DateUtils.formatDate(
                        value.date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                );

                int idx = 0;
                stmt.setString(++idx, dateStr);
                stmt.setInt(++idx, value.numCode);
                stmt.setString(++idx, value.charCode);
                stmt.setInt(++idx, value.nominal);
                stmt.setString(++idx, value.name);
                stmt.setFloat(++idx, value.value);
                stmt.setFloat(++idx, value.vUnitRate);

                stmt.executeUpdate();
            }
        }
        return valutes.size();
    }

    public static int getCountCcy(Connection connection) throws SQLException {
        String sqlGetCount = """
                    SELECT count()
                    FROM Currency
                    """;
        int cnt = 0;
        try (
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sqlGetCount)
        ) {
            if (rs.next()) {
                cnt = rs.getInt(1);
            }
        }
        return cnt;
    }

    public static void validateDb(Connection connection) {
        final String sqlCheckTable = """
                    SELECT count()
                    FROM sqlite_master
                    WHERE type = 'table' AND name = 'Currency'
                    """;
        final String sqlCreateTable = """
                    CREATE TABLE 'Currency' (
                        Date TEXT(10),
                        NumCode INTEGER,
                        CharCode TEXT,
                        Nominal INTEGER,
                        Name TEXT,
                        Value REAL,
                        VunitRate REAL,
                        PRIMARY KEY(Date, NumCode)
                    );
                    """;

        try (
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sqlCheckTable)
        ) {
            if (rs.next()) {
                log.info("Check table existing");
                if (rs.getInt(1) == 0) {
                    log.warning("Table not found");
                    log.info("Creating table");
                    stmt.executeUpdate(sqlCreateTable);
                    log.fine("Table created");
                } else {
                    log.fine("Table found");
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        }
    }
}
