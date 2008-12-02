// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package javax.validation;

import javax.validation.bootstrap.DefaultValidationProviderResolver;
import javax.validation.bootstrap.GenericBuilderFactory;
import javax.validation.bootstrap.SpecializedBuilderFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ValidationProvider;

/**
 * This class is the entry point for the Bean Validation framework. There are three ways
 * to bootstrap the framework:
 * <ul>
 * <li>
 * The easiest approach is to use the default Bean Validation provider.
 * <pre>
 * ValidatorFactory factory = Validation.getBuilder().build();
 * </pre>
 * In this case {@link  javax.validation.bootstrap.DefaultValidationProviderResolver}
 * will be used to locate available providers.
 *
 * The chosen provider is defined as followed:
 * <ul>
 * <li>if the XML configuration defines a provider, this provider is used</li>
 * <li>if the XML configuration does not define a provider or if no XML configuration
 * is present the first provider returned by the ValidationProviderResolver
 * isntance is used.</li>
 * </ul>
 * </li>
 * <li>
 * The second bootstrap approach allows to choose a custom
 * <code>ValidationProviderResolver</code>. The chosen
 * <code>ValidationProvider</code> is then determined in the same way
 * as in the default bootstrapping case (see above).
 * <pre>
 * ValidatorFactoryBuilder&lt?&gt; builder = Validation
 *    .defineBootstrapState()
 *    .providerResolver( new MyResolverStrategy() )
 *    .getBuilder();
 * ValidatorFactory factory = builder.build();
 * </pre>
 * </li>
 * 
 * <p/>
 * <li>
 * The third approach allows you to specify explicitly and in
 * a type safe fashion the expected provider by
 * using its specific <code>ValidatorFactoryBuilder</code> sub-interface.
 *
 * Optionally you can choose a custom <code>ValidationProviderResolver</code>.
 * <pre>
 * ACMEValidatorFactoryBuilder builder = Validation
 *    .builderType(ACMEValidatorFactoryBuilder.class)
 *    .providerResolver( new MyResolverStrategy() )  // optionally set the provider resolver
 *    .getBuilder();
 * ValidatorFactory factory = builder.build();
 * </pre>
 * </li>
 * </ul>
 * Note:<br/>
 * <ul>
 * <li>
 * The ValidatorFactory object built by the bootstrap process should be cached
 * and shared amongst Validator consumers.
 * </li>
 * <li>
 * This class is thread-safe.
 * </li>
 * </ul>
 *
 * @author Emmanuel Bernard
 * @author Hardy Feretnschik
 * @see DefaultValidationProviderResolver
 */
public class Validation {

	/**
	 * Build a <code>ValidatorFactoryBuilder</code>. The actual provider
	 * choice is given by the XML configuration. If the
	 * XML configuration does not exsist the default is taken.
	 * <p/>
	 * The provider list is resolved using the
	 * {@link  javax.validation.bootstrap.DefaultValidationProviderResolver}.
	 *
	 * @return <code>ValidatorFactoryBuilder</code> instance.
	 */
	public static ValidatorFactoryBuilder<?> getBuilder() {
		return defineBootstrapState().getBuilder();
	}

	/**
	 * Build a <code>ValidatorFactoryBuilder</code>. The provider list is resolved
	 * using the strategy provided to the bootstrap state.
	 * <pre>
	 * ValidatorFactoryBuilder&lt?&gt; builder = Validation
	 *    .defineBootstrapState()
	 *    .providerResolver( new MyResolverStrategy() )
	 *    .getBuilder();
	 * ValidatorFactory factory = builder.build();
	 * </pre>
	 * The actual provider choice is given by the XML configuration. If the XML
	 * configuration does not exsist the first available provider will be returned.
	 *
	 * @return instance building a generic <code>ValidatorFactoryBuilder</code>
	 * compliant with the bootstrap state provided.
	 */
	public static GenericBuilderFactory defineBootstrapState() {
		return new GenericBuilderFactoryImpl();
	}

	/**
	 * Build a <code>ValidatorFactoryBuilder</code> for a particular provider implementation.
	 * Optionally override the provider resolution strategy used to determine the provider.
	 * <p/>
	 * Used by applications targeting a specific provider programmatically.
	 * <p/>
	 * <pre>
	 * ACMEValidatorFactoryBuilder builder = Validation.builderType(ACMEValidatorFactoryBuilder.class)
	 *     .providerResolver( new MyResolverStrategy() )
	 *     .build();
	 * </pre>,
	 * where <code>ACMEValidatorFactoryBuilder</code> is the <code>ValidatorFactoryBuilder</code>
	 * sub interface uniquely identifying the ACME Bean Validation provider.
	 *
	 * @param builderType the <code>ValidatorFactoryBuilder</code> sub interface
	 * uniquely defining the targeted provider.
	 *
	 * @return instance building a provider specific <code>ValidatorFactoryBuilder</code>
	 * sub interface implementation.
	 *
	 * @see #getBuilder()
	 */
	public static <T extends ValidatorFactoryBuilder<T>>
	        SpecializedBuilderFactory<T> builderType(Class<T> builderType) {
		return new SpecializedBuilderFactoryImpl<T>( builderType );
	}

	//private class, not exposed
	private static class SpecializedBuilderFactoryImpl<T extends ValidatorFactoryBuilder<T>>
			implements SpecializedBuilderFactory<T> {

		private Class<T> builderType;
		private ValidationProviderResolver resolver;

		public SpecializedBuilderFactoryImpl(Class<T> builderType) {
			this.builderType = builderType;
		}

		/**
		 * Optionally define the provider resolver implementation used.
		 * If not defined, use the default ValidationProviderResolver
		 *
		 * @param resolver ValidationProviderResolver implementation used
		 *
		 * @return self
		 */
		public SpecializedBuilderFactory<T> providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		/**
		 * Determine the provider implementation suitable for builderType and delegate the creation
		 * of this specific ValidatorFactoryBuilder subclass to the provider.
		 *
		 * @return a ValidatorFactoryBuilder sub interface implementation
		 */
		public T getBuilder() {
			if ( builderType == null ) {
				throw new ValidationException(
						"builder is mandatory. Use getBuilder() to use the generic provider discovery mechanism"
				);
			}
			if ( resolver == null ) {
				resolver = new DefaultValidationProviderResolver();
			}
			for ( ValidationProvider provider : resolver.getValidationProviders() ) {
				if ( provider.isSuitable( builderType ) ) {
					GenericBuilderFactoryImpl state = new GenericBuilderFactoryImpl();
					state.providerResolver( resolver );
					return provider.createSpecializedValidatorFactoryBuilder( state, builderType );
				}
			}
			throw new ValidationException( "Unable to find provider: " + builderType );
		}
	}

	//private class, not exposed
	private static class GenericBuilderFactoryImpl implements GenericBuilderFactory, BootstrapState {

		private ValidationProviderResolver resolver;

		public GenericBuilderFactory providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		public ValidationProviderResolver getValidationProviderResolver() {
			return resolver;
		}

		public ValidatorFactoryBuilder<?> getBuilder() {
			ValidationProviderResolver resolver = this.resolver == null ?
					new DefaultValidationProviderResolver() :
					this.resolver;

			if ( resolver.getValidationProviders().size() == 0 ) {
				//FIXME looks like an assertion error almost
				throw new ValidationException( "Unable to find a default provider" );
			}
			return resolver.getValidationProviders().get( 0 ).createGenericValidatorFactoryBuilder( this );
		}
	}
}
