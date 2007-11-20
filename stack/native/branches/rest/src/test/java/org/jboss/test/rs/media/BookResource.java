package org.jboss.test.rs.media;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;

@XmlRootElement(name = "book")
public class BookResource
{
   private String author;
   private String ISBN;
   private String title;

   public BookResource()
   {
   }

   public BookResource(String author, String ISBN, String title)
   {
      this.author = author;
      this.ISBN = ISBN;
      this.title = title;
   }

   @HttpMethod
   @UriTemplate("author")
   @XmlElement
   public String getAuthor()
   {
      return author;
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   @XmlElement
   public String getISBN()
   {
      return ISBN;
   }

   public void setISBN(String ISBN)
   {
      this.ISBN = ISBN;
   }

   @HttpMethod
   @UriTemplate("title")
   @XmlElement
   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }
}
