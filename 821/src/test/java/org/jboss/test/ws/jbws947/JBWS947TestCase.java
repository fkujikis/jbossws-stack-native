package org.jboss.test.ws.jbws947;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.jbws947.Items.Item;
import org.jboss.util.xml.DOMUtils;
import org.w3c.dom.Element;

/*
 * NoClassDefFoundError: javax/xml/bind/JAXBException
 * 
 * http://jira.jboss.org/jira/browse/JBWS-947
 *
 * @author Thomas.Diesler@jboss.org
 * @author ryan_shoemaker (jaxb)
 * @since 1-Jun-2006
 */
public class JBWS947TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS947TestCase.class, "jbossws-jbws947.war");
   }
   
   public void testMarshall() throws Exception
   {
      JAXBContext jc = JAXBContext.newInstance("org.jboss.test.ws.jbws947");

      PurchaseOrderType po = new PurchaseOrderType();
      po.setShipTo(createUSAddress("Alice Smith", "123 Maple Street", "Cambridge", "MA", "12345", "US"));
      po.setBillTo(createUSAddress("Robert Smith", "8 Oak Avenue", "Cambridge", "MA", "12345", "US"));
      //po.setOrderDate(getDate());

      Items items = new Items();
      po.setItems(items);

      List<Items.Item> itemList = items.getItem();
      itemList.add(createItem("Nosferatu - Special Edition (1929)", 5, new BigDecimal("19.99"), null, null, "242-NO"));
      itemList.add(createItem("The Mummy (1959)", 3, new BigDecimal("19.98"), null, null, "242-MU"));
      itemList.add(createItem("Godzilla and Mothra: Battle for Earth/Godzilla vs. King Ghidora", 3, new BigDecimal("27.95"), null, null, "242-GZ"));

      JAXBElement<PurchaseOrderType> poElement = (new ObjectFactory()).createPurchaseOrder(po);

      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //m.marshal(poElement, System.out);
      m.marshal(poElement, baos);

      Element exp = DOMUtils.parse(new FileInputStream("resources/jbws947/po.xml"));
      Element was = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()));
      assertEquals(exp, was);
   }

   public void testUnmarshall() throws Exception
   {
      JAXBContext jc = JAXBContext.newInstance("org.jboss.test.ws.jbws947");
      Unmarshaller u = jc.createUnmarshaller();

      JAXBElement poElement = (JAXBElement)u.unmarshal(new FileInputStream("resources/jbws947/po.xml"));
      PurchaseOrderType po = (PurchaseOrderType)poElement.getValue();
      
      List<Item> items = po.getItems().getItem();
      assertEquals(3, items.size());
   }

   public void testMessageEndpoint() throws Exception
   {
      /*MessageFactory factory = MessageFactory.newInstance();
      
      SOAPMessage reqMsg = factory.createMessage();
      Element po = DOMUtils.parse(new FileInputStream("resources/jbws947/po.xml"));
      reqMsg.getSOAPBody().addChildElement(new SOAPFactoryImpl().createElement(po, true));
      
      String soapAddress = "http://" + getServerHost() + ":8080/jbossws-jbws947/MessageEndpoint";
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, soapAddress);
      
      String expStr = "<ns1:processElementResponse xmlns:ns1='http://org.jboss.test.ws/jbws947'>3</ns1:processElementResponse>";
      assertEquals(DOMUtils.parse(expStr), (Element)resMsg.getSOAPBody().getChildElements().next());
      */

      System.out.println("FIXME: JBWS-947");
   }
   
   private USAddress createUSAddress(String name, String street, String city, String state, String zip, String country)
   {
      USAddress address = new USAddress();
      address.setName(name);
      address.setStreet(street);
      address.setCity(city);
      address.setState(state);
      address.setZip(new BigDecimal(zip));
      address.setCountry(country);
      return address;
   }

   private Items.Item createItem(String productName, int quantity, BigDecimal price, String comment, XMLGregorianCalendar shipDate, String partNum)
   {
      Items.Item item = new Items.Item();
      item.setProductName(productName);
      item.setQuantity(quantity);
      item.setUSPrice(price);
      item.setComment(comment);
      item.setShipDate(shipDate);
      item.setPartNum(partNum);
      return item;
   }

   private XMLGregorianCalendar getDate() throws DatatypeConfigurationException
   {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(1999, 10, 22));
   }
}
