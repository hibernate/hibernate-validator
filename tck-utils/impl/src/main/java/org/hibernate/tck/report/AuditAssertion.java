package org.hibernate.tck.report;

/**
 * Represents a single assertion as defined in the audit xml 
 * 
 * @author Shane Bryzak
 *
 */
public class AuditAssertion implements Comparable<AuditAssertion>
{
   private String section;
   private String id;
   private String text;
   private String note;
   private boolean testable;
   private boolean implied;
   
   public AuditAssertion(String section, String id, String text, String note, 
         boolean testable, boolean implied)
   {
      this.section = section;
      this.id = id;
      this.text = text;
      this.note = note;
      this.testable = testable;
      this.implied = implied;
   }
   
   public String getSection()
   {
      return section;
   }
   
   public String getId()
   {
      return id;
   }
   
   public String getText()
   {
      return text;
   }
   
   public String getNote()
   {
      return note;
   }
   
   public boolean isTestable()
   {
      return testable;
   }
   
   public boolean isImplied()
   {
      return implied;
   }

   public int compareTo(AuditAssertion other)
   {            
      int i = section.compareTo(other.section);      
      return i != 0 ? i : id.compareTo(other.id);
   }
   
}
