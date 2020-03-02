/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import java.lang.reflect.Field;

import jakarta.validation.Configuration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.bootstrap.GenericBootstrap;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Chris Beckey
 */
public class ConfigurationFilePropertiesTest {

	/**
	 * The following test assumes that the file META-INF/validation.xml is present and
	 * contains:
	 * <pre>{@code
	 * <property name="hibernate.validator.allow_parameter_constraint_override">true</property>
	 * <property name="hibernate.validator.allow_multiple_cascaded_validation_on_return_values">true</property>
	 * <property name="hibernate.validator.allow_parallel_method_parameter_constraint">true</property>
	 * <property name="hibernate.validator.fail_fast">true</property>
	 * }</pre>
	 *
	 * The Maven build runs this test in a separate execution of surefire, which adds the
	 * path to the required file onto its classpath.
	 */
	@Test
	public void testAllowMultipleCascadedValidationOnReturnValues() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;

				// Note that the configuration from the XML is not read until the
				// buildValidatorFactory() method is called.
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowMultipleCascadedValidationOnReturnValues() );
			}
		} );
	}

	@Test
	public void testAllowOverridingMethodAlterParameterConstraint() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;

				// Note that the configuration from the XML is not read until the
				// buildValidatorFactory() method is called.
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowOverridingMethodAlterParameterConstraint() );
			}
		} );
	}

	@Test
	public void testAllowParallelMethodsDefineParameterConstraints() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;

				// Note that the configuration from the XML is not read until the
				// buildValidatorFactory() method is called.
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowParallelMethodsDefineParameterConstraints() );
			}
		} );
	}

	/**
	 * Reflect into the subject and find the first property of the given type.
	 *
	 * @param subject - the instance to reflect on
	 * @param clazz - exactly the class to match on
	 *
	 * @return
	 */
	@IgnoreForbiddenApisErrors(reason = "Prints the stacktrace in case an exception is raised")
	private <T extends Object> T findPropertyOfType(Object subject, Class<T> clazz) {
		Field[] fields = subject.getClass().getDeclaredFields();
		for ( Field field : fields ) {
			if ( field.getType().equals( clazz ) ) {
				boolean accessible = field.isAccessible();
				try {
					field.setAccessible( true );
					return clazz.cast( field.get( subject ) );
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				finally {
					field.setAccessible( accessible );
				}
			}
		}
		return null;
	}

	private void runWithCustomValidationXml(String validationXmlName, Runnable runnable) {
		new ValidationXmlTestHelper( ConfigurationFilePropertiesTest.class ).
			runWithCustomValidationXml( validationXmlName, runnable );
	}

}
