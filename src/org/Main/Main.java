package org.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {
        // TODO: Можно еще больше разных исключений использовать, а не только один Exception....... Плохо разобрался с исключениями =(
        if (args.length < 2) {
            throw new Exception(
                    """
                    Please fill the arguments
                    <Date or Date Range> - The date or period for which exchange rates will be searched
                                           (Example #1: 31.12.2000; Example #2: 01.12.2000-31.12.2000)
                    <Database> - Path for the database file
                    """
            );
        } else if (args[0] == null) {
            throw new Exception("<Date or Date Range> filled incorrect");
        } else if (args[1] == null) {
            throw new Exception("<Database> filled incorrect");
        }
        final String inputDate = args[0];
        final String inputDb = args[1];

        final List<LocalDate> dateList = DateUtils.getDates(inputDate);
        if (dateList.size() > 1) {
            System.out.printf(
                    "Date range: %s to %s\n",
                    DateUtils.formatDate(dateList.get(0)),
                    DateUtils.formatDate(dateList.get(dateList.size() - 1))
            );
        } else {
            System.out.println("Single date: " + DateUtils.formatDate(dateList.get(0)));
        }

        String currentPath = System.getProperty("user.dir");
        String dbPath = inputDb;
        if (!inputDb.contains("/") && !inputDb.contains("\\")) {
            dbPath = currentPath + "\\" + inputDb;
        }
        Class.forName("org.sqlite.JDBC");
        try (final Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            System.out.println("DB connected");

            DBUtils.validateDb(c);

            int ccyAddCounter = DBUtils.getCountCcy(c);
            int ccyCounter = 0;
            for (LocalDate date : dateList) {
                ccyCounter += DBUtils.appendToDb(CbrRu.getValutes(date), c);
                Thread.sleep(5000);
            }
            System.out.printf("%d currencies found\n", ccyCounter);
            System.out.printf("%d currencies added\n", DBUtils.getCountCcy(c) - ccyAddCounter);
            System.out.println("DB filled");
        }
    }
}