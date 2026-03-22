package mmu.ac.uk.serialisationhandler;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
 
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
 
import mmu.ac.uk.models.Book;
import mmu.ac.uk.models.BookList;


/**
 * Implementation of application/xml content type.
 * Concrete instantiation driven by FormatHandlerFactory.
 * Uses JAXB to marshall a list of Book objects wrapped in a BookList element, then unmarshall an individUal Book from an XML request body.
 */
public class XmlFormatHandler implements FormatHandler {
		
	/**
     * Serialises all books in the list to a formatted XML document.
     * @param - list of books to serialise.
     * @return - XML string.
     * @throws - exception such as JAXB marshalling fails.
     */
    @Override
    public String serialise(ArrayList<Book> books) throws Exception {
        BookList bookList = new BookList(books);
        StringWriter sw = new StringWriter();
 
        JAXBContext context = JAXBContext.newInstance(BookList.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(bookList, sw);
 
        return sw.toString();
    }
 
    /**
     * Deserialises a single Book from an XML request body.
     * @param - raw XML string from the request body.
     * @return - Book populated from the XML.
     * @throws - exception such as JAXB unmarshalling fails.
     */
    @Override
    public Book deserialise(String body) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Book.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Book) unmarshaller.unmarshal(new StringReader(body));
    }
 
    /**
     * Returns code type "text/xml".
     * @return the XML MIME type used in HTTP responses.
     */
    @Override
    public String getResponseContentType() {
        return "text/xml";
    }

}
