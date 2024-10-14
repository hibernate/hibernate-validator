/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ValidationException;

import org.hibernate.validator.internal.util.actions.LoadClass;
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
			LoadClass.action( className, classLoader, fallbackOnTCCL );
			return true;
		}
		catch (ValidationException e) {
			return false;
		}
	}
}
