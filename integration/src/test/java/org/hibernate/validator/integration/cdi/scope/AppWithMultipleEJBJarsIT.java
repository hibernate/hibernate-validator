/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

import java.lang.annotation.Annotation;
import javax.ejb.EJB;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.testutil.TestForIssue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test creates an EAR application with two EJB jars. Each EJB jar contains the same {@code PojoBean} class
 * which gets configured via Bean Validation constraint mappiung XML file. Depending on the EAR jar, the constraint
 * configuration differs, as a result validation of the pojo should differ depending on in which context (EJB jar) the
 * validation occurs.
 */
@RunWith(Arquillian.class)
@TestForIssue(jiraKey = "HV-987")
public class AppWithMultipleEJBJarsIT {
	private static final String EAR_FILE_NAME = AppWithMultipleEJBJarsIT.class.getSimpleName() + ".ear";
	private static final String EJB1_FILE_NAME = "ejb1.jar";
	private static final String EJB2_FILE_NAME = "ejb2.jar";
	private static final String TEST_JAR_FILE_NAME = "it-test.jar";

	@Deployment
	public static EnterpriseArchive createDeployment() throws Exception {

		JavaArchive ejbJar1 = getFirstEJBJar();

		JavaArchive ejbJar2 = getSecondEJBJAr();

		JavaArchive testClassesJar = ShrinkWrap.create( JavaArchive.class, TEST_JAR_FILE_NAME )
				.addClass( AppWithMultipleEJBJarsIT.class )
				.addClass( PojoBean.class );

		EnterpriseArchive ear = ShrinkWrap.create( EnterpriseArchive.class, EAR_FILE_NAME )
				.addAsModule( ejbJar1 )
				.addAsModule( ejbJar2 )
				.addAsLibrary( testClassesJar );

		return ear;
	}

	// Foo is contained in first EJB jar
	@EJB
	private FooRemote fooRemote;

	// Bar is contained in second EJB jar
	@EJB
	private BarRemote barRemote;

	@EJB
	private FooLocal fooLocal;

	@EJB
	private BarLocal barLocal;

	@Test
	public void test_pojo_included_in_first_ejb_has_min_constraint() throws Exception {
		// min is 100
		int violations = fooRemote.verifyValidatorOnPojo( 150 );
		assertTrue(
				"Valid values are [100, Integer.MAX_VALUE). Setting 150 for Foo is correct," +
						" so violations should be 0, but actually : " + violations,
				violations == 0
		);

		violations = fooRemote.verifyValidatorOnPojo( 50 );
		assertTrue(
				"Valid values are [100, Integer.MAX_VALUE). Setting 50 for Foo is not correct," +
						" so violations should be 1, but actually : " + violations,
				violations == 1
		);
	}

	@Test
	public void test_pojo_included_in_second_ejb_has_max_constraint() throws Exception {
		// max is 10
		int violations = barRemote.verifyValidatorOnPojo( 5 );
		assertTrue(
				"Valid values are (Integer.MIN_VALUE, 10]. Setting 5 for Bar is correct," +
						" so violations should be 0, but actually : " + violations,
				violations == 0
		);

		violations = barRemote.verifyValidatorOnPojo( 15 );
		assertTrue(
				"Valid values are (Integer.MIN_VALUE, 10]. Setting 15 for Bar is not correct," +
						" so violations should be 1, but actually : " + violations,
				violations == 1
		);
	}

	@Test
	public void test_validator_instances_retrieved_from_separate_ejb_jars_have_independent_configuration()
			throws Exception {
		Validator validator1 = fooLocal.getValidator();
		Validator validator2 = barLocal.getValidator();

		assertTrue( "Retrieved validator instances cannot be the same", validator1 != validator2 );

		Annotation constraintAnnotation = getConstraintAnnotationFromMetadata( validator1 );
		assertEquals( "Unexpected constraint type", Min.class, constraintAnnotation.annotationType() );

		constraintAnnotation = getConstraintAnnotationFromMetadata( validator2 );
		assertEquals( "Unexpected constraint type", Max.class, constraintAnnotation.annotationType() );
	}

	private Annotation getConstraintAnnotationFromMetadata(Validator validator1) {
		javax.validation.metadata.BeanDescriptor beanDescriptor = validator1.getConstraintsForClass( PojoBean.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "value" );
		assertEquals(
				"Unexpected number of constraint descriptors",
				1,
				propertyDescriptor.getConstraintDescriptors().size()
		);
		ConstraintDescriptor constraintDescriptor = propertyDescriptor.getConstraintDescriptors().iterator().next();
		return constraintDescriptor.getAnnotation();
	}

	private static JavaArchive getFirstEJBJar() {
		JavaArchive ejbJar1 = ShrinkWrap.create( JavaArchive.class, EJB1_FILE_NAME )
				.addClass( FooRemote.class )
				.addClass( FooLocal.class )
				.addClass( FooBean.class );

		ejbJar1.add(
				new ValidationXmlBuilder().constraintMapping( "META-INF/constraint-mappings.xml" ).build(),
				"META-INF/validation.xml"
		);

		ejbJar1.add(
				ConstraintMappingXmlFactory.build(
						"<bean class=\"" + PojoBean.class.getName() + "\" ignore-annotations=\"false\">\n" +
								"    <field name=\"value\">\n" +
								"      <constraint annotation=\"javax.validation.constraints.Min\">\n" +
								"        <element name=\"value\">100</element>\n" +
								"      </constraint>\n" +
								"    </field>\n" +
								"  </bean>\n"
				), "META-INF/constraint-mappings.xml"
		);
		return ejbJar1;
	}

	private static JavaArchive getSecondEJBJAr() {
		JavaArchive ejbJar2 = ShrinkWrap.create( JavaArchive.class, EJB2_FILE_NAME )
				.addClass( BarRemote.class )
				.addClass( BarLocal.class )
				.addClass( BarBean.class );

		ejbJar2.add(
				new ValidationXmlBuilder()
						.constraintMapping( "META-INF/constraint-mappings.xml" )
						.build(),
				"META-INF/validation.xml"
		);

		ejbJar2.add(
				ConstraintMappingXmlFactory.build(
						"<bean class=\"" + PojoBean.class.getName() + "\" ignore-annotations=\"false\">\n" +
								"    <field name=\"value\">\n" +
								"      <constraint annotation=\"javax.validation.constraints.Max\">\n" +
								"        <element name=\"value\">10</element>\n" +
								"      </constraint>\n" +
								"    </field>\n" +
								"  </bean>"
				), "META-INF/constraint-mappings.xml"
		);
		return ejbJar2;
	}
}
