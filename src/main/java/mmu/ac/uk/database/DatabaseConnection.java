package mmu.ac.uk.database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * A true singleton database connection using an enum.
 * This class creates ONE shared Connection object when the application starts.
 * All DAO methods use the same connection. It is not closed until the application shuts down.
 */
public enum DatabaseConnection {
	 
	// One single shared instance of the database connection.
	INSTANCE;
	
	private static final String USER = "hallhald";
    private static final String PASSWORD = "Vot7WruttA";
    private static final String URL = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + USER;
    
    private Connection connection;
    
    /**
     * Creates the JDBC database connection ONCE when the enum is initiated.
     */
    DatabaseConnection() {
    	try {
    		Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
			connection = DriverManager.getConnection(URL, USER, PASSWORD);
    	} catch (Exception e) {
	    	throw new RuntimeException("JDBC - Java database connection failed to initialise", e);
    	}
    }
    
    /**
     * Returns the single shared JDBC Connection object.
     * DAO methods call this instead of opening new connections.
     */
    public Connection getConnection() {
    	return connection;
    }
 	    	
}
