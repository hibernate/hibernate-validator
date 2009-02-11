package org.hibernate.tck.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the TCK spec coverage report
 *
 * @author Shane Bryzak
 */
public class CoverageReport {
    /*
    * References to the spec assertions made by the tck tests
    */
    private final Map<String, List<SpecReference>> references;

    private AuditParser auditParser;

    public CoverageReport(List<SpecReference> references, AuditParser auditParser) {
        this.references = new HashMap<String, List<SpecReference>>();

        for (SpecReference ref : references) {
            if (!this.references.containsKey(ref.getSection())) {
                this.references.put(ref.getSection(), new ArrayList<SpecReference>());
            }

            this.references.get(ref.getSection()).add(ref);
        }

        this.auditParser = auditParser;
    }

    public void generate(OutputStream out) throws IOException {
        writeHeader(out);
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
        sb.append("  .code {\n");
        sb.append("    float: left;\n");
        sb.append("    font-weight: bold;\n");
        sb.append("    width: 50px;\n");
        sb.append("    margin-top: 0px;\n");
        sb.append("    height: 100%; }\n");
        sb.append("  .results {\n");
        sb.append("    margin-left: 50px; }\n");
        sb.append("  .description {\n");
        sb.append("    margin-top: 2px;\n");
        sb.append("    margin-bottom: 2px; }\n");
        sb.append("  .sectionHeader {\n");
        sb.append("    font-weight: bold; }\n");
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

    private void writeCoverage(OutputStream out) throws IOException {
        for (String sectionId : auditParser.getSectionIds()) {
            out.write(("<div class=\"sectionHeader\">Section " + sectionId + " - " +
                    auditParser.getSectionTitle(sectionId) + "</div>\n").getBytes());

            List<AuditAssertion> sectionAssertions = auditParser.getAssertionsForSection(sectionId);

            if (sectionAssertions != null && !sectionAssertions.isEmpty()) {
                StringBuilder sb = new StringBuilder();

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

                    if (coverage.isEmpty()) {
                        sb.append("        <p class=\"noCoverage\">No tests exist for this assertion</p>\n");
                    } else {
                        for (SpecReference ref : coverage) {
                            sb.append("        <p>");
                            sb.append(ref.getClassName());
                            sb.append(".");
                            sb.append(ref.getMethodName());
                            sb.append("()");
                            sb.append("</p>\n");
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
