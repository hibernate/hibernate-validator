/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ValidatorFactoryNoELBootstrapTest {

	@Test
	@TestForIssue(jiraKey = "HV-793")
	public void bootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory() throws Throwable {
		runWithoutElLibs( BootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory.class );
	}

	public static class BootstrapFailsWhenUsingDefaultInterpolatorWithoutExpressionFactory {

		public void run() {
			try {
				Validation.buildDefaultValidatorFactory();
				fail( "An exception should have been thrown" );
			}
			catch (Throwable e) {
				assertTrue(
						getRootCause( e ).getMessage().startsWith( "HV000183" ),
						"Bootstrapping in Validation should throw an unexpected exception: " + e.getMessage()
				);
			}
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1131")
	public void canUseParameterInterpolatorWithoutExpressionFactory() throws Throwable {
		runWithoutElLibs( CanUseParameterInterpolatorWithoutExpressionFactory.class );
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
			assertCorrectConstraintViolationMessages( violations, "must be greater than or equal to 42" );
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
		private final String EL_PACKAGE_PREFIX = "javax.el";
		private final String[] PASS_THROUGH_PACKAGE_PREFIXES = new String[] {
				"java.",
				"javax.xml.",
				"sun.",
				"org.apache.",
				"org.xml",
				"jdk.internal"
		};

		public ELIgnoringClassLoader() {
			super( ELIgnoringClassLoader.class.getClassLoader() );
		}

		@Override
		public Class<?> loadClass(String className) throws ClassNotFoundException {
			// This is what we in the end want to achieve. Throw ClassNotFoundException for javax.el classes
			if ( className.startsWith( EL_PACKAGE_PREFIX ) ) {
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
	 */
	private void runWithoutElLibs(Class<?> delegateType) throws Throwable {
		try {
			Object test = new ELIgnoringClassLoader().loadClass( delegateType.getName() ).newInstance();
			test.getClass().getMethod( "run" ).invoke( test );
		}
		catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
	}

	private static Throwable getRootCause(Throwable throwable) {
		while ( true ) {
			Throwable cause = throwable.getCause();
			if ( cause == null ) {
				return throwable;
			}
			throwable = cause;
		}
	}
}
