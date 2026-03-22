package mmu.ac.uk.serialisationhandler;

import java.util.ArrayList;

import mmu.ac.uk.models.Book;

/**
 * Strategy interface used for handling serialising and deserialising Books objects to and from a user specified format (JSON, XML, plain text, etc.).
 * Each concrete implementation handles exactly one content type, and can be easily extended.
 * @link FormatHandlerFactory manages the correct implementation at runtime.
 */
public interface FormatHandler {
	
	/**
     * Serialises a list of Book objects into a format-specific string.
     *
     * @param - list of books to serialise.
     * @return - serialised representation as a String.
     * @throws - exception if serialisation fails.
     */
    String serialise(ArrayList<Book> books) throws Exception;
 
    /**
     * Deserialises a single Book from a format-specific request body.
     *
     * @param - raw request body string.
     * @return - Book populated from the body content.
     * @throws - exception if the body cannot be parsed or is invalid.
     */
    Book deserialise(String body) throws Exception;
 
    /**
     * Returns the MIME type string that should be set on the HTTP response
     * Content-Type header when this handler is used.
     *
     * @return - response, in format based on the content type selected.
     */
    String getResponseContentType();

}
