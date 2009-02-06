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
import java.util.Collections;

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
 * An APT annotation processor for creating a TCK coverage report.
 *
 * @author Hardy Ferentschik
 */
public class TCKAnnotationProcessor implements AnnotationProcessor {

	private static final String OUTDIR_OPTION_NAME = "-s";
	private static final String REPORT_FILE_NAME = "tck.html";

	private final AnnotationProcessorEnvironment env;
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
							new CreateReferenceVisitor(),
							NO_OP
					)
			);
		}

		Collections.sort( references );
		TCKReportGenerator generator = new HtmlTckReportGenerator();
		String report = generator.generateReport( references );
		writeReportFile( report );
	}

	private void writeReportFile(String report) {
		try {
			File reportFile = new File( baseDir, REPORT_FILE_NAME );
			BufferedWriter writer = new BufferedWriter( new FileWriter( reportFile ) );
			writer.write( report );
			writer.close();
		}
		catch ( IOException e ) {
			System.err.println( "Error writing report." );
		}
	}

	private class CreateReferenceVisitor extends SimpleDeclarationVisitor {
		public void visitMethodDeclaration(MethodDeclaration d) {
			SpecAssertion annotation = d.getAnnotation( SpecAssertion.class );
			JSRReference ref = new JSRReference(
					annotation.section(), d.getDeclaringType().getQualifiedName(), d.getSimpleName()
			);
			if ( annotation.note().length() > 0 ) {
				ref.note = annotation.note();
			}
			references.add( ref );
		}
	}
}
