package mmu.ac.uk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import mmu.ac.uk.helpers.DateHelper;

public class DateCleanupUtility {
    static String user = "hallhald";
    static String password = "Vot7WruttA";
    static String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection(url, user, password);

            String selectSQL = "SELECT id, date FROM books";
            PreparedStatement selectPS = conn.prepareStatement(selectSQL);
            ResultSet rs = selectPS.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String rawDate = rs.getString("date");

                String cleanDate = DateHelper.normalise(rawDate);

                if (cleanDate == null) {
                    System.out.println("ID " + id + ": '" + rawDate + "' → Date unknown (NULL)");
                    cleanDate = null;
                } else {
                    System.out.println("ID " + id + ": '" + rawDate + "' → '" + cleanDate + "'");
                }

                String updateSQL = "UPDATE books SET date=? WHERE id=?";
                PreparedStatement updatePS = conn.prepareStatement(updateSQL);
                updatePS.setString(1, cleanDate);
                updatePS.setInt(2, id);
                updatePS.executeUpdate();
                updatePS.close();
            }

            rs.close();
            selectPS.close();
            conn.close();

            System.out.println("Date cleanup complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
