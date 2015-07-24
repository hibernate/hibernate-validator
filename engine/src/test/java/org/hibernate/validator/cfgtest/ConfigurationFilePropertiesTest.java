/**
 * Hibernate Validator, declare and validate application constraints
 * <p>
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfgtest;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.junit.After;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;
import java.lang.reflect.Field;
import java.net.URL;

public class ConfigurationFilePropertiesTest {

    @Test
    public void testConfigurationAvailable() {
        URL validationUrl = Thread.currentThread().getContextClassLoader().getResource( "META-INF/validation.xml" );
        Assert.assertNotNull( validationUrl, "unable to find 'META-INF/validation.xml' on classpath" );
    }

    /**
     * The following test assumes that the file META-INF/validation.xml is present and
     * contains:
     * <property name="hibernate.validator.allow_parameter_constraint_override">true</property>
     * <property name="hibernate.validator.allow_multiple_cascaded_validation_on_return_values">true</property>
     * <property name="hibernate.validator.allow_parallel_method_parameter_constraint">true</property>
     * <property name="hibernate.validator.fail_fast">true</property>
     * <p>
     * The Maven build runs this test in a separate execution of surefire, which adds the
     * path to the required file onto its classpath.
     */
    @Test
    public void testAllowMultipleCascadedValidationOnReturnValues() {
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

    @Test
    public void testAllowOverridingMethodAlterParameterConstraint() {
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

    @Test
    public void testAllowParallelMethodsDefineParameterConstraints() {
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

    @After
    public void afterTest() {

    }

    /**
     * Reflect into the subject and find the first property of the given type.
     *
     * @param subject - the instance to reflect on
     * @param clazz   - exactly the class to match on
     * @return
     */
    private <T extends Object> T findPropertyOfType( Object subject, Class<T> clazz ) {
        Field[] fields = subject.getClass().getDeclaredFields();
        for( Field field : fields ) {
            if( field.getType().equals( clazz ) ) {
                boolean accessible = field.isAccessible();
                try {
                    field.setAccessible( true );
                    return (T) field.get( subject );
                }
                catch( IllegalArgumentException e ) {
                    e.printStackTrace();
                }
                catch( IllegalAccessException e ) {
                    e.printStackTrace();
                }
                finally {
                    field.setAccessible( accessible );
                }
            }
        }
        return null;
    }

}
