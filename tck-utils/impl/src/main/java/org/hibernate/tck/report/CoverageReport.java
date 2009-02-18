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
        }
        catch (Exception ex)
        {
           // swallow
        }        
    }

    public void generate(OutputStream out) throws IOException {
        writeHeader(out);
        writeMasterSummary(out);
        writeSectionSummary(out);
        writeCoverage(out);
        writeUnmatched(out);
        writeFooter(out);
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
        sb.append("    margin-top: 2px;\n");
        sb.append("    margin-bottom: 2px;\n");
        sb.append("    font-weight: bold;\n");
        sb.append("    font-style: italic;\n");
        sb.append("    color: #ff0000; }\n");
        sb.append("   .coverageHeader {\n");
        sb.append("    font-weight: bold;\n");
        sb.append("    text-decoration: underline;\n");
        sb.append("    margin-top: 2px;\n");
        sb.append("    margin-bottom: 2px; }\n");
        sb.append("   .coverageMethod {\n");
        sb.append("    font-style: italic; }\n");
        sb.append("  .pass {\n");
        sb.append("    background-color: #dfd; }\n");
        sb.append("  .fail {\n");
        sb.append("    background-color: #fdd; }\n");

        sb.append("</style>\n");

        sb.append("</head><body>");
        sb.append("<h1>JSR-299 TCK Coverage</h1>");
        sb.append("<h2>");
        sb.append(auditParser.getVersion());
        sb.append("</h2>\n");

        out.write(sb.toString().getBytes());
    }
    
    private void writeMasterSummary(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3>Master Summary</h3>\n");
       
       sb.append("<table border=\"0\">");
       
       sb.append("<tr>");
       sb.append("<td>Total number of assertions</td>");
       sb.append("<td>");
       
       int assertionTotal = 0;
       
       for (List<AuditAssertion> assertions : auditParser.getAssertions().values())
       {
          assertionTotal += assertions.size();
       }
       
       sb.append(assertionTotal);
       sb.append("</td>");
       sb.append("</tr>");
       
       sb.append("<tr>");
       sb.append("<td>Total number of tested assertions</td>");
       
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
       
       sb.append("<td>");
       sb.append(coverage);
       sb.append("</td>");
              
       sb.append("</tr>");

       sb.append("<tr>");
       sb.append("<td>Total percentage of tested assertions</td>");
       
       double coveragePercent = assertionTotal > 0 ? ((coverage * 1.0) / assertionTotal) * 100 : 0;
       sb.append("<td>");
       sb.append(String.format("%.2f%%", coveragePercent));
       sb.append("</td>");
              
       sb.append("</tr>");       
       
       sb.append("</table>");
       
       out.write(sb.toString().getBytes());
    }

    private void writeSectionSummary(OutputStream out) throws IOException {
       StringBuilder sb = new StringBuilder();
       
       sb.append("<h3>Section Summary</h3>\n");

       sb.append("<table width=\"100%\">");
       
       sb.append("<tr style=\"background-color:#dddddd\">");
       sb.append("<th align=\"left\">Section</th>");
       sb.append("<th>Assertions</th>");
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
         int coverage = 0;
         
         for (AuditAssertion assertion : auditParser.getAssertionsForSection(sectionId))
         {
            if (!getCoverageForAssertion(sectionId, assertion.getId()).isEmpty())
            {
               coverage++;
            }
         }
         
         double coveragePercent = assertions > 0 ? ((coverage * 1.0) / assertions) * 100 : 0;
         
         sb.append("<td align=\"center\">");
         sb.append(assertions);
         sb.append("</td>");
         
         sb.append("<td align=\"center\">");
         sb.append(coverage);
         sb.append("</td>");
         
         String bgColor = coveragePercent < 60 ? "#ffaaaa" : coveragePercent < 80 ? "#ffffaa" : "#aaffaa";
         
         sb.append("<td align=\"center\" style=\"background-color:" + bgColor + "\">");
         sb.append(String.format("%.2f%%", coveragePercent));
         sb.append("</td>");
         
         sb.append("</tr>");
       }

       sb.append("</table>");       
       out.write(sb.toString().getBytes());       
    }
    
    private void writeCoverage(OutputStream out) throws IOException {
       
        out.write("<h3>Coverage Detail</h3>\n".getBytes());
       
        for (String sectionId : auditParser.getSectionIds()) {

            List<AuditAssertion> sectionAssertions = auditParser.getAssertionsForSection(sectionId);

            if (sectionAssertions != null && !sectionAssertions.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                out.write(("<h4 class=\"sectionHeader\" id=\"" + sectionId + "\">Section " + 
                      sectionId + " - " +
                      auditParser.getSectionTitle(sectionId) + "</h4>\n").getBytes());                
                
                for (AuditAssertion assertion : sectionAssertions) {
                    List<SpecReference> coverage = getCoverageForAssertion(sectionId, assertion.getId());

                    sb.append("  <div class=\"" + (coverage.isEmpty() ? "fail" : "pass") + "\">\n");

                    sb.append("    <span class=\"code\">");
                    sb.append(assertion.getId());
                    sb.append(")");
                    sb.append("</span>\n");

                    sb.append("    <div class=\"results\">");
                    sb.append("<p class=\"description\">");
                    sb.append(assertion.getText());
                    sb.append("</p>\n");

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

                    sb.append("    </div>\n  </div>\n</div>");
                }

                out.write(sb.toString().getBytes());
            }
        }
    }

    private void writeUnmatched(OutputStream out) throws IOException {
        List<SpecReference> unmatched = new ArrayList<SpecReference>();

        for (String sectionId : references.keySet()) {
            for (SpecReference ref : references.get(sectionId)) {
                if (!auditParser.hasAssertion(ref.getSection(), ref.getAssertion())) {
                    unmatched.add(ref);
                }
            }
        }

        if (unmatched.isEmpty()) return;

        StringBuilder sb = new StringBuilder();

        sb.append("<h3>Unmatched tests</h3>\n");
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
