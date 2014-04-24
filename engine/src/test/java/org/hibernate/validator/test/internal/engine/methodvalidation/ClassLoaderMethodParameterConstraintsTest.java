/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import junit.framework.TestCase;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration test for {@link ValidatorImpl} which tests that the ClassLoader of the
 * base object and of classes in a parameter list do not affect validation.
 * These tests attempt to simulate a web application that has its own class loader that
 * does not preferentially delegate to a parent class loader.
 *
 * @author Chris Beckey <cbeckey@paypal.com>
 */
public class ClassLoaderMethodParameterConstraintsTest {
	// For these tests to be valid, do not reference the following classes, else the current ClassLoader will
	// load them.
	public static final String INTERFACE_NAME = 
			"org.hibernate.validator.test.internal.engine.methodvalidation.subject.Interface";
	public static final String CLASS_NAME = 
			"org.hibernate.validator.test.internal.engine.methodvalidation.subject.ConcreteClass";
	public static final String SUBCLASS_NAME = 
			"org.hibernate.validator.test.internal.engine.methodvalidation.subject.SubClass";
	public static final String SIMPLE_VO_CLASSNAME = 
			"org.hibernate.validator.test.internal.engine.methodvalidation.subject.ValueObject";
	public static final String SUB_VO_CLASSNAME = 
			"org.hibernate.validator.test.internal.engine.methodvalidation.subject.ValueObjectSubClass";
	
	/**
	 * This class tests that a class, with a method whose parameters are to be validated,
	 * that is loaded by a "foreign" class loader will validate correctly.
	 * In this context a foreign class loader is one that the Validation code does not have access
	 * to, something like a web application class loader.
	 * 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * 
	 */
	@Test
	public void testForeignClassLoaderValidation() 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(true);

		ClassLoader thisClassLoader = this.getClass().getClassLoader();
		ClassLoader childClassLoader = new NonDelegatingClassLoader(thisClassLoader);
		
		Class<?> interfaceClass = childClassLoader.loadClass(INTERFACE_NAME);
		Class<?> classClass = childClassLoader.loadClass(CLASS_NAME);
		Class<?> subclassClass = childClassLoader.loadClass(SUBCLASS_NAME);
		Class<?> simpleValueObjectClass = childClassLoader.loadClass(SIMPLE_VO_CLASSNAME);

		Assert.assertNotEquals(thisClassLoader, interfaceClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, classClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, subclassClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, simpleValueObjectClass.getClassLoader());
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Constructor<?> ctor = simpleValueObjectClass.getConstructor(String.class);
		Object simpleVO = ctor.newInstance("Hello World");
		
		Set<? extends ConstraintViolation<?>> violations = validator.forExecutables().validateParameters(
				classClass.newInstance(), classClass.getDeclaredMethods()[0], new Object[] {simpleVO}
		);
		TestCase.assertNotNull(violations);
		TestCase.assertEquals(0, violations.size());
	}
	
	/**
	 * This class tests that a class, with a method whose parameters are to be validated and is a derived class,
	 * that is loaded by a "foreign" class loader will validate correctly.
	 * In this context a foreign class loader is one that the Validation code does not have access
	 * to, something like a web application class loader.
	 * 
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@Test
	public void testSubClassWithForeignClassLoader() 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		HibernateValidatorConfiguration configure = Validation.byProvider( HibernateValidator.class ).configure();
		configure.getMethodValidationConfiguration().allowOverridingMethodAlterParameterConstraint(true);

		ClassLoader thisClassLoader = this.getClass().getClassLoader();
		ClassLoader childClassLoader = new NonDelegatingClassLoader(thisClassLoader);
		
		Class<?> interfaceClass = childClassLoader.loadClass(INTERFACE_NAME);
		Class<?> classClass = childClassLoader.loadClass(CLASS_NAME);
		Class<?> subclassClass = childClassLoader.loadClass(SUBCLASS_NAME);
		Class<?> simpleValueObjectClass = childClassLoader.loadClass(SIMPLE_VO_CLASSNAME);
		Class<?> subValueObjectClass = childClassLoader.loadClass(SUB_VO_CLASSNAME);

		Assert.assertNotEquals(thisClassLoader, interfaceClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, classClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, subclassClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, simpleValueObjectClass.getClassLoader());
		Assert.assertNotEquals(thisClassLoader, subValueObjectClass.getClassLoader());
		
		ValidatorFactory factory = configure.buildValidatorFactory();
		Validator validator = factory.getValidator();
		
		Constructor<?> ctor = subValueObjectClass.getConstructor(String.class, Integer.class);
		Object simpleVO = ctor.newInstance("Hello World", new Integer(42));

		Set<? extends ConstraintViolation<?>> violations = validator.forExecutables().validateParameters(
				subclassClass.newInstance(), 
				subclassClass.getDeclaredMethods()[0], 
				new Object[] {simpleVO}
		);
		TestCase.assertNotNull(violations);
		TestCase.assertEquals(0, violations.size());
	}
	
	/**
	 * A Class Loader that will not delegate to a parent class loader.
	 *
	 */
	class NonDelegatingClassLoader
	extends ClassLoader {

		public NonDelegatingClassLoader() {
			super(ClassLoader.getSystemClassLoader());
		}
		
		public NonDelegatingClassLoader(ClassLoader parent) {
			super(parent);
		}
		
	    private static final int BUFFER_SIZE = 8192;

		@Override
	    protected synchronized Class<?> loadClass(String className, boolean resolve) 
	    throws ClassNotFoundException {
			
	        // 1. is this class already loaded?
	        Class<?> cls = findLoadedClass(className);
	        if (cls != null) {
	            return cls;
	        }

	        // 2. get class file name from class name
	        String clsFile = className.replace('.', '/') + ".class";
	        
	        // 3. get bytes for class
	        byte[] classBytes = null;
	        try {
	            InputStream in = getResourceAsStream(clsFile);
	            byte[] buffer = new byte[BUFFER_SIZE];
	            ByteArrayOutputStream out = new ByteArrayOutputStream();
	            int n = -1;
	            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
	                out.write(buffer, 0, n);
	            }
	            classBytes = out.toByteArray();
	        }
	        catch (IOException e) {
	        	throw new ClassNotFoundException("Unable to read class file", e);
	        }

	        if (classBytes == null) {
	            throw new ClassNotFoundException("Cannot load class: " + className);
	        }

	        // 4. turn the byte array into a Class
	        try {
	            cls = defineClass(className, classBytes, 0, classBytes.length);
	            if (resolve) {
	                resolveClass(cls);
	            }
	        }
	        catch (SecurityException e) { 
	            // loading core java classes such as java.lang.String
	            // is prohibited, throws java.lang.SecurityException.
	            // delegate to parent if not allowed to load class
	            cls = super.loadClass(className, resolve);
	        }

	        return cls;
	    }
	};

}
