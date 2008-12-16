// $Id:$
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
package org.hibernate.javadoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;

/**
 * This doclet writes a report for all junit tests marked documented with <i>jsr</i>.
 * Tests documented with this tag are referencing sections of the Bean Validation spec they
 * are testing/validating.
 *
 * @author Hardy Ferentschik
 */
public class JSRDoclet {

	public static final String TAG_NAME = "jsr";
	private static final String[] tableHeaders = new String[] { "Bean Validation Specification", "Class", "Method" };
	private static StringBuffer out = new StringBuffer();


	public static boolean start(RootDoc root) {
		String outDirName = readOptions( root.options() );
		File outDir = new File( outDirName );
		outDir.mkdirs();
		File specCheckReport = new File( outDir, "index.html" );

		List<JSRReference> references = processClasses( root.classes() );
		Collections.sort( references );

		writeHeader();
		writeContents( references );
		writeFooter();


		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter( specCheckReport ) );
			writer.write( out.toString() );
			writer.close();
		}
		catch ( IOException e ) {
			System.err.println( "Error writing tck report." );
		}
		return true;
	}

	public static int optionLength(String option) {
		if ( option.equals( "-d" ) ) {
			return 2;
		}
		else {
			return Standard.optionLength( option );
		}
	}

	public static boolean validOptions(String options[][], DocErrorReporter reporter) {
		return true;
	}

	private static void writeFooter() {
		out.append( "</body></html>" );
	}

	private static void writeHeader() {
		out.append( "<html><head></head><body>" );
	}

	private static void writeTableHeader() {
		out.append( "<table border=\"1\"><tr>" );
		for ( String s : tableHeaders ) {
			out.append( "<th>" ).append( s ).append( "</th>" );
		}
		out.append( "</tr>" );
	}

	private static void writeTableFooter() {
		out.append( "</table>" );
	}

	private static String readOptions(String[][] options) {
		String tagName = null;
		for ( String[] opt : options ) {
			if ( opt[0].equals( "-d" ) ) {
				tagName = opt[1];
			}
		}
		return tagName;
	}

	private static void writeContents(List<JSRReference> references) {
		writeTableHeader();
		for ( JSRReference reference : references ) {
			out.append( "<tr>" );
			out.append( "<td>" ).append( reference.jsrReference ).append( "</td>" );
			out.append( "<td><a href=\"" )
					.append( reference.getSourceLink() )
					.append( "\">" )
					.append( reference.className )
					.append( "</a></td>" );
			out.append( "<td>" ).append( reference.methodName ).append( "</td>" );
			out.append( "</tr>" );
		}
		writeTableFooter();
	}

	private static List<JSRReference> processClasses(ClassDoc[] classDocs) {
		ArrayList<JSRReference> references = new ArrayList<JSRReference>();
		for ( ClassDoc aClass : classDocs ) {
			MethodDoc[] methods = aClass.methods();
			for ( MethodDoc method : methods ) {
				Tag[] tags = method.tags( TAG_NAME );
				if ( tags.length > 0 ) {
					for ( Tag tag : tags ) {
						JSRReference reference = new JSRReference( tag.text(), aClass.qualifiedName(), method.name() );
						references.add( reference );
					}
				}
			}
		}
		return references;
	}

	static class JSRReference implements Comparable {
		/**
		 * The JSR 303 section this instance references.
		 */
		String jsrReference;

		/**
		 * The name of the class which references the JSR.
		 */
		String className;

		/**
		 * The method which references the JSR.
		 */
		String methodName;

		/**
		 * @todo Add some validation
		 */
		JSRReference(String reference, String className, String methodName) {
			this.jsrReference = reference;
			this.className = className;
			this.methodName = methodName;
		}

		public String getSourceLink() {
			StringBuilder builder = new StringBuilder();
			builder.append( "../xref-test/" );
			builder.append( className.replace( '.', '/' ) );
			builder.append( ".html" );
			return builder.toString();
		}

		public int compareTo(Object o) {
			return jsrReference.compareTo( ( ( JSRReference ) o ).jsrReference );
		}
	}
}
