/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
import static org.testng.Assert.fail;

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

	private static final File BASE_DIR;
	private static final File TARGET_DIR;
	private static final File PROCESSOR_OUT_DIR;

	static {
		TARGET_DIR = getTargetDir();
		BASE_DIR = TARGET_DIR.getParentFile();
		PROCESSOR_OUT_DIR = new File( TARGET_DIR, "processor-generated-test-classes" );
		if ( !PROCESSOR_OUT_DIR.exists() ) {
			if ( !PROCESSOR_OUT_DIR.mkdirs() ) {
				fail( "Unable to create test output directory " + PROCESSOR_OUT_DIR.toString() );
			}
		}
	}

	public CompilerTestHelper(JavaCompiler compiler) {
		this.compiler = compiler;
		this.sourceBaseDir = BASE_DIR.getAbsolutePath() + "/src/test/java";
		this.testLibraryDir = TARGET_DIR.getAbsolutePath() + "/test-dependencies";
	}

	/**
	 * Retrieves a file object containing the source of the given class.
	 *
	 * @param clazz The class of interest.
	 *
	 * @return A file with the source of the given class.
	 */
	public File getSourceFile(Class<?> clazz) {
		String sourceFileName = File.separator + clazz.getName().replace( ".", File.separator ) + ".java";
		return new File( sourceBaseDir + sourceFileName );
	}

	/**
	 * @see CompilerTestHelper#compile(Processor, DiagnosticCollector, Kind, Boolean, Boolean, EnumSet, File...)
	 */
	public boolean compile(Processor annotationProcessor,
						   DiagnosticCollector<JavaFileObject> diagnostics,
						   File... sourceFiles) {
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
	public boolean compile(Processor annotationProcessor,
						   DiagnosticCollector<JavaFileObject> diagnostics,
						   boolean verbose,
						   boolean allowMethodConstraints,
						   File... sourceFiles) {
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
	public boolean compile(Processor annotationProcessor,
						   DiagnosticCollector<JavaFileObject> diagnostics,
						   EnumSet<Library> dependencies,
						   File... sourceFiles) {
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
	public boolean compile(Processor annotationProcessor,
						   DiagnosticCollector<JavaFileObject> diagnostics,
						   Kind diagnosticKind,
						   Boolean verbose,
						   Boolean allowMethodConstraints,
						   EnumSet<Library> dependencies,
						   File... sourceFiles) {
		StandardJavaFileManager fileManager = compiler.getStandardFileManager( null, null, null );
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects( sourceFiles );
		List<String> options = new ArrayList<String>();

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
			fileManager.setLocation( StandardLocation.CLASS_OUTPUT, Arrays.asList( PROCESSOR_OUT_DIR ) );
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

	/**
	 * Returns the target directory of the build.
	 *
	 * @return the target directory of the build
	 */
	public static File getTargetDir() {
		// target/test-classes
		String targetClassesDir = CompilerTestHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		return new File ( targetClassesDir ).getParentFile();
	}
}
