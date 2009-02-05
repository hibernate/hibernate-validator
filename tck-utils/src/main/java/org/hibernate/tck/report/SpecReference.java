package org.hibernate.tck.report;

/**
 * Represents the metadata for a single instance of @SpecAssertion
 * 
 * @author Shane Bryzak
 */
public class SpecReference
{
   private String section;
   private String assertion;
   private String className;
   private String methodName;
   
   SpecReference(String section, String assertion, String className, String methodName)
   {
      this.section = section;
      this.assertion = assertion;
      this.className = className;
      this.methodName = methodName;
   }
   
   public String getSection()
   {
      return section;
   }
   
   public String getAssertion()
   {
      return assertion;
   }
   
   public String getClassName()
   {
      return className;
   }
   
   public String getMethodName()
   {
      return methodName;
   }
}
