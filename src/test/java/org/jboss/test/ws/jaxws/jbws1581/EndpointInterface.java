/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Thu Nov 02 21:17:37 CET 2006
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jaxws.jbws1581;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)

/**
 * The SEI that is packaged with the war endpoint may potentially
 * contain different annotations that the one packaged with the EJB client
 */
public interface EndpointInterface
{
   String hello(String msg);
}
