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
package org.jboss.test.ws.jaxws.jbws1702.types;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organisation: Marabu EDV</p>
 * @author strauch
 * @version     1.0
 */
@XmlSeeAlso({ClassC.class})
public class ClassB extends ClassA {

  private String propB;

  /**
   * Creates a new instance of ClassB
   */
  public ClassB() {
  }

  public String getPropB() {
    return propB;
  }

  public void setPropB(String propB) {
    this.propB = propB;
  }

}
