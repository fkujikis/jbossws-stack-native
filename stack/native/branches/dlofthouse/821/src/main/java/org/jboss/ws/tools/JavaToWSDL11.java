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
package org.jboss.ws.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.FaultMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.tools.helpers.JavaToWSDLHelper;

/**
 *  Java To WSDL11 Converter
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since   Jul 24, 2005
 */

public class JavaToWSDL11
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaToWSDL11.class);

   // The required wsdl namespace URI
   private final String wsdlNamespace = Constants.NS_WSDL11;

   /** Features as represented by Constants*/
   private HashMap<String, Boolean> features = new HashMap<String, Boolean>();

   // A Map of package/namespace mapping that needs to be passed onto types generator
   private Map<String, String> packageNamespaceMap = null;

   private TypeMapping typeMapping = null;

   private JavaWsdlMapping javaWsdlMapping = null;

   private UnifiedMetaData umd = null;

   private boolean qualifiedElements = false;

   /**
    * Default Constructor
    */
   public JavaToWSDL11()
   {
      log.debug("Creating JavaToWSDL11 instance");
   }

   /**
    * Add a feature to this subsystem
    * @see org.jboss.ws.tools.WSToolsConstants
    * @param name
    * @param value
    */
   public void addFeature(String name, boolean value)
   {
      features.put(name, new Boolean(value));
   }

   /**
    * Append features
    * @see org.jboss.ws.tools.WSToolsConstants
    * @param map
    */
   public void addFeatures(Map<String, Boolean> map)
   {
      features.putAll(map);
   }

   /**
    * Return a feature if set
    * @see org.jboss.ws.tools.WSToolsConstants
    * @param name
    * @return boolean value representing the feature, if not
    * @throws IllegalStateException  Feature unrecognized
    */
   public boolean getFeature(String name)
   {
      Boolean val = features.get(name);
      if (val != null)
         return val.booleanValue();
      throw new WSException("Feature unrecognized");
   }

   /**
    * A customized Package->Namespace map
    *
    * @param map
    */
   public void setPackageNamespaceMap(Map<String, String> map)
   {
      packageNamespaceMap = map;
   }

   /**
    * During the WSDL generation process, a typeMapping will be
    * created that maps xml types -> java types
    *
    * @return  typeMapping
    * @exception IllegalStateException If typeMapping has not been generated
    */
   public TypeMapping getTypeMapping()
   {
      if (typeMapping == null)
         throw new WSException("TypeMapping has not been generated");
      return typeMapping;
   }

   /**
    * Clients of Tools can build a UnifiedMetaData externally
    * and pass it to the Java To WSDL subsystem [Optional]
    *
    * @param um
    */
   public void setUnifiedMetaData(UnifiedMetaData um)
   {
      this.umd = um;
   }

   /**
    *   Generate the common WSDL definition for a given endpoint
    */
   public WSDLDefinitions generate(Class endpoint)
   {
      WSDLDefinitions wsdl = new WSDLDefinitions();
      wsdl.setWsdlNamespace(this.wsdlNamespace);

      if (umd != null)
      {
         JavaToWSDLHelper helper = new JavaToWSDLHelper(wsdl, wsdlNamespace);
         try
         {
            helper.setFeatures(features);
            helper.setPackageNamespaceMap(packageNamespaceMap);
            handleJavaToWSDLGeneration(helper, endpoint.getName());

            typeMapping = helper.getTypeMapping();
         }
         catch (IOException e)
         {
            log.error("Error during Java->WSDL generation:", e);
         }
      }

      // This is somewhat of a hack
      if (qualifiedElements)
      {
         JBossXSModel schemaModel = wsdl.getWsdlTypes().getSchemaModel();
         if (schemaModel != null)
            schemaModel.setQualifiedElements(true);
      }

      return wsdl;
   }

   public boolean isQualifiedElements()
   {
      return qualifiedElements;
   }

   public void setQualifiedElements(boolean qualifiedElements)
   {
      this.qualifiedElements = qualifiedElements;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   //PRIVATE METHODS
   private void handleJavaToWSDLGeneration(JavaToWSDLHelper helper, String endpointName) throws IOException
   {
      if (umd == null)
         throw new WSException("Unified Meta Data Model is null");

      List<ServiceMetaData> servicelist = umd.getServices();
      for (ServiceMetaData smd : servicelist)
      {
         List<EndpointMetaData> endpoints = smd.getEndpoints();
         for (EndpointMetaData endpt : endpoints)
         {
            // FIXME - The API needs to be reworked instead of using hacks like this
            if (!endpointName.equals(endpt.getServiceEndpointInterfaceName()))
               continue;

            // FIXME - We should store the port type
            QName endptName = endpt.getName();
            String intfName = endptName.getLocalPart();

            helper.appendDefinitions(endptName.getNamespaceURI());

            if (intfName.endsWith("Port"))
               intfName = intfName.substring(0, intfName.length() - 4);

            List<OperationMetaData> ops = endpt.getOperations();
            for (OperationMetaData op : ops)
            {
               //Generate Types for the individual parameters
               List<ParameterMetaData> pmds = op.getParameters();
               for (ParameterMetaData pmd : pmds)
                  helper.generateTypesForXSD(pmd);

               List<FaultMetaData> fmds = op.getFaults();
               for (FaultMetaData fmd : fmds)
               {
                  helper.generateTypesForXSD(fmd);
               }

               ParameterMetaData pmd = op.getReturnParameter();
               if (pmd != null)
                  helper.generateTypesForXSD(pmd);

               helper.generateInterfaces(op, intfName);
               helper.generateBindings(op, intfName + "Binding");
            }

            helper.generateServices(endpt, intfName);
            javaWsdlMapping = helper.getJavaWsdlMapping();
            break;
         }
      }
   }
}