package org.hibernate.tck.report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parsing utilities for tck-audit.xml
 * 
 * @author Shane Bryzak
 * 
 */
public class AuditParser
{
   private String version;
   
   private Map<String,List<AuditAssertion>> assertions = new HashMap<String,List<AuditAssertion>>();
   
   private Map<String,String> titles = new HashMap<String,String>();   
   
   private InputStream source;
   
   public AuditParser(InputStream source)
   {
      this.source = source;
   }   
   
   public String getVersion()
   {
      return version;
   }
   
   public String getSectionTitle(String sectionId)
   {
      return titles.get(sectionId);
   }
   
   public Map<String,List<AuditAssertion>> getAssertions()
   {
      return assertions;
   }
   
   public List<String> getSectionIds()
   {
      List<String> sectionIds = new ArrayList<String>(assertions.keySet());
      
      Collections.sort(sectionIds, new Comparator<String>() {
         public int compare(String value1, String value2)
         {
            String[] parts1 = value1.split("[.]");
            String[] parts2 = value2.split("[.]");
            
            for (int i = 0;;i++)
            {                              
               if (parts1.length < (i + 1)) 
               {
                  return parts2.length < (i + 1) ? 0 : 0;
               }
               else if (parts2.length < (i + 1))
               {
                  return parts1.length < (i + 1) ? 0 : 1;
               }

               try
               {
                  int val1 = Integer.parseInt(parts1[i]);
                  int val2 = Integer.parseInt(parts2[i]);
                  
                  if (val1 != val2) return val1 - val2;
               }
               catch (NumberFormatException ex)
               {
                  int comp = parts1[i].compareTo(parts2[i]);
                  if (comp != 0) 
                  {
                     return comp;
                  }                  
               }                              
            }            
         }
      });
      
      return sectionIds;
   }
   
   /**
    * Returns a sorted list of assertions for the specified section ID
    * 
    * @param sectionId
    * @return
    */
   public List<AuditAssertion> getAssertionsForSection(String sectionId)
   {
      List<AuditAssertion> sectionAssertions = new ArrayList<AuditAssertion>(assertions.get(sectionId));
      Collections.sort(sectionAssertions);
      return sectionAssertions;
   }
   
   /**
    * 
    * @param sectionId
    * @param assertionId
    * @return
    */
   public boolean hasAssertion(String sectionId, String assertionId)
   {            
      if (!assertions.containsKey(sectionId)) 
      {
         return false;
      }
      
      for (AuditAssertion assertion : assertions.get(sectionId))
      {
         if (assertion.getId().equals(assertionId)) return true;
      }
      
      return false;
   }
   
   /**
    * Load the spec assertions defined in tck-audit.xml 
    */
   public void parse() throws Exception
   {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      
      Document doc = builder.parse(source);
      NodeList sectionNodes = doc.getDocumentElement().getChildNodes();
      
      version = doc.getDocumentElement().getAttribute("version");
      
      for (int i = 0; i < sectionNodes.getLength(); i++)
      {         
         if (sectionNodes.item(i) instanceof Element && 
             "section".equals(sectionNodes.item(i).getNodeName()))
         {
            processSectionNode((Element) sectionNodes.item(i));
         }
      }
   }
   
   private void processSectionNode(Element node)
   {
      String sectionId = node.getAttribute("id");
      titles.put(sectionId, node.getAttribute("title"));
      
      assertions.put(sectionId, new ArrayList<AuditAssertion>());
      
      NodeList assertionNodes = node.getChildNodes();
      
      for (int i = 0; i < assertionNodes.getLength(); i++)
      {
         if (assertionNodes.item(i) instanceof Element && 
             "assertion".equals(assertionNodes.item(i).getNodeName()))
         {
            processAssertionNode(sectionId, (Element) assertionNodes.item(i));            
         }
      }            
   }
   
   private void processAssertionNode(String sectionId, Element node)
   {      
      List<AuditAssertion> value = assertions.get(sectionId);
                        
      String text = null;
      String note = null;
      
      for (int i = 0; i < node.getChildNodes().getLength(); i++)
      {
         Node child = node.getChildNodes().item(i);
         
         if (child instanceof Element)
         {
            if ("text".equals(child.getNodeName()))
            {
               text = child.getTextContent();
            }
            else if ("note".equals(child.getNodeName()))
            {
               note = child.getTextContent();
            }              
         }                   
      }
      
      boolean testable = node.hasAttribute("testable") ? 
            Boolean.parseBoolean(node.getAttribute("testable")) : true;
            
      boolean implied = node.hasAttribute("implied") ?
            Boolean.parseBoolean(node.getAttribute("implied")) : false;
      
      value.add(new AuditAssertion(sectionId, 
            node.getAttribute("id"), text, note, testable, implied));     
   }
}
