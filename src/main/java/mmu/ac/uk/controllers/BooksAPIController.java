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
import mmu.ac.uk.models.Book;
import mmu.ac.uk.models.BookList;


@WebServlet("/books-api")
public class BooksAPIController extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Read the Accept header sent by the client in Bruno. e.g. Accept: application/json, Accept: text/xml....or other
        String acceptHeader = request.getHeader("Accept");  
        
        BookDAO dao = new BookDAO();
		ArrayList<Book> allBooks = dao.getAllBooks();
		
		// Default to JSON if no Accept header is provided
	    if (acceptHeader == null) {
	        acceptHeader = "application/json";
	    }
		
		//If Accept header contains "application/json" return json.
		if (acceptHeader != null && acceptHeader.contains("application/json")) {
			response.setContentType("application/json"); // server to client format
			PrintWriter out = response.getWriter();
			
			//Convert Java to json
			Gson gson = new Gson();
			String json = gson.toJson(allBooks);
			out.write(json);
			out.close();
			return;
		}
		//If Accept header contains "text/xml" return XML.
		if (acceptHeader != null && (acceptHeader.contains("application/xml") || acceptHeader.contains("text/xml"))) {
			response.setContentType("text/xml"); // server to client format
			PrintWriter out = response.getWriter();
			
			try {
				//JAVA Array List (you get this from the ArrayList<> method to get allBooks -> to XML
				//Wrap ArrayList inside BookList class (umbrella needed for XML)
				BookList cl = new BookList(allBooks);
				StringWriter sw = new StringWriter();
				
				//Convert Java -> XML using JAXB
				JAXBContext context = JAXBContext.newInstance(BookList.class);
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
				m.marshal(cl, sw);
				out.write(sw.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
                response.sendError(500, "XML marshalling failed");				
			}
			out.close();
			return;
		}
		
		// Unsupported Accept header
	    response.sendError(406, "Not Acceptable: Supported types are application/json and text/xml AND some weird shit");

	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		// Read input format: Content-Type header - highlights the format the client sent data in
	    String contentType = request.getHeader("Content-Type");

	    // Read the entire request body as raw text and concatenates it
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    Book newBook = null;
	    
	    try {
	        // If Content Type JSON input
	        if (contentType != null && contentType.contains("application/json")) {

	            Gson gson = new Gson();
	            newBook = gson.fromJson(body, Book.class);
	        }

	        // If Content Type XML input
	        else if (contentType != null && (contentType.contains("application/xml") || contentType.contains("text/xml"))) {

	            JAXBContext context = JAXBContext.newInstance(Book.class);
	            Unmarshaller unmarshaller = context.createUnmarshaller();
	            newBook = (Book) unmarshaller.unmarshal(new StringReader(body));
	        }

	        // Deal with an unsupported format
	        else {
	            response.sendError(415, "Unsupported Content-Type");
	            return;
	        }

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

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendError(500, "Error processing Add Book POST request");
	    }
	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Read input format: Content-Type header - highlights the format the client sent data in
		String contentType = request.getHeader("Content-Type");

	    // Read the request body
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    Book updateBook = null;

	    try {
	        // JSON input
	        if (contentType != null && contentType.contains("application/json")) {

	            Gson gson = new Gson();
	            updateBook = gson.fromJson(body, Book.class);
	        }
	        // XML input
	        else if (contentType != null && (contentType.contains("application/xml") || contentType.contains("text/xml"))) {

	            JAXBContext context = JAXBContext.newInstance(Book.class);
	            Unmarshaller unmarshaller = context.createUnmarshaller();
	            updateBook = (Book) unmarshaller.unmarshal(new StringReader(body));
	        }
	        // Deal with any unsupported input type
	        else {
	            response.sendError(415, "Unsupported Content-Type");
	            return;
	        }

	        // ID is required AND must exist in the database for update to be allowed. The latter dealt with below.
	        if (updateBook.getId() == 0) {
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

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendError(500, "Error processing PUT request.");
	    }
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Read input format: Content-Type header - highlights the format the client sent data in
		String contentType = request.getHeader("Content-Type");
	    String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);

	    int idToDelete = 0;

	    try {
	        // JSON input
	        if (contentType != null && contentType.contains("application/json")) {

	        	Gson gson = new Gson();
	        	Book toDelete = gson.fromJson(body, Book.class);

	            idToDelete = toDelete.getId();
	        }
	        // XML input
	        else if (contentType != null && (contentType.contains("application/xml") || contentType.contains("text/xml"))) {

	            JAXBContext context = JAXBContext.newInstance(Book.class);
	            Unmarshaller unmarshaller = context.createUnmarshaller();
	            Book b = (Book) unmarshaller.unmarshal(new StringReader(body));
	            idToDelete = b.getId();
	        }
	        // Deal with Unsupported input
	        else {
	            response.sendError(415, "Unsupported Content-Type for DELETE");
	            return;
	        }

	        // Validate ID
	        if (idToDelete == 0) {
	            response.sendError(400, "Book ID is required to delete book");
	            return;
	        }
	        
	        // Delete using DAO
	        BookDAO dao = new BookDAO();
	        Book b = new Book();
	        //DAO requires a Book object, not an int
	        b.setId(idToDelete);
	        
	        int rows = dao.deleteBook(b);  
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

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendError(500, "Error processing DELETE request");
	    }
	}

}
