package org.jboss.test.rs.media;

import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.ProduceMime;
import java.util.List;
import java.util.ArrayList;

@UriTemplate("books")
public class BookStoreResource
{
   
   private List<BookResource> availableBooks = new ArrayList<BookResource>();

   public BookStoreResource()
   {
      availableBooks.add( new BookResource("Leonard Richardson", "596529260", "RESTful Web Services") );
      availableBooks.add( new BookResource("Sam Ruby", "3897217279", "Web Services mit REST") );
   }

   @UriTemplate("{isbn}")
   public BookResource getBookByISBN(
     @UriParam("isbn")
     String isbn)
   {
      BookResource match = null;

      for(BookResource book : availableBooks)
      {
         if(book.getISBN().equals(isbn))
         {
            match = book;
            break;
         }
      }

      return match;
   }

   @HttpMethod
   @ProduceMime("text/xml")
   public List<BookResource> getAllBooks()
   {
      return availableBooks;
   }
}
