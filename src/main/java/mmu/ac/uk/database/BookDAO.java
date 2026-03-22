package mmu.ac.uk.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import mmu.ac.uk.helpers.BookSanitiser;
import mmu.ac.uk.helpers.DateHelper;
import mmu.ac.uk.models.Book;


/**
 * Data Access Object (DAO) that interacts with the Books database table.
 * A single shared database connection provided by a singleton DatabaseConnection.INSTANCE enum.
 */
public class BookDAO {
	
	/**
	 * DAO now retrieves the shared connection from the enum singleton.
	 */
	private Connection getConnection() {
		return DatabaseConnection.INSTANCE.getConnection();
	}

	/**
     * Converts a ResultSet row into a Book object.
     *
     * @param rs ResultSet positioned at a valid row.
     * @return Book object.
     */
	private Book getNextBook(ResultSet rs){
		try {			
			return new Book(
					rs.getInt("id"),
					rs.getString("title"),
					rs.getString("author"),
					DateHelper.normalise(rs.getString("date")),
					rs.getString("genres"),
					rs.getString("characters"),
					rs.getString("synopsis"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
     * Retrieves all books from the database.
     *
     * @return An Array list of all Book objects.
     */	
   public ArrayList<Book> getAllBooks(){
	   
		ArrayList<Book> allBooks = new ArrayList<Book>();
	    String selectSQL = "SELECT * from books";

		try (
			Statement stmt = getConnection().createStatement();
		    ResultSet rs1 = stmt.executeQuery(selectSQL);
			) { while (rs1.next()){
					allBooks.add(getNextBook(rs1));
				}
		   } catch (SQLException se) {
			se.printStackTrace(); 
			}

	   return allBooks;
   }
   
   /**
    * Inserts a new Book into the database.
    *
    * @param b the Book object to insert.
    * @return number of rows affected.
    * @throws SQLException If a database error occurs.
    */
   public int insertBook(Book b) throws SQLException {
	   
	   // Ensuring a consistent date format is used for the Book objects 
	   b = BookSanitiser.sanitise(b);
	   
	   String sql = "INSERT INTO books (title, author, date, genres, characters, synopsis) "
               + "VALUES (?, ?, ?, ?, ?, ?)";
	   
	   try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
		   ps.setString(1, b.getTitle());
		   ps.setString(2, b.getAuthor());
		   ps.setString(3, b.getDate());
		   ps.setString(4, b.getGenres());
		   ps.setString(5, b.getCharacters());
		   ps.setString(6, b.getSynopsis());
		   
		   return ps.executeUpdate();
	   }
   }
   
   /**
    * Updates an existing Book in the database.
    *
    * @param b the Book object that contains the FULL book data.....no patches here (using the book ID)
    * @return number of rows affected.
    * @throws SQLException If a database error occurs.
    */
   public int updateBook(Book b) throws SQLException {
	   
	   // Ensuring a consistent date format is used for the Book objects 
	   b = BookSanitiser.sanitise(b);
	   
       String sql = "UPDATE books SET title=?, author=?, date=?, genres=?, characters=?, synopsis=? "
                  + "WHERE id=?";

	   try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
		   ps.setString(1, b.getTitle());
	       ps.setString(2, b.getAuthor());
	       ps.setString(3, b.getDate());
	       ps.setString(4, b.getGenres());
	       ps.setString(5, b.getCharacters());
	       ps.setString(6, b.getSynopsis());
	       ps.setInt(7, b.getId());
		   
		   return ps.executeUpdate();
	   }
   }
   
   /**
    * Retrieves all data held for a single Book from the database using its unique id.
    *
    * @param id - unique id of the book to retrieve
    * @return - Returns null if not found, or Book object that matches the id
    */
   public Book getBookById(int id) {

       String sql = "SELECT * FROM books WHERE id = ?";

       try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

           ps.setInt(1, id);

           try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                   return getNextBook(rs);
               }
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return null;
   }

   
   /**
    * Deletes a Book from the database.
    *
    * @param b the Book object to delete (uses the book ID).
    * @return number of rows affected. Key: (1 = deleted, 0 = not found).
    * @throws SQLException If a database error occurs.
    */
   public int deleteBook(Book b) throws SQLException {

       String sql = "DELETE FROM books WHERE id=?";

       try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
           ps.setInt(1, b.getId());
           return ps.executeUpdate();
       } catch (SQLException e) {
           throw new SQLException("Book Not Deleted");
       }
   }
   
   /**
    * Performs a paginated search across title, year and genres.
    * The year is extracted from the normalised date format before being returned.
    *
    * @param search - user search query
    * @param offset - starting row index for pagination
    * @param limit - maximum number of results to return
    * @return List of Book objects that match the users search criteria
    */
   public List<Book> searchBooksPaginated(String search, int offset, int limit) {

        List<Book> results = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR genres LIKE ? OR RIGHT(date, 4) LIKE ? LIMIT ? OFFSET ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            String wildcard = "%" + search + "%";

            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(getNextBook(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
  
   
   /**
    * Counts the total number of books matching the search criteria, to aid number of paginated pages required.
    *
    * @param search - user search query
    * @return total number of matching book records
    */
   public int getSearchCount(String search) {

       String sql = "SELECT COUNT(*) FROM books WHERE title LIKE ? OR genres LIKE ? OR RIGHT(date, 4) LIKE ?";
       
	   try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
		   
		   String wildcard = "%" + search + "%";

           ps.setString(1, wildcard);
           ps.setString(2, wildcard);
           ps.setString(3, wildcard);
           
           try (ResultSet rs = ps.executeQuery()){
        	   if (rs.next()) {
        		   return rs.getInt(1);
        	   }
           }
       } catch (SQLException e) {
    	   e.printStackTrace();
       }
           
       return 0;
   }

   /**
    * Retrieves a paginated subset of books from the database for the BooksController.
    *
    * @param offset the starting row index (e.g., (page - 1) * limit)
    * @param limits the max number of books per page
    * @return a List of Book objects for required page
    */
   public List<Book> getBooks(int offset, int limit) {
	    
	    List<Book> list = new ArrayList<>();
	    String sql = "SELECT * FROM books LIMIT ? OFFSET ?";
	    
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			 ps.setInt(1, limit);
		     ps.setInt(2, offset);
		     
		     try (ResultSet rs = ps.executeQuery()) {
	        	   while (rs.next()) {
	        		   list.add(getNextBook(rs));
	        	   }
	           } 
		} catch (SQLException e) {
	        	   e.printStackTrace();
	           }

	    return list;
	}

   /**
    * Counts total number of books stored in the database - used to support pagination in the BooksController.
    *
    * @return the total number of book records
    */

	public int getBookCount() {
		
		String sql = "SELECT COUNT(*) FROM books";
		
	    try (
	    	Statement stmt = getConnection().createStatement();
	    	ResultSet rs = stmt.executeQuery(sql);
	    	) {
	    	if (rs.next()) {
	    		return rs.getInt(1);
	    	}
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return 0;
	}
	
	/**
     * Validates a Book object before insert/update based on the database requirements.
     *
     * @param b the Book to validate
     * @param requireId whether the ID must be checked (true for update)
     */
    public void validateBook(Book b, boolean requireId) {

        if (requireId && b.getId() <= 0)
            throw new IllegalArgumentException("Book ID must be a positive integer.");

        if (b.getTitle() == null || b.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty.");

        if (b.getAuthor() == null || b.getAuthor().trim().isEmpty())
            throw new IllegalArgumentException("Author cannot be empty.");

        if (b.getDate() == null || b.getDate().trim().isEmpty())
            throw new IllegalArgumentException("Date cannot be empty.");

        if (!b.getDate().matches("\\d{2}/\\d{2}/\\d{4}"))
            throw new IllegalArgumentException("Date must follow DD/MM/YYYY format.");
        
        if (b.getGenres() == null || b.getGenres().trim().isEmpty())
            throw new IllegalArgumentException("Genres cannot be empty.");

        if (b.getCharacters() == null || b.getCharacters().trim().isEmpty())
            throw new IllegalArgumentException("Characters cannot be empty.");

        if (b.getSynopsis() == null || b.getSynopsis().trim().isEmpty())
            throw new IllegalArgumentException("Synopsis cannot be empty.");

        if (b.getTitle().length() > 255)
            throw new IllegalArgumentException("Title exceeds 255 characters.");

        if (b.getAuthor().length() > 255)
            throw new IllegalArgumentException("Author exceeds 255 characters.");

        if (b.getGenres().length() > 255)
            throw new IllegalArgumentException("Genres exceed 255 characters.");
    }
}
