package mmu.ac.uk.models;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)  
@XmlRootElement(name = "books") 
public class BookList {
	
	@XmlElement(name = "book")
	private List<Book> booksList;
	
	public BookList() {}
	
	public BookList(List<Book> booksList) {
		this.booksList = booksList;
	}
	
	public List<Book> getBooksList() {
		return booksList;
	}
		
	public void setBooksList(List<Book> booksList) {
		this.booksList = booksList;
	}

}
