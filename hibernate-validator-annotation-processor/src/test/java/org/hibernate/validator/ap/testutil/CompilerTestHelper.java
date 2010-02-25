// $Id$
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

import org.hibernate.validator.ap.util.DiagnosticExpection;

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

	/**
	 * TODO GM: How can JavaCompiler access all dependencies of the project? This works within
	 * Eclipse, but not on the command line.
	 */
	private final String pathToBeanValidationApiJar;

	public CompilerTestHelper(JavaCompiler compiler, String sourceBaseDir, String pathToBeanValidationApiJar) {

		this.compiler = compiler;
		this.sourceBaseDir = sourceBaseDir;
		this.pathToBeanValidationApiJar = pathToBeanValidationApiJar;
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
	 * Creates and executes a {@link CompilationTask} using the given input.
	 *
	 * @param annotationProcessor An annotation processor to be attached to the task.
	 * @param diagnostics An diagnostics listener to be attached to the task.
	 * @param sourceFiles The source files to be compiled.
	 *
	 * @return True, if the source files could be compiled successfully (meaning
	 *         in especially, that the given annotation processor didn't raise
	 *         any errors), false otherwise.
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, File... sourceFiles) {

		return compile( annotationProcessor, diagnostics, Kind.ERROR, sourceFiles );
	}

	/**
	 * Creates and executes a {@link CompilationTask} using the given input.
	 *
	 * @param annotationProcessor An annotation processor to be attached to the task.
	 * @param diagnostics An diagnostics listener to be attached to the task.
	 * @param diagnosticKind A value for the "diagnosticKind" option.
	 * @param sourceFiles The source files to be compiled.
	 *
	 * @return True, if the source files could be compiled successfully (meaning
	 *         in especially, that the given annotation processor didn't raise
	 *         any errors), false otherwise.
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, Kind diagnosticKind, File... sourceFiles) {

		StandardJavaFileManager fileManager =
				compiler.getStandardFileManager( null, null, null );

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects( sourceFiles );

		List<String> optionList = new ArrayList<String>();
		optionList.addAll(
				Arrays.asList(
						"-classpath", pathToBeanValidationApiJar, "-AdiagnosticKind=" + diagnosticKind, "-d", "target"
				)
		);

		CompilationTask task = compiler.getTask( null, fileManager, diagnostics, optionList, null, compilationUnits );
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
	 * @param expections The expectations to compare against.
	 */
	public static void assertThatDiagnosticsMatch(DiagnosticCollector<JavaFileObject> diagnostics, DiagnosticExpection... expections) {

		List<Diagnostic<? extends JavaFileObject>> diagnosticsList = diagnostics.getDiagnostics();

		if ( expections == null ) {
			assertTrue( diagnosticsList.isEmpty() );
		}
		else {

			if ( diagnosticsList.size() != expections.length ) {
				System.out.println( diagnosticsList );
			}

			assertEquals( diagnosticsList.size(), expections.length, "Wrong number of diagnostics." );

			int i = 0;
			for ( DiagnosticExpection oneExpection : expections ) {

				assertEquals( diagnosticsList.get( i ).getKind(), oneExpection.getKind() );
				assertEquals( diagnosticsList.get( i ).getLineNumber(), oneExpection.getLineNumber() );

				i++;
			}
		}
	}
}