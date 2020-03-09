/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import jakarta.el.ExpressionFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ValidatorFactoryNoELBootstrapTest {

	private final String EL_PACKAGE_PREFIX = "jakarta.el";

	private final String EL_IMPL_PACKAGE_PREFIX = "com.sun.el";

	@Test
	@TestForIssue(jiraKey = "HV-793")
	public void bootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory() throws Throwable {
		runWithoutElLibs( BootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory.class, EL_PACKAGE_PREFIX );
	}

	public static class BootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory {

		public void run() {
			try {
				Validation.buildDefaultValidatorFactory();
				fail( "An exception should have been thrown" );
			}
			catch (Throwable e) {
				assertThat( e ).isInstanceOf( ValidationException.class );
				assertTrue(
						e.getMessage().startsWith( "HV000183" ),
						"Bootstrapping in Validation should throw an unexpected exception: " + e.getMessage()
				);
			}
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1131")
	public void canUseParameterInterpolatorWithoutExpressionFactory() throws Throwable {
		runWithoutElLibs( CanUseParameterInterpolatorWithoutExpressionFactory.class, EL_PACKAGE_PREFIX );
	}

	public static class CanUseParameterInterpolatorWithoutExpressionFactory {

		public void run() {
			Validator validator = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator( new ParameterMessageInterpolator() )
				.buildValidatorFactory()
				.getValidator();

			assertNotNull( validator );

			Set<ConstraintViolation<SomeBean>> violations = validator.validate( new SomeBean() );
			ConstraintViolationAssert.assertThat( violations ).containsOnlyViolations(
					violationOf( Min.class ).withMessage( "must be greater than or equal to 42" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1153")
	public void missingImplementationThrowsValidationException() throws Throwable {
		runWithoutElLibs( MissingImplementationThrowsValidationException.class, EL_IMPL_PACKAGE_PREFIX );
	}

	public static class MissingImplementationThrowsValidationException {

		public void run() {
			try {
				Validation.buildDefaultValidatorFactory();
				fail( "An exception should have been thrown" );
			}
			catch (Throwable e) {
				assertThat( e ).isInstanceOf( ValidationException.class );
				assertTrue(
						e.getMessage().startsWith( "HV000183" ),
						"Bootstrapping in Validation should throw an unexpected exception: " + e.getMessage()
				);
			}
		}
	}

	public static class SomeBean {

		@Min(42)
		private final long myLong = 41;
	}

	/**
	 * A lot of classpath magic is happening here. The test suite has the EL dependencies on the classpath.
	 * To test what happens if the EL classes cannot be found, we need to somehow "remove" these classes from the
	 * classpath.
	 */
	private static class ELIgnoringClassLoader extends ClassLoader {
		private final String[] PASS_THROUGH_PACKAGE_PREFIXES = new String[] {
				"java.",
				"javax.xml.",
				"sun.",
				"org.apache.",
				"org.xml",
				"jdk.internal"
		};

		private final String packageMissing;

		public ELIgnoringClassLoader( String packageMissing ) {
			super( ELIgnoringClassLoader.class.getClassLoader() );
			this.packageMissing = packageMissing;
		}

		@Override
		@IgnoreForbiddenApisErrors(reason = "getPackage() is deprecated but getDefinedPackage() is only available from JDK 9.")
		public Class<?> loadClass(String className) throws ClassNotFoundException {
			// This is what we in the end want to achieve. Throw ClassNotFoundException for jakarta.el classes
			if ( className.startsWith( packageMissing ) ) {
				throw new ClassNotFoundException();
			}

			// for all other classes we have to jump through some hoops

			// load the class via the parent class loader
			// if we have a class from the java name space we need to return it, since the security manager
			// won't allow to redefine it
			for ( String prefix : PASS_THROUGH_PACKAGE_PREFIXES ) {
				if ( className.startsWith( prefix ) ) {
					return super.loadClass( className );
				}
			}

			// if the class is already loaded, we return it
			Class<?> clazz = findLoadedClass( className );
			if ( clazz != null ) {
				return clazz;
			}

			// for all other classes we load the class data from disk and define the class new
			// this will ensure that ELIgnoringClassLoader will be the associated classloader for this class
			byte[] classData;
			try {
				classData = loadClassData( className );
			}
			catch (IOException e) {
				throw new RuntimeException();
			}
			clazz = defineClass( className, classData, 0, classData.length, null );

			// it is our responsability to define the package of the class
			String packageName = getPackageName( className );
			if ( packageName != null && getPackage( packageName ) == null ) {
				definePackage( packageName, null, null, null, null, null, null, null );
			}

			return clazz;
		}

		private String getPackageName(String className) {
			int i = className.lastIndexOf( '.' );
			if ( i > 0 ) {
				return className.substring( 0, i );
			}
			else {
				return null;
			}
		}

		private byte[] loadClassData(String className) throws IOException, ClassNotFoundException {
			String path = "/" + className.replace( ".", "/" ) + ".class";
			InputStream in = ValidatorFactoryNoELBootstrapTest.class.getResourceAsStream( path );

			if ( in == null ) {
				throw new ClassNotFoundException();
			}

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int bytesRead;
			byte[] data = new byte[16384];
			while ( ( bytesRead = in.read( data, 0, data.length ) ) != -1 ) {
				buffer.write( data, 0, bytesRead );
			}

			buffer.flush();
			return buffer.toByteArray();
		}
	}

	/**
	 * Loads the given class using the EL-ignoring class loader and executes it.
	 *
	 * We need to override the Thread context class loader temporarily as {@link jakarta.el.FactoryFinder} is directly using it to load the
	 * {@link ExpressionFactory} in {@link ExpressionFactory#newInstance(java.util.Properties)}.
	 */
	private void runWithoutElLibs(Class<?> delegateType, String packageMissing) throws Throwable {
		try {
			ClassLoader originClassLoader = run( GetClassLoader.fromContext() );
			try {
				ClassLoader classLoader = new ELIgnoringClassLoader( packageMissing );
				run( SetContextClassLoader.action( classLoader ) );

				Object test = classLoader.loadClass( delegateType.getName() ).getConstructor().newInstance();
				test.getClass().getMethod( "run" ).invoke( test );
			}
			finally {
				run( SetContextClassLoader.action( originClassLoader ) );
			}
		}
		catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
