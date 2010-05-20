// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.jsr181.complex.client;


public class Customer {
    protected org.jboss.test.ws.jsr181.complex.client.Address address;
    protected org.jboss.test.ws.jsr181.complex.client.PhoneNumber[] contactNumbers;
    protected long id;
    protected org.jboss.test.ws.jsr181.complex.client.Name name;

    // since there is no way to differentiate between a null array
    // and an array with 1 element that is null
    protected org.jboss.test.ws.jsr181.complex.client.Customer[] referredCustomers = new Customer[0];

    public Customer() {
    }

    public Customer(org.jboss.test.ws.jsr181.complex.client.Address address, org.jboss.test.ws.jsr181.complex.client.PhoneNumber[] contactNumbers, long id, org.jboss.test.ws.jsr181.complex.client.Name name, org.jboss.test.ws.jsr181.complex.client.Customer[] referredCustomers) {
        this.address = address;
        this.contactNumbers = contactNumbers;
        this.id = id;
        this.name = name;
        this.referredCustomers = referredCustomers;
    }

    public org.jboss.test.ws.jsr181.complex.client.Address getAddress() {
        return address;
    }

    public void setAddress(org.jboss.test.ws.jsr181.complex.client.Address address) {
        this.address = address;
    }

    public org.jboss.test.ws.jsr181.complex.client.PhoneNumber[] getContactNumbers() {
        return contactNumbers;
    }

    public void setContactNumbers(org.jboss.test.ws.jsr181.complex.client.PhoneNumber[] contactNumbers) {
        this.contactNumbers = contactNumbers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public org.jboss.test.ws.jsr181.complex.client.Name getName() {
        return name;
    }

    public void setName(org.jboss.test.ws.jsr181.complex.client.Name name) {
        this.name = name;
    }

    public org.jboss.test.ws.jsr181.complex.client.Customer[] getReferredCustomers() {
        return referredCustomers;
    }

    public void setReferredCustomers(org.jboss.test.ws.jsr181.complex.client.Customer[] referredCustomers) {
        this.referredCustomers = referredCustomers;
    }
}
