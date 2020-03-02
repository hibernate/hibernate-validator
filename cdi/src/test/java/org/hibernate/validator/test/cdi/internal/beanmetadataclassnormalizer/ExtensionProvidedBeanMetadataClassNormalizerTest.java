/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.beanmetadataclassnormalizer;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.cdi.HibernateValidator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

public class ExtensionProvidedBeanMetadataClassNormalizerTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" )
				// Register the CDI extension that provides the normalizer bean
				.addAsManifestResource(
						new StringAsset( CustomProxyBeanMetadataClassNormalizerCdiExtension.class.getName() ),
						"services/jakarta.enterprise.inject.spi.Extension"
				);
	}

	@HibernateValidator
	@Inject
	ValidatorFactory validatorFactory;

	@HibernateValidator
	@Inject
	Validator validator;

	@Inject
	ValidatorFactory defaultValidatorFactory;

	@Inject
	Validator defaultValidator;

	@Test
	public void testProxyMetadataIgnoredWithQualifiedValidator() throws Exception {
		assertThat( validator ).isNotNull();
		doTest( validator );
	}

	@Test
	public void testProxyMetadataIgnoredWithDefaultValidator() throws Exception {
		assertThat( defaultValidator ).isNotNull();
		doTest( defaultValidator );
	}

	@Test
	public void testProxyMetadataIgnoredWithQualifiedValidatorFactory() throws Exception {
		assertThat( validatorFactory ).isNotNull();
		doTest( validatorFactory.getValidator() );
	}

	@Test
	public void testProxyMetadataIgnoredWithDefaultValidatorFactory() throws Exception {
		assertThat( defaultValidatorFactory ).isNotNull();
		doTest( defaultValidatorFactory.getValidator() );
	}

	private void doTest(Validator validator) {
		assertThat( validator ).isNotNull();
		/*
		 * Even though we pass an instance of the proxy class that has invalid annotations,
		 * we expect those to be ignored
		 * because of the class normalizer we defined.
		 */
		assertThat( validator.validate( new TestEntityProxy() ) ).hasSize( 1 );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}

	public static class TestEntityProxy extends TestEntity implements CustomProxy {
		/*
		 * This is invalid, but should be ignored because it's defined in a proxy class which gets ignored
		 * because of the class normalizer we defined.
		 */
		@DecimalMax(value = "foo")
		private String foo;
	}
}
