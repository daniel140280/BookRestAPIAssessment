package mmu.ac.uk.serialisationhandler;

import mmu.ac.uk.helpers.ContentTypeHelper;

/**
 * Factory (and Facade) that resolves the correct
 * {@link FormatHandler} for a given HTTP {@code Accept} or
 * {@code Content-Type} header value.
 *
 * <h2>Design patterns in use</h2>
 * <ul>
 *   <li><strong>Factory</strong> — {@link #getHandler(String)} centralises the
 *       creation logic for all {@link FormatHandler} implementations. The
 *       servlet never calls {@code new JsonFormatHandler()} directly; it always
 *       goes through this factory. Adding a new format requires only a new
 *       implementation class and a single new {@code case} here.</li>
 *   <li><strong>Strategy</strong> — each concrete handler ({@link JsonFormatHandler},
 *       {@link XmlFormatHandler}, {@link PlainTextFormatHandler}) is an
 *       interchangeable algorithm for the same serialise/deserialise
 *       operations, selected at runtime.</li>
 *   <li><strong>Facade</strong> — the servlet calls one method
 *       ({@code FormatHandlerFactory.getHandler(header)}) and receives a
 *       ready-to-use handler. All decisions about which class to instantiate,
 *       and all header normalisation via {@link ContentTypeHelper}, are hidden
 *       behind this single entry point.</li>
 * </ul>
 */
public class FormatHandlerFactory {
	
	/**
     * Private constructor — not to be instantiated.
     */
    private FormatHandlerFactory() {}
 
    /**
     * Returns the appropriate FormatHandler (instantiation) that matches the client request.
     * @param - raw Accept or Content-Type header value from the HTTP request.
     * @return - matching FormatHandler, or null if unsupported.
     */
    public static FormatHandler getHandler(String header) {

        String normalised = ContentTypeHelper.normalise(header);
 
        switch (normalised) {
            case "application/json":
            	return new JsonFormatHandler();
            case "application/xml":
            	return new XmlFormatHandler();
            case "text/plain":
            	return new PlainTextFormatHandler();
            default:
            	return null; 
        }
    }

}
