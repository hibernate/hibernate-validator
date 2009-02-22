package org.hibernate.tck.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.tck.config.RuntimeProperties;

/**
 * Generates the TCK spec coverage report
 *
 * @author Shane Bryzak
 */
public class CoverageReport {
   
   public static final String FISHEYE_BASE_URL_PROPERTY = "fisheye_base_url";
   
   public static final String SVN_BASE_URL_PROPERTY = "svn_base_url";
   
    /*
    * References to the spec assertions made by the tck tests
    */
    private final Map<String, List<SpecReference>> references;

    private AuditParser auditParser;
    
    private RuntimeProperties properties;
    
    private String fisheyeBaseUrl = null;
    
    private String svnBaseUrl = null;
    
    private List<SpecReference> unmatched;
    
    private int failThreshold;
    private int passThreshold;

    public CoverageReport(List<SpecReference> references, AuditParser auditParser) {
        this.references = new HashMap<String, List<SpecReference>>();

        for (SpecReference ref : references) {
            if (!this.references.containsKey(ref.getSection())) {
                this.references.put(ref.getSection(), new ArrayList<SpecReference>());
            }

            this.references.get(ref.getSection()).add(ref);
        }

        this.auditParser = auditParser;
        
        this.properties = new RuntimeProperties();
        
        try
        {
           fisheyeBaseUrl = this.properties.getStringValue(FISHEYE_BASE_URL_PROPERTY, null, false);
           
           if (!fisheyeBaseUrl.endsWith("/"))
           {
              fisheyeBaseUrl = fisheyeBaseUrl + "/";
           }
           
           svnBaseUrl = this.properties.getStringValue(SVN_BASE_URL_PROPERTY, null, false);
           
           if (!svnBaseUrl.endsWith("/"))
           {
              svnBaseUrl = svnBaseUrl + "/";
           }
           
           passThreshold = this.properties.getIntValue("pass_threshold", 75, false);
           failThreshold = this.properties.getIntValue("fail_threshold", 50, false);
        }
        catch (Exception ex)
        {
           // swallow
        }        
    }

    public void generate(OutputStream out) throws IOException {
        calculateUnmatched();
        writeHeader(out);
        writeContents(out);
        writeMasterSummary(out);
        writeChapterSummary(out);
        writeSectionSummary(out);
        writeCoverage(out);
        writeUnmatched(out);
        writeFooter(out);
    }
    
    private void calculateUnmatched()
    {
       unmatched = new ArrayList<SpecReference>();

       for (String sectionId : references.keySet()) {
           for (SpecReference ref : references.get(sectionId)) {
               if (!auditParser.hasAssertion(ref.getSection(), ref.getAssertion())) {
                   unmatched.add(ref);
               }
           }
       }       
    }

    private void writeHeader(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n");
        sb.append("\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        sb.append("<html>\n");
        sb.append("<head><title>JSR-299 TCK Coverage Report</title>\n");

        sb.append("<style type=\"text/css\">\n");
        sb.append("  body {\n");
        sb.append("   font-family: verdana, arial, sans-serif;\n");
        sb.append("   font-size: 11px; }\n");
        sb.append("  .code {\n");
        sb.append("    float: left;\n");
        sb.append("    font-weight: bold;\n");
        sb.append("    width: 50px;\n");
        sb.append("    margin-top: 0px;\n");
        sb.append("    height: 100%; }\n");
        sb.append("   a.external, a.external:visited, a.external:hover {\n");
        sb.append("    color: #0000ff;\n");
        sb.append("    font-size: 9px;\n");
        sb.append("    font-style: normal;\n");
        sb.append("    padding-left: 2px;\n");
        sb.append("    margin-left: 6px;\n");
        sb.append("    margin-right: 6px;\n");
        sb.append("    padding-right: 2px; }\n");       
        sb.append("  .results {\n");
        sb.append("    margin-left: 50px; }\n");
        sb.append("  .description {\n");
        sb.append("    margin-top: 2px;\n");
        sb.append("    margin-bottom: 2px; }\n");
        sb.append("  .sectionHeader {\n");
        sb.append("    border-bottom: 1px solid #cccccc;\n");
        sb.append("    margin-top: 8px;\n");
        sb.append("    font-weight: bold; }\n");
        sb.append("  .packageName {\n");
        sb.append("   color: #999999;\n");
        sb.append("   font-size: 9px;\n");
        sb.append("   font-weight: bold; }\n");
        sb.append("  .noCoverage {\n");
        sb.append("   margin-top: 2px;\n");
        sb.append("   margin-bottom: 2px;\n");
        sb.append("   font-weight: bold;\n");
        sb.append("   font-style: italic;\n");
        sb.append("   color: #ff0000; }\n");
        sb.append("  .coverageHeader {\n");
        sb.append("   font-weight: bold;\n");
        sb.append("   text-decoration: underline;\n");
        sb.append("   margin-top: 2px;\n");
        sb.append("   margin-bottom: 2px; }\n");
        sb.append("  .coverageMethod {\n");
        sb.append("   font-style: italic; }\n");
        sb.append("  .implied {\n");        
        sb.append("    color: #fff;\n");
        sb.append("    font-weight: bold;\n");
        sb.append("    background-color: #000; }\n");
        sb.append("  .pass {\n");
        sb.append("    border-top: 1px solid #488c41;\n");
        sb.append("    border-bottom: 1px solid #488c41;\n");
        sb.append("    padding-bottom: 1px;\n");
        sb.append("    margin-bottom: 2px;\n");
        sb.append("    background-color: #dfd; }\n");
        sb.append("  .fail {\n");
        sb.append("    border-top: 1px solid #ab2020;\n");
        sb.append("    border-bottom: 1px solid #ab2020;\n");
        sb.append("    padding-bottom: 1px;\n");
        sb.append("    margin-bottom: 2px;\n");
        sb.append("    background-color: #fdd; }\n");
        sb.append("  .untestable {\n");
        sb.append("    padding-bottom: 16px;\n");
        sb.append("    margin-bottom: 2px;\n");
        sb.append("    border-top: 1px solid #317ba6;\n");
        sb.append("    border-bottom: 1px solid #317ba6;\n");
        sb.append("    background-color: #80d1ff; }\n");        

        sb.append("</style>\n");

        sb.append("</head><body>");
        sb.append("<h1>JSR-299 TCK Coverage</h1>");
        sb.append("<h2>");
        sb.append(auditParser.getVersion());
        sb.append("</h2>\n");

        out.write(sb.toString().getBytes());
    }
    
    private void writeContents(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3>Contents</h3>");
       sb.append("<div><a href=\"#masterSummary\">Master Summary</a></div>");
       sb.append("<div><a href=\"#chapterSummary\">Chapter Summary</a></div>");
       sb.append("<div><a href=\"#sectionSummary\">Section Summary</a></div>");
       sb.append("<div><a href=\"#coverageDetail\">Coverage Detail</a></div>");
       sb.append("<div><a href=\"#unmatched\">Unmatched Tests</a></div>");
       
       out.write(sb.toString().getBytes());
    }
    
    private void writeMasterSummary(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3 id=\"masterSummary\">Master Summary</h3>\n");
       
       sb.append("<table border=\"0\">");
       
       sb.append("<tr>");
       sb.append("<td>Total number of assertions</td>");
       sb.append("<td>");
       
       int assertionTotal = 0;
       int testableAssertionTotal = 0;
       
       for (List<AuditAssertion> assertions : auditParser.getAssertions().values())
       {
          assertionTotal += assertions.size();
          
          for (AuditAssertion a : assertions)
          {
             if (a.isTestable()) testableAssertionTotal++;
          }
       }
       
       double untestablePercent = assertionTotal > 0 ? (((assertionTotal - testableAssertionTotal) * 1.0) / assertionTotal) * 100 : 100;
       
       sb.append(assertionTotal);
       sb.append("</td>");
       sb.append("</tr>");

       sb.append("<tr>");
       sb.append("<td>Total number of untestable assertions</td>");
       sb.append("<td>");
       sb.append(assertionTotal - testableAssertionTotal).append(" (").append(String.format("%.2f%%", untestablePercent)).append(")");
       sb.append("</td>");
       sb.append("</tr>");       
       
       int coverage = 0;
       
       for (String sectionId : auditParser.getSectionIds())
       {
          for (AuditAssertion assertion : auditParser.getAssertionsForSection(sectionId))
          {
             if (!getCoverageForAssertion(sectionId, assertion.getId()).isEmpty())
             {
                coverage++;
             }
          }
       }

       double coveragePercent = testableAssertionTotal > 0 ? ((coverage * 1.0) / testableAssertionTotal) * 100 : 100;
       String bgColor = coveragePercent < failThreshold ? "#ffaaaa" : 
          coveragePercent < passThreshold ? "#ffffaa" : "#aaffaa";
       
       sb.append("<tr style=\"background-color:" + bgColor + "; border-color: " + bgColor + "\">");
       
       sb.append("<td style=\"background-color:" + bgColor + "; border-color: " + bgColor + "\">Total number of tested assertions</td>");
       sb.append("<td align=\"center\" style=\"background-color:" + bgColor + "; border-color: " + bgColor + "\">");       
       
       sb.append(coverage).append(" (");
       
       sb.append(String.format("%.2f%%", coveragePercent));
       sb.append(")</td>");
              
       sb.append("</tr>");      
       
       sb.append("<tr>");
       sb.append("<td>Total number of unmatched tests</td>");
       
       sb.append("<td>");
       sb.append(unmatched.size());
       sb.append("</td>");
              
       sb.append("</tr>");        
       
       sb.append("</table>");
       
       out.write(sb.toString().getBytes());
    }
    
    private void writeChapterSummary(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3 id=\"chapterSummary\">Chapter Summary</h3>\n");

       sb.append("<table width=\"100%\">");
       
       sb.append("<tr style=\"background-color:#dddddd\">");
       sb.append("<th align=\"left\">Chapter</th>");
       sb.append("<th>Assertions</th>");
       sb.append("<th>Testable</th>");
       sb.append("<th>Tested</th>");
       sb.append("<th>Coverage %</th>");
       sb.append("</tr>");
       
       boolean odd = true;
              
       for (String sectionId : auditParser.getSectionIds()) {
          
          // Chapters have no .'s in their id
         if (sectionId.split("[.]").length == 1)
         {
            String prefix = sectionId + ".";
            
            int assertions = auditParser.getAssertionsForSection(sectionId).size();
            int testable = 0;
            int coverage = 0;                     
            
            for (AuditAssertion assertion : auditParser.getAssertionsForSection(sectionId))
            {
               if (assertion.isTestable()) testable++;
               
               if (!getCoverageForAssertion(sectionId, assertion.getId()).isEmpty())
               {
                  coverage++;
               }
            }             
            
            // Gather stats here
            for (String subSectionId : auditParser.getSectionIds())
            {
               if (subSectionId.startsWith(prefix))
               {
                  assertions += auditParser.getAssertionsForSection(subSectionId).size();
                                    
                  for (AuditAssertion assertion : auditParser.getAssertionsForSection(subSectionId))
                  {
                     if (assertion.isTestable()) testable++;                     
                     
                     if (!getCoverageForAssertion(subSectionId, assertion.getId()).isEmpty())
                     {
                        coverage++;
                     }
                  }                  
               }
            }
            
            if (odd)
            {
               sb.append("<tr style=\"background-color:#f7f7f7\">");
            }
            else
            {
               sb.append("<tr>");
            }
            
            odd = !odd;
            
            int margin = (sectionId.split("[.]").length - 1) * 16;         
            
            sb.append("<td style=\"padding-left:" + margin + "px\">");
            sb.append("<a href=\"#" + sectionId + "\">");
            sb.append(sectionId);
            sb.append(" ");
            sb.append(auditParser.getSectionTitle(sectionId));
            sb.append("</a>");
            sb.append("</td>");
                        
            double coveragePercent = assertions > 0 ? ((coverage * 1.0) / assertions) * 100 : -1;
            
            sb.append("<td align=\"center\">");
            sb.append(assertions);
            sb.append("</td>");
            
            sb.append("<td align=\"center\">");
            sb.append(testable);
            sb.append("</td>");
            
            sb.append("<td align=\"center\">");
            sb.append(coverage);
            sb.append("</td>");
            
            if (coveragePercent >= 0)
            {
               String bgColor = coveragePercent < 60 ? "#ffaaaa" : coveragePercent < 80 ? "#ffffaa" : "#aaffaa" ;
            
               sb.append("<td align=\"center\" style=\"background-color:" + bgColor + "\">");
               sb.append(String.format("%.2f%%", coveragePercent));
               sb.append("</td>");
            }
            else
            {
               sb.append("<td />");
            }

            sb.append("</tr>");                        
            
         }
          
       }

       sb.append("</table>");       
       out.write(sb.toString().getBytes());        
    }

    private void writeSectionSummary(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3 id=\"sectionSummary\">Section Summary</h3>\n");

       sb.append("<table width=\"100%\">");
       
       sb.append("<tr style=\"background-color:#dddddd\">");
       sb.append("<th align=\"left\">Section</th>");
       sb.append("<th>Assertions</th>");
       sb.append("<th>Testable</th>");
       sb.append("<th>Tested</th>");
       sb.append("<th>Coverage %</th>");
       sb.append("</tr>");
       
       boolean odd = true;
              
       for (String sectionId : auditParser.getSectionIds()) {
          
         if (odd)
         {
            sb.append("<tr style=\"background-color:#f7f7f7\">");
         }
         else
         {
            sb.append("<tr>");
         }
         
         odd = !odd;
         
         int margin = (sectionId.split("[.]").length - 1) * 16;         
         
         sb.append("<td style=\"padding-left:" + margin + "px\">");
         sb.append("<a href=\"#" + sectionId + "\">");
         sb.append(sectionId);
         sb.append(" ");
         sb.append(auditParser.getSectionTitle(sectionId));
         sb.append("</a>");
         sb.append("</td>");
         
         int assertions = auditParser.getAssertionsForSection(sectionId).size();
         int testable = 0;         
         int coverage = 0;
         
         for (AuditAssertion assertion : auditParser.getAssertionsForSection(sectionId))
         {
            if (assertion.isTestable()) testable++;
            
            if (!getCoverageForAssertion(sectionId, assertion.getId()).isEmpty())
            {
               coverage++;
            }
         }
         
         double coveragePercent = assertions > 0 ? ((coverage * 1.0) / assertions) * 100 : -1;
         
         sb.append("<td align=\"center\">");
         sb.append(assertions);
         sb.append("</td>");
         
         sb.append("<td align=\"center\">");
         sb.append(testable);
         sb.append("</td>");
         
         sb.append("<td align=\"center\">");
         sb.append(coverage);
         sb.append("</td>");
         
         if (coveragePercent >= 0)
         {
            String bgColor = coveragePercent < 60 ? "#ffaaaa" : coveragePercent < 80 ? "#ffffaa" : "#aaffaa" ;
         
            sb.append("<td align=\"center\" style=\"background-color:" + bgColor + "\">");
            sb.append(String.format("%.2f%%", coveragePercent));
            sb.append("</td>");
         }
         else
         {
            sb.append("<td />");
         }

         sb.append("</tr>");
       }

       sb.append("</table>");       
       out.write(sb.toString().getBytes());       
    }
    
    private void writeCoverage(OutputStream out) throws IOException {
       
        out.write("<h3 id=\"coverageDetail\">Coverage Detail</h3>\n".getBytes());
       
        for (String sectionId : auditParser.getSectionIds()) {

            List<AuditAssertion> sectionAssertions = auditParser.getAssertionsForSection(sectionId);

            if (sectionAssertions != null && !sectionAssertions.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                out.write(("<h4 class=\"sectionHeader\" id=\"" + sectionId + "\">Section " + 
                      sectionId + " - " +
                      auditParser.getSectionTitle(sectionId) + "</h4>\n").getBytes());                
                
                for (AuditAssertion assertion : sectionAssertions) {
                    List<SpecReference> coverage = getCoverageForAssertion(sectionId, assertion.getId());

                    String divClass = null;
                    
                    if (assertion.isTestable())
                    {
                       if (coverage.isEmpty())
                       {
                          divClass = "fail";
                       }
                       else
                       {
                          divClass = "pass";
                       }
                    }
                    else
                    {
                       divClass = "untestable";
                    }
                    
                    sb.append("  <div class=\"" + divClass + "\">\n");

                    if (assertion.isImplied())
                    {
                       sb.append("<span class=\"implied\">The following assertion is not made explicitly by the spec, however it is implied</span>");
                    }
                    
                    sb.append("    <span class=\"code\">");
                    sb.append(assertion.getId());
                    sb.append(")");
                    sb.append("</span>\n");

                    sb.append("    <div class=\"results\">");
                    sb.append("<p class=\"description\">");
                    sb.append(assertion.getText());
                    sb.append("</p>\n");

                    if (assertion.isTestable())
                    {
                       sb.append("    <div class=\"coverage\">\n");
                       sb.append("      <p class=\"coverageHeader\">Coverage</p>\n");
                       
                       String currentPackageName = null;                    
   
                       if (coverage.isEmpty()) {
                           sb.append("        <p class=\"noCoverage\">No tests exist for this assertion</p>\n");
                       } else {
                           for (SpecReference ref : coverage) {
                               if (!ref.getPackageName().equals(currentPackageName))
                               {
                                  currentPackageName = ref.getPackageName();
                                  sb.append("        <div class=\"packageName\">");
                                  sb.append(currentPackageName);
                                  sb.append("        </div>\n");                               
                               }                           
                             
                               sb.append("        <div class=\"coverageMethod\">");
                               sb.append(ref.getClassName());
                               sb.append(".");
                               sb.append(ref.getMethodName());
                               sb.append("()");
                               
                               if (fisheyeBaseUrl != null)
                               {                               
                                  sb.append("<a class=\"external\" target=\"_blank\" href=\"");
                                  sb.append(fisheyeBaseUrl);
                                  sb.append(currentPackageName.replace('.', '/'));
                                  sb.append("/");
                                  sb.append(ref.getClassName());
                                  sb.append(".java");
                                  sb.append("\">fisheye</a>");
                               }
                               
                               if (svnBaseUrl != null)
                               {
                                  if (fisheyeBaseUrl != null)
                                  {
                                     sb.append("|");                                  
                                  }
                                  
                                  sb.append("<a class=\"external\" target=\"_blank\" href=\"");
                                  sb.append(svnBaseUrl);
                                  sb.append(currentPackageName.replace('.', '/'));
                                  sb.append("/");
                                  sb.append(ref.getClassName());
                                  sb.append(".java");
                                  sb.append("\">svn</a>");                               
                               }
                               
                               sb.append("</div>\n");
                           }
                       }
   
                       sb.append("    </div>\n");
                    }
                    
                    sb.append("</div></div>");
                }

                out.write(sb.toString().getBytes());
            }
        }
    }

    private void writeUnmatched(OutputStream out) throws IOException {
        if (unmatched.isEmpty()) return;

        StringBuilder sb = new StringBuilder();

        sb.append("<h3 id=\"unmatched\">Unmatched tests</h3>\n");
        sb.append(String.format("<p>The following %d tests do not match any known assertions:</p>",
                unmatched.size()));

        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"0\">\n");
        sb.append("  <tr><th>Section</th><th>Assertion</th><th>Test Class</th><th>Test Method</th></tr>\n");

        for (SpecReference ref : unmatched) {
            sb.append("<tr>");

            sb.append("<td>");
            sb.append(ref.getSection());
            sb.append("</td>");

            sb.append("<td>");
            sb.append(ref.getAssertion());
            sb.append("</td>");

            sb.append("<td>");
            sb.append("<div class=\"packageName\">");
            sb.append(ref.getPackageName());
            sb.append("</div>");
            sb.append(ref.getClassName());
            sb.append("</td>");

            sb.append("<td>");
            sb.append(ref.getMethodName());
            sb.append("()");
            sb.append("</td>");

            sb.append("</tr>");
        }

        sb.append("</table>");

        out.write(sb.toString().getBytes());
    }

    private List<SpecReference> getCoverageForAssertion(String sectionId, String assertionId) {
        List<SpecReference> refs = new ArrayList<SpecReference>();

        if (references.containsKey(sectionId)) {
            for (SpecReference ref : references.get(sectionId)) {
                if (ref.getAssertion().equals(assertionId)) {
                    refs.add(ref);
                }
            }
        }

        return refs;
    }

    private void writeFooter(OutputStream out) throws IOException {
        out.write("</table>".getBytes());
        out.write("</body></html>".getBytes());
    }

    public void writeToFile(File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            generate(out);
            out.flush();
            out.close();
        }
        catch (IOException ex) {
            throw new RuntimeException("Error generating report file", ex);
        }
    }
}
