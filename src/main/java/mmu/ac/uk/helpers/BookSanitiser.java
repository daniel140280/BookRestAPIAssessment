package mmu.ac.uk.helpers;

import mmu.ac.uk.models.Book;


/**
 * Provides sanitisation and normalisation for Book objects before inserting or updating the database.
 * Date field is converted consistently to the DD/MM/YYYY format using DateHelper
 * This helper prevents duplication of sanitisation logic across DAO methods.
 */
public class BookSanitiser {
	
	/**
     * Normalises and cleans a Book object's fields.
     *
     * @param b - Book to sanitise
     * @return - Book instance with consistent date fields
     */
	public static Book sanitise(Book b) {
	    b.setDate(DateHelper.normalise(b.getDate()));
	    return b;
	}

}
