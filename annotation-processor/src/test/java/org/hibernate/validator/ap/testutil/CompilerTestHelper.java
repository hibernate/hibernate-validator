/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.testutil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.DiagnosticExpectation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Infrastructure for unit tests based on the Java Compiler API.
 *
 * @author Gunnar Morling
 */
public class CompilerTestHelper {

	private final JavaCompiler compiler;

	private final String sourceBaseDir;

	public CompilerTestHelper(JavaCompiler compiler) {

		this.compiler = compiler;

		String basePath;

		try {
			basePath = new File( "." ).getCanonicalPath();
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}

		this.sourceBaseDir = basePath + "/src/test/java";
	}

	/**
	 * Retrieves a file object containing the source of the given class.
	 *
	 * @param clazz The class of interest.
	 *
	 * @return A file with the source of the given class.
	 */
	public File getSourceFile(Class<?> clazz) {

		String sourceFileName =
				File.separator + clazz.getName().replace( ".", File.separator ) + ".java";

		return new File( sourceBaseDir + sourceFileName );
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, File... sourceFiles) {

		return compile( annotationProcessor, diagnostics, null, null, null, sourceFiles );
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, Kind diagnosticKind, File... sourceFiles) {

		return compile( annotationProcessor, diagnostics, diagnosticKind, null, null, sourceFiles );
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, boolean verbose, boolean allowMethodConstraints, File... sourceFiles) {

		return compile( annotationProcessor, diagnostics, null, verbose, allowMethodConstraints, sourceFiles );
	}

	/**
	 * Creates and executes a {@link CompilationTask} using the given input.
	 *
	 * @param annotationProcessor An annotation processor to be attached to the task.
	 * @param diagnostics An diagnostics listener to be attached to the task.
	 * @param diagnosticKind A value for the "diagnosticKind" option.
	 * @param verbose A value for the "verbose" option.
	 * @param sourceFiles The source files to be compiled.
	 *
	 * @return True, if the source files could be compiled successfully (meaning
	 *         in especially, that the given annotation processor didn't raise
	 *         any errors), false otherwise.
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, Kind diagnosticKind, Boolean verbose, Boolean allowMethodConstraints, File... sourceFiles) {

		StandardJavaFileManager fileManager =
				compiler.getStandardFileManager( null, null, null );

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects( sourceFiles );

		List<String> options = new ArrayList<String>();

		options.addAll( Arrays.asList( "-classpath", System.getProperty( "java.class.path" ), "-d", "target" ) );

		if ( diagnosticKind != null ) {
			options.add( String.format( "-A%s=%s", Configuration.DIAGNOSTIC_KIND_PROCESSOR_OPTION, diagnosticKind ) );
		}

		if ( verbose != null ) {
			options.add( String.format( "-A%s=%b", Configuration.VERBOSE_PROCESSOR_OPTION, verbose ) );
		}

		if ( allowMethodConstraints != null ) {
			options.add(
					String.format(
							"-A%s=%b",
							Configuration.METHOD_CONSTRAINTS_SUPPORTED_PROCESSOR_OPTION,
							allowMethodConstraints
					)
			);
		}

		CompilationTask task = compiler.getTask( null, fileManager, diagnostics, options, null, compilationUnits );
		task.setProcessors( Arrays.asList( annotationProcessor ) );

		return task.call();
	}

	/**
	 * <p>
	 * Asserts, that the given diagnostics match with the given expectations.
	 * </p>
	 * <p>
	 * First checks, whether the number of actual diagnostics matches with the
	 * number of given expectations. If that's the case, {@link Kind} and line
	 * number of each expectation are compared.
	 * </p>
	 *
	 * @param diagnostics The actual diagnostics as populated by the executed
	 * {@link CompilationTask}.
	 * @param expectations The expectations to compare against.
	 */
	public static void assertThatDiagnosticsMatch(DiagnosticCollector<JavaFileObject> diagnostics, DiagnosticExpectation... expectations) {

		List<Diagnostic<? extends JavaFileObject>> diagnosticsList = diagnostics.getDiagnostics();

		if ( expectations == null ) {
			assertTrue( diagnosticsList.isEmpty() );
		}
		else {

			if ( diagnosticsList.size() != expectations.length ) {

				for ( Diagnostic<? extends JavaFileObject> oneDiagnostic : diagnosticsList ) {
					System.out.println( oneDiagnostic );
				}
			}

			assertEquals( diagnosticsList.size(), expectations.length, "Wrong number of diagnostics." );

			int i = 0;
			for ( DiagnosticExpectation oneExpectation : expectations ) {

				assertEquals( diagnosticsList.get( i ).getKind(), oneExpectation.getKind() );
				assertEquals( diagnosticsList.get( i ).getLineNumber(), oneExpectation.getLineNumber() );

				i++;
			}
		}
	}
}
