package mmu.ac.uk.helpers;

public class ContentTypeHelper {
	
	public static String normalise(String header) {
        if (header == null) return "application/json";
        if (header.contains("application/json")) return "application/json";
        if (header.contains("application/xml") || header.contains("text/xml")) return "application/xml";
        if (header.contains("text/plain")) return "text/plain";
        return "unsupported";
    }

}