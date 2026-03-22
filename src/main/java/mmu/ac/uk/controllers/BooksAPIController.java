package mmu.ac.uk.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import mmu.ac.uk.database.BookDAO;
import mmu.ac.uk.helpers.ContentTypeHelper;
import mmu.ac.uk.models.Book;
import mmu.ac.uk.models.BookList;
import mmu.ac.uk.serialisationhandler.FormatHandler;
import mmu.ac.uk.serialisationhandler.FormatHandlerFactory;

/**
 * REST API servlet for the Books database, linked to /Bookapi.
 * Supports four HTTP methods:
 * 1. GET — retrieve all books.
 * 2. POST — create a new book.
 * 3. PUT — update an existing book (ID required).
 * 4. DELETE — delete a book (ID required).
 * All serialisation and deserialisation delegated to the FormatHandlerFactory.
 */
@WebServlet("/Bookapi")
public class BooksAPIController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	/**
     * GET request - retrieve all books in the format requested by the client.
     * @param - HTTP request containing Accept header.
     * @param - HTTP response to write books into.
     * @throws - ServletException if a servlet error occurs.
     * @throws - IOException if a response write error occurs.
     */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Accept header is passed directly from the client (i.e Bruno) to the factory, which is then obtained here.
        FormatHandler handler = FormatHandlerFactory.getHandler(request.getHeader("Accept"));
        
        // If null or unsupported Accept header returned from factory, send error.
        if (handler == null) {
    	    response.sendError(406, "Not Acceptable: Supported types: JSON, XML, TEXT");
            return;
        }
 
        try {
            BookDAO dao = new BookDAO();
            ArrayList<Book> allBooks = dao.getAllBooks();
 
            // No need for switch statement as handler encapsulates all format-specific serialisation logic.
            response.setContentType(handler.getResponseContentType()); // server to client format
            PrintWriter out = response.getWriter();
            out.write(handler.serialise(allBooks));
            out.close();
 
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Server error processing GET all books request");
        }
        
	}
	
	/**
	 * POST request - create / add a new book.
     * @param - HTTP request containing the Content-Type header and serialised book in the body.
     * @param - HTTP response.
     * @throws - ServletException if a servlet error occurs.
     * @throws - IOException if a response write error occurs.
     */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		
		// Content Type is passed directly from the client (i.e Bruno) to the factory, which is then obtained here.
        FormatHandler handler = FormatHandlerFactory.getHandler(request.getHeader("Content-Type"));
		
        // If null or unsupported Content Type returned from factory, send error.
        if (handler == null) {
            response.sendError(415, "Unsupported Content-Type. Supported types: JSON, XML, TEXT");
            return;
        }
        
	    // Read the entire request body as raw text and concatenates it
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    try {
	    	Book newBook = handler.deserialise(body);
	        // Insert into DB
	        BookDAO dao = new BookDAO();
	        dao.insertBook(newBook);

	        // Return 201 for new Book created
	        response.setStatus(201);
	        response.setContentType("application/json");

	        // Optional confirmation body
	        PrintWriter out = response.getWriter();
	        out.write("{\"status\":\"created\"}");
	        out.close();

	    } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendError(500, "Error processing Add Book POST request");
	    }
	}	    
	    
    /**
     * PUT request - update an existing book. The book ID must be present in the body to identify the book to update.
     * @param - HTTP request containing the Content-Type header and serialised book (with ID) in the body.
     * @param - HTTP response.
     * @throws - ServletException if a servlet error occurs.
     * @throws - IOException if a response write error occurs.
     */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Content Type is passed directly from the client (i.e Bruno) to the factory, which is then obtained here.
        FormatHandler handler = FormatHandlerFactory.getHandler(request.getHeader("Content-Type"));
		
        // If null or unsupported Content Type returned from factory, send error.
        if (handler == null) {
            response.sendError(415, "Unsupported Content-Type. Supported types: JSON, XML, TEXT");
            return;
        }
        
	    // Read the entire request body as raw text and concatenates it
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    try {
	    	Book updateBook = handler.deserialise(body);

	    	// ID is required AND must exist in the database for update to be allowed. The latter dealt with below.
	        if (updateBook.getId() <= 0) {
	            response.sendError(400, "Book ID is required for PUT update");
	            return;
	        }
	        
	        //Perform action and Update book entry in DB.
	        BookDAO dao = new BookDAO();
	        int rows = dao.updateBook(updateBook);
	        // rows = 1 → updated
	        // rows = 0 → ID not found
	        
	        // Handle book not found
	        if(rows == 0) {
	        	response.sendError(404,  "Book ID does not exist");
	        	return;
	        }
	        // Return 200 for Book updated, or send error
	        response.setStatus(200);
	        response.setContentType("application/json");

	        // Optional confirmation body
	        PrintWriter out = response.getWriter();
	        out.write("{\"status\":\"book updated\"}");
	        out.close();

		    } catch (IllegalArgumentException e) {
	            response.sendError(400, e.getMessage());
	            } catch (Exception e) {
	            	e.printStackTrace();
	            	response.sendError(500, "Error processing PUT request.");
	            	}
	    }
	
    /**
     * DELETE request - deletes an existing book. The book ID must be present in the body to identify the book to delete.
     * @param - HTTP request containing the Content-Type header and book ID in the body.
     * @param - HTTP response.
     * @throws - ServletException if a servlet error occurs.
     * @throws - IOException if a response write error occurs.
     */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Content Type is passed directly from the client (i.e Bruno) to the factory, which is then obtained here.
        FormatHandler handler = FormatHandlerFactory.getHandler(request.getHeader("Content-Type"));
		
        // If null or unsupported Content Type returned from factory, send error.
        if (handler == null) {
            response.sendError(415, "Unsupported Content-Type. Supported types: JSON, XML, TEXT");
            return;
        }
		
	    // Read the entire request body as raw text and concatenates it
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    try {
	    	Book parsed = handler.deserialise(body);
	    	int idToDelete = parsed.getId();
	    	
	    	// Validate ID
	        if (idToDelete <= 0) {
	            response.sendError(400, "Book ID is required to delete book");
	            return;
	        }
	        
	        // Delete using DAO
	        BookDAO dao = new BookDAO();
	        int rows = dao.deleteBook(parsed);  
	        // rows = 1 → deleted
	        // rows = 0 → ID not found

	        if (rows == 0) {
	            response.sendError(404, "Book ID does not exist");
	            return;
	        }

	        // Status 200 OK - book deleted
	        response.setStatus(200);
	        response.setContentType("application/json");

	        // Optional confirmation body
	        PrintWriter out = response.getWriter();
	        out.write("{\"status\":\"book deleted\"}");
	        out.close();

	    } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage());
	    } catch (Exception e) {
	    	e.printStackTrace();
	        response.sendError(500, "Error processing DELETE request");
	    }
	    }
	}

