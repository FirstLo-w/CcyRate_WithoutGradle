package org.Main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;
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

    public static int appendToDb(List<Valute> valutes, Connection connection) {
        int count = 0;
        for (Valute value : valutes) {
            count += insertRate(connection, value);
        }
        return count;
    }

    public static int insertRate(Connection connection , Valute valute) {
        String sqlInsert = """
                INSERT OR IGNORE INTO Currency (Date, CreateDate, NumCode, CharCode, Nominal, Name, Value, VunitRate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sqlInsert)) {
            final String dateStr = DateUtils.formatDate(
                    valute.date,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
            int idx = 0;
            stmt.setString(++idx, dateStr);
            stmt.setString(++idx, DateUtils.getCurrentDateStr());
            stmt.setInt(++idx, valute.numCode);
            stmt.setString(++idx, valute.charCode);
            stmt.setInt(++idx, valute.nominal);
            stmt.setString(++idx, valute.name);
            stmt.setFloat(++idx, valute.value);
            stmt.setFloat(++idx, valute.vUnitRate);

            stmt.executeUpdate();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
            return 0;
        }
        return 1;
    }

    public static Valute getRate(Connection connection, LocalDate date, int ccy) {
        String sqlGetCount = """
                    SELECT Date, CreateDate, NumCode, CharCode, Nominal, Name, Value, VunitRate
                    FROM Currency
                    WHERE Date = ? AND NumCode = ?
                    """;
        try (PreparedStatement stmt = connection.prepareStatement(sqlGetCount)){
            stmt.setString(0, DateUtils.formatDate(date));
            stmt.setInt(1, ccy);
            ResultSet rs = stmt.executeQuery(sqlGetCount);
            if (rs.next()) {
                int idx = 0;
                String rateDate = rs.getString(++idx);
                String createDate = rs.getString(++idx);
                return new Valute(
                        DateUtils.parseDate(rateDate),
                        DateUtils.parseDateTime(createDate),
                        rs.getInt(++idx),
                        rs.getString(++idx),
                        rs.getInt(++idx),
                        rs.getString(++idx),
                        rs.getFloat(++idx),
                        rs.getFloat(++idx)
                        );
            } else {
                log.log(Level.SEVERE, "Rate getting. Rate not found");
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        }
        return null;
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
                        CreateDate TEXT(10),
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
