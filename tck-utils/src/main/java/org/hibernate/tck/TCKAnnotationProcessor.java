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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import static com.sun.mirror.util.DeclarationVisitors.NO_OP;
import static com.sun.mirror.util.DeclarationVisitors.getDeclarationScanner;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import org.hibernate.tck.annotations.SpecAssertion;

/**
 * @author Hardy Ferentschik
 */
public class TCKAnnotationProcessor implements AnnotationProcessor {

	private static final String OUTDIR_OPTION_NAME = "-s";
	private static final String REPORT_FILE_NAME = "tck.html";

	private final AnnotationProcessorEnvironment env;
	private final String[] tableHeaders = new String[] { "Section", "Class", "Method" };
	private final StringBuffer out = new StringBuffer();
	private final List<JSRReference> references = new ArrayList<JSRReference>();
	private final File baseDir;

	public TCKAnnotationProcessor(AnnotationProcessorEnvironment annotationProcessorEnvironment) {
		this.env = annotationProcessorEnvironment;
		String baseDirName = env.getOptions().get( OUTDIR_OPTION_NAME );
		baseDir = new File( baseDirName );
		baseDir.mkdirs();
	}

	public void process() {


		AnnotationTypeDeclaration annType = ( AnnotationTypeDeclaration ) env.getTypeDeclaration(
				SpecAssertion.class.getCanonicalName()
		);
		for ( Declaration d : env.getDeclarationsAnnotatedWith( annType ) ) {
			d.accept(
					getDeclarationScanner(
							new DoNothingVisitor(),
							NO_OP
					)
			);
		}


		writeHeader();
		writeContents();
		writeFooter();

		writeReporttoFile();
	}

	private void writeReporttoFile() {
		try {
			File report = new File( baseDir, REPORT_FILE_NAME );
			BufferedWriter writer = new BufferedWriter( new FileWriter( report ) );
			writer.write( out.toString() );
			writer.close();
		}
		catch ( IOException e ) {
			System.err.println( "Error writing report." );
		}
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

	private void writeContents() {
		writeTableHeader();
		for ( JSRReference reference : references ) {
			out.append( "<tr>" );
			out.append( "<td>" ).append( reference.jsrSectionReference ).append( "</td>" );
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

	private class DoNothingVisitor extends SimpleDeclarationVisitor {
		public void visitMethodDeclaration(MethodDeclaration d) {
			SpecAssertion annotation = d.getAnnotation( SpecAssertion.class );
			JSRReference ref = new JSRReference(
					annotation.section()[0], d.getDeclaringType().getQualifiedName(), d.getSimpleName()
			);
			references.add( ref );
		}
	}

	private static class JSRReference implements Comparable {
		/**
		 * The JSR  section this instance references.
		 */
		String jsrSectionReference;

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
			this.jsrSectionReference = reference;
			this.className = className;
			this.methodName = methodName;
		}

		public String getSourceLink() {
			StringBuilder builder = new StringBuilder();
			builder.append( "xref-test/" );
			builder.append( className.replace( '.', '/' ) );
			builder.append( ".html" );
			return builder.toString();
		}

		public int compareTo(Object o) {
			return jsrSectionReference.compareTo( ( ( JSRReference ) o ).jsrSectionReference );
		}
	}
}
