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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.DiagnosticExpectation;

import static org.testng.Assert.assertEquals;

/**
 * Infrastructure for unit tests based on the Java Compiler API.
 *
 * @author Gunnar Morling
 */
public class CompilerTestHelper {

	/**
	 * Dependencies which are used as class path elements for the compilation tasks.
	 *
	 * @author Gunnar Morling
	 */
	public enum Library {

		HIBERNATE_VALIDATOR( "hibernate-validator.jar" ),

		VALIDATION_API( "validation-api.jar" ),

		JODA_TIME( "joda-time.jar" );

		private final String name;

		private Library(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private final JavaCompiler compiler;

	private final String sourceBaseDir;

	private final String testLibraryDir;

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
		this.testLibraryDir = basePath + "/target/test-dependencies";
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
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, EnumSet, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, File... sourceFiles) {

		return compile(
				annotationProcessor,
				diagnostics,
				null,
				null,
				null,
				EnumSet.allOf( Library.class ),
				sourceFiles
		);
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, EnumSet, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, Kind diagnosticKind, File... sourceFiles) {

		return compile(
				annotationProcessor,
				diagnostics,
				diagnosticKind,
				null,
				null,
				EnumSet.allOf( Library.class ),
				sourceFiles
		);
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, EnumSet, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, boolean verbose, boolean allowMethodConstraints, File... sourceFiles) {

		return compile(
				annotationProcessor,
				diagnostics,
				null,
				verbose,
				allowMethodConstraints,
				EnumSet.allOf( Library.class ),
				sourceFiles
		);
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, EnumSet, File...)
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, EnumSet<Library> dependencies, File... sourceFiles) {

		return compile( annotationProcessor, diagnostics, null, null, null, dependencies, sourceFiles );
	}


	/**
	 * Creates and executes a {@link CompilationTask} using the given input.
	 *
	 * @param annotationProcessor An annotation processor to be attached to the task.
	 * @param diagnostics An diagnostics listener to be attached to the task.
	 * @param diagnosticKind A value for the "diagnosticKind" option.
	 * @param verbose A value for the "verbose" option.
	 * @param allowMethodConstraints A value for the "methodConstraintsSupported" option.
	 * @param dependencies A set with libraries which shall be added to the class path of
	 * the compilation task.
	 * @param sourceFiles The source files to be compiled.
	 *
	 * @return True, if the source files could be compiled successfully (meaning
	 *         in especially, that the given annotation processor didn't raise
	 *         any errors), false otherwise.
	 */
	public boolean compile(
			Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, Kind diagnosticKind, Boolean verbose, Boolean allowMethodConstraints, EnumSet<Library> dependencies, File... sourceFiles) {

		StandardJavaFileManager fileManager =
				compiler.getStandardFileManager( null, null, null );

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects( sourceFiles );

		List<String> options = new ArrayList<String>();

		options.addAll( Arrays.asList( "-d", "target" ) );

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

		try {
			fileManager.setLocation( StandardLocation.CLASS_PATH, getDependenciesAsFiles( dependencies ) );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
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

		assertEquals( asExpectations( diagnostics.getDiagnostics() ), CollectionHelper.asSet( expectations ) );
	}

	private static Set<DiagnosticExpectation> asExpectations(Collection<Diagnostic<? extends JavaFileObject>> diagnosticsList) {

		Set<DiagnosticExpectation> theValue = CollectionHelper.newHashSet();

		for ( Diagnostic<? extends JavaFileObject> diagnostic : diagnosticsList ) {
			theValue.add( new DiagnosticExpectation( diagnostic.getKind(), diagnostic.getLineNumber() ) );
		}

		return theValue;
	}

	private Set<File> getDependenciesAsFiles(EnumSet<Library> dependencies) {

		Set<File> files = new HashSet<File>();

		for ( Library oneDependency : dependencies ) {
			files.add( new File( testLibraryDir + File.separator + oneDependency.getName() ) );
		}

		return files;
	}
}
