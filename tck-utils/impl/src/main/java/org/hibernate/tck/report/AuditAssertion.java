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
   
   public AuditAssertion(String section, String id, String text, String note)
   {
      this.section = section;
      this.id = id;
      this.text = text;
      this.note = note;
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

   public int compareTo(AuditAssertion other)
   {            
      int i = section.compareTo(other.section);      
      return i != 0 ? i : id.compareTo(other.id);
   }
   
   
}
