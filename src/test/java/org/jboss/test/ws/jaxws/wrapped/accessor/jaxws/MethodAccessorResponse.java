/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.wrapped.accessor.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "methodAccessorResponse", namespace = "http://accessor.wrapped.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "methodAccessorResponse", namespace = "http://accessor.wrapped.jaxws.ws.test.jboss.org/")
public class MethodAccessorResponse {

    private String renamed;

    /**
     *
     * @return
     *     returns String
     */
    @XmlElement(name = "return", namespace = "")
    public String get_return() {
        return this.renamed;
    }

    /**
     *
     * @param _return
     *     the value for the _return property
     */
    public void set_return(String _return) {
        this.renamed = _return;
    }
}
