package mmu.ac.uk.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import mmu.ac.uk.helpers.DateHelper;

/**
 * A single date utility that normalises the initial date values stored in the Book database.
 * Dates are formatted to a standardised DD/MM/YYYY format.
 * If a missing day or month exists, the default will be to make them 01/01.
 * In no date exists or is not in a date format accepted, it will return null.
 * {@link DatabaseConnection} singleton is used to keep credential management in one place and avoid additional connections being created.
 */
public class DateCleanupUtility {

    public static void main(String[] args) {
    	
        Connection conn = DatabaseConnection.INSTANCE.getConnection();
        
        String selectSQL = "SELECT id, date FROM books";

        try (
            PreparedStatement selectPS = conn.prepareStatement(selectSQL);
            ResultSet rs = selectPS.executeQuery()){
        	
        	while (rs.next()) {
                int id = rs.getInt("id");
                String rawDate = rs.getString("date");
                String cleanDate = DateHelper.normalise(rawDate);
                
                if (cleanDate == null) {
                    System.out.println("ID " + id + ": '" + rawDate + "' → Date unknown (NULL)");
                } else {
                    System.out.println("ID " + id + ": '" + rawDate + "' → '" + cleanDate + "'");
                }
            
                String updateSQL = "UPDATE books SET date=? WHERE id=?";
                try (
                	PreparedStatement updatePS = conn.prepareStatement(updateSQL)) {
                	updatePS.setString(1, cleanDate);
                    updatePS.setInt(2, id);
                    updatePS.executeUpdate();
                }   
            }

            System.out.println("Date cleanup complete.");

        } catch (Exception e) {
        	System.err.println("Date cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
