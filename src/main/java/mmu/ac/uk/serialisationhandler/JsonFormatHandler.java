package mmu.ac.uk.serialisationhandler;

import java.util.ArrayList;

import com.google.gson.Gson;
 
import mmu.ac.uk.models.Book;

/**
 * Implementation of application/json content type.
 * Concrete instantiation driven by FormatHandlerFactory.
 */
public class JsonFormatHandler implements FormatHandler {
	
	 
    /** 
     * Gson instance.
     * Stateless and safe to reuse.
     */
    private final Gson gson = new Gson();
 
    /**
     * Serialises all books in the list to a JSON array string.
     * @param - list of books to serialise.
     * @return - JSON array string.
     * @throws - exception.
     */
    @Override
    public String serialise(ArrayList<Book> books) throws Exception {
        return gson.toJson(books);
    }
 
    /**
     * Deserialises a single Book from a JSON object string.
     * @param - raw JSON string from the request body.
     * @return - Book populated from the JSON.
     * @throws - exception.
     */
    @Override
    public Book deserialise(String body) throws Exception {
        return gson.fromJson(body, Book.class);
    }
 
    /**
     * Returns code type "application/json".
     * @return the JSON MIME type used in HTTP responses.
     */
    @Override
    public String getResponseContentType() {
        return "application/json";
    }

}
