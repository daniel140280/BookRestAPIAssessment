package mmu.ac.uk.serialisationhandler;

import java.util.ArrayList;

import mmu.ac.uk.models.Book;


/**
 * Implementation of text/plain content type.
 * Utilises a double-hash (##) delimiter to separate fields.
 * Concrete instantiation driven by FormatHandlerFactory.
 */
public class PlainTextFormatHandler implements FormatHandler {
	
    /**  
     * Text field delimiter (##), which is used in both serialisation and deserialisation.
     * */
    private static final String DELIMITER = "##";
 
    /**
     * Serialises all books in the list to a plain-text string, one book per line, separated by ## delimiter.
     * @param - list of books to serialise.
     * @return - multi-line plain text string.
     * @throws - exception.
     */
    @Override
    public String serialise(ArrayList<Book> books) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Book b : books) {
            sb.append(b.getId()).append(DELIMITER)
              .append(b.getTitle()).append(DELIMITER)
              .append(b.getAuthor()).append(DELIMITER)
              .append(b.getDate()).append(DELIMITER)
              .append(b.getGenres()).append(DELIMITER)
              .append(b.getCharacters()).append(DELIMITER)
              .append(b.getSynopsis()).append("\n");
        }
        return sb.toString();
    }
     
    /**
     * Deserialises a single Book from a plain-text string, separated by ## delimiter.
     * @param - raw delimited string from the request body.
     * @return - Book populated with the plain-text string.
     * @throws - IllegalArgumentException if the body contains fewer than 6 fields.
     * @throws - NumberFormatException if a 7-field body has a non-integer ID.
     * @throws - Exception for any other parse error.
     */
    @Override
    public Book deserialise(String body) throws Exception {
        String[] parts = body.split(DELIMITER);
        Book book = new Book();
 
        if (parts.length == 7) {
            // PUT: id present as first field.
            book.setId(Integer.parseInt(parts[0].trim()));
            book.setTitle(parts[1].trim());
            book.setAuthor(parts[2].trim());
            book.setDate(parts[3].trim());
            book.setGenres(parts[4].trim());
            book.setCharacters(parts[5].trim());
            book.setSynopsis(parts[6].trim());
 
        } else if (parts.length == 6) {
            // POST: no ID present.
            book.setTitle(parts[0].trim());
            book.setAuthor(parts[1].trim());
            book.setDate(parts[2].trim());
            book.setGenres(parts[3].trim());
            book.setCharacters(parts[4].trim());
            book.setSynopsis(parts[5].trim());
            
        } else if (parts.length == 1) {
            // DELETE: just the ID required.
            book.setId(Integer.parseInt(parts[0].trim()));
 
        } else {
            throw new IllegalArgumentException(
                "Plain text body must have 1 field (DELETE), 6 fields (POST) or 7 fields (PUT). "
                + "Received: " + parts.length);
        }
 
        return book;
    }
 
    /**
     * Returns code type "text/plain".
     * @return the plain-text MIME type used in HTTP responses.
     */
    @Override
    public String getResponseContentType() {
        return "text/plain";
    }

}
