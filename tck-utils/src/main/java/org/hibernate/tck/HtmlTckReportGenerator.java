// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.tck;

import java.util.List;

/**
 * @author Hardy Ferentschik
 */
public class HtmlTckReportGenerator implements TCKReportGenerator {
	private final String[] tableHeaders = new String[] { "Section", "Class", "Method", "Note" };
	private StringBuffer out;

	public String generateReport(List<JSRReference> references) {
		out = new StringBuffer();
		writeHeader();
		writeContents( references );
		writeFooter();
		return out.toString();
	}

	private void writeFooter() {
		out.append( "</body></html>" );
	}

	private void writeHeader() {
		out.append( "<html><head></head><body>" );
	}

	private void writeTableHeader() {
		out.append( "<table border=\"1\"><tr>" );
		for ( String s : tableHeaders ) {
			out.append( "<th>" ).append( s ).append( "</th>" );
		}
		out.append( "</tr>" );
	}

	private void writeTableFooter() {
		out.append( "</table>" );
	}

	private void writeContents(List<JSRReference> references) {
		writeTableHeader();
		String currentReference = "";
		boolean sameReference;
		String currentClass = "";
		for ( JSRReference reference : references ) {
			out.append( "<tr>" );

			if ( currentReference.equals( reference.jsrSectionReference ) ) {
				sameReference = true;
				out.append( "<td></td>" );
			}
			else {
				currentReference = reference.jsrSectionReference;
				sameReference = false;
				out.append( "<td>" ).append( reference.jsrSectionReference ).append( "</td>" );
			}

			if ( sameReference && currentClass.equals( reference.className ) ) {
				out.append( "<td></td>" );
			}
			else {
				currentClass = reference.className;
				out.append( "<td><a href=\"" )
						.append( reference.getSourceLink() )
						.append( "\">" )
						.append( reference.className )
						.append( "</a></td>" );
			}

			out.append( "<td>" ).append( reference.methodName ).append( "</td>" );

			out.append( "<td>" ).append( reference.note ).append( "</td>" );
			out.append( "</tr>" );
		}
		writeTableFooter();
	}
}
