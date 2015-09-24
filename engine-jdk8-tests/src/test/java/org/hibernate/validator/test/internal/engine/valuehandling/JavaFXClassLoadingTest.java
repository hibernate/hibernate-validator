/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.validation.ValidationException;

import org.fest.assertions.Assertions;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 * @author Davide D'Alto
 */
public class JavaFXClassLoadingTest {

	/**
	 * This class will be present in the TCCL beacuse is part of JDK 8
	 */
	private static final String JAVAFX_APPLICATION_CLASS = "javafx.application.Application";

	@Test
	public void shouldBeAbleToFindTheClassInTCCL() throws Exception {
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class ).addClass( JavaFXClassLoadingTest.class );
		ShrinkWrapClassLoader classLoaderWithoutExpectedClass = new ShrinkWrapClassLoader( (ClassLoader) null, archive );
		Assertions.assertThat( isClassPresent( JAVAFX_APPLICATION_CLASS, classLoaderWithoutExpectedClass, true ) ).isTrue();
	}

	@Test
	public void shouldNotFindTheClass() throws Exception {
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class ).addClass( JavaFXClassLoadingTest.class );
		ShrinkWrapClassLoader classLoaderWithoutExpectedClass = new ShrinkWrapClassLoader( (ClassLoader) null, archive );
		Assertions.assertThat( isClassPresent( JAVAFX_APPLICATION_CLASS, classLoaderWithoutExpectedClass, false ) ).isFalse();
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
