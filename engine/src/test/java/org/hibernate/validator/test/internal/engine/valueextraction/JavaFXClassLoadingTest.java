/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.AccessController;
import java.security.PrivilegedAction;

import jakarta.validation.ValidationException;

import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.testutil.TestForIssue;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * @author Davide D'Alto
 */
@TestForIssue(jiraKey = "HV-1017")
public class JavaFXClassLoadingTest {

	/**
	 * This class will be present in the TCCL because it is either part of the JDK (JDK 10-) or in the classpath (JDK 11+).
	 */
	private static final String JAVAFX_APPLICATION_CLASS = "javafx.beans.value.ObservableValue";

	@Test
	public void shouldBeAbleToFindTheClassInTCCL() throws Exception {
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class ).addClass( JavaFXClassLoadingTest.class );
		ShrinkWrapClassLoader classLoaderWithoutExpectedClass = new ShrinkWrapClassLoader( (ClassLoader) null, archive );
		assertThat( isClassPresent( JAVAFX_APPLICATION_CLASS, classLoaderWithoutExpectedClass, true ) ).isTrue();
	}

	@Test
	public void shouldNotFindTheClass() throws Exception {
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class ).addClass( JavaFXClassLoadingTest.class );
		ShrinkWrapClassLoader classLoaderWithoutExpectedClass = new ShrinkWrapClassLoader( (ClassLoader) null, archive );
		assertThat( isClassPresent( JAVAFX_APPLICATION_CLASS, classLoaderWithoutExpectedClass, false ) ).isFalse();
	}

	private static boolean isClassPresent(String className, ClassLoader classLoader, boolean fallbackOnTCCL) {
		try {
			run( LoadClass.action( className, classLoader, fallbackOnTCCL ) );
			return true;
		}
		catch (ValidationException e) {
			return false;
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
