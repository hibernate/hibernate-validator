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
import javax.validation.bootstrap.GenericBootstrap;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ValidationProvider;

/**
 * This class is the entry point for the Bean Validation framework. There are three ways
 * to bootstrap the framework:
 * <ul>
 * <li>
 * The easiest approach is to use the default Bean Validation provider.
 * <pre>
 * ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
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
 * Configuration&lt?&gt; configuration = Validation
 *    .byDefaultProvider()
 *    .providerResolver( new MyResolverStrategy() )
 *    .configure();
 * ValidatorFactory factory = configuration.buildValidatorFactory();
 * </pre>
 * </li>
 * 
 * <p/>
 * <li>
 * The third approach allows you to specify explicitly and in
 * a type safe fashion the expected provider by
 * using its specific <code>Configuration</code> sub-interface.
 *
 * Optionally you can choose a custom <code>ValidationProviderResolver</code>.
 * <pre>
 * ACMEConfiguration configuration = Validation
 *    .byProvider(ACMEConfiguration.class)
 *    .providerResolver( new MyResolverStrategy() )  // optionally set the provider resolver
 *    .configure();
 * ValidatorFactory factory = configuration.buildValidatorFactory();
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
	 * Build and return a ValidatorFactory instanced based on the
	 * default Bean Validation provider and following the
	 * XML configuration.
	 * <p/>
	 * The provider list is resolved using the
	 * {@link  javax.validation.bootstrap.DefaultValidationProviderResolver}.
	 * <p/> The code is semantically equivalent to
	 * <code>Validation.byDefaultProvider().configure().buildValidatorFactory()</code>
	 *
	 * @return <code>ValidatorFactory</code> instance.
	 */
	public static ValidatorFactory buildDefaultValidatorFactory() {
		return byDefaultProvider().configure().buildValidatorFactory();
	}

	/**
	 * Build a <code>Configuration</code>. The provider list is resolved
	 * using the strategy provided to the bootstrap state.
	 * <pre>
	 * Configuration&lt?&gt; configuration = Validation
	 *    .byDefaultProvider()
	 *    .providerResolver( new MyResolverStrategy() )
	 *    .configure();
	 * ValidatorFactory factory = configuration.buildValidatorFactory();
	 * </pre>
	 * The actual provider choice is given by the XML configuration. If the XML
	 * configuration does not exsist the first available provider will be returned.
	 *
	 * @return instance building a generic <code>Configuration</code>
	 * compliant with the bootstrap state provided.
	 */
	public static GenericBootstrap byDefaultProvider() {
		return new GenericBootstrapImpl();
	}

	/**
	 * Build a <code>Configuration</code> for a particular provider implementation.
	 * Optionally override the provider resolution strategy used to determine the provider.
	 * <p/>
	 * Used by applications targeting a specific provider programmatically.
	 * <p/>
	 * <pre>
	 * ACMEConfiguration configuration =
	 *     Validation.byProvider(ACMEConfiguration.class)
	 *             .providerResolver( new MyResolverStrategy() )
	 *             .configure();
	 * </pre>,
	 * where <code>ACMEConfiguration</code> is the
	 * <code>Configuration</code> sub interface uniquely identifying the
	 * ACME Bean Validation provider.
	 *
	 * @param configurationType the <code>Configuration</code> sub interface
	 * uniquely defining the targeted provider.
	 *
	 * @return instance building a provider specific <code>Configuration</code>
	 * sub interface implementation.
	 *
	 */
	public static <T extends Configuration<T>>
	ProviderSpecificBootstrap<T> byProvider(Class<T> configurationType) {
		return new ProviderSpecificBootstrapImpl<T>( configurationType );
	}

	//private class, not exposed
	private static class ProviderSpecificBootstrapImpl<T extends Configuration<T>>
			implements ProviderSpecificBootstrap<T> {

		private Class<T> configurationType;
		private ValidationProviderResolver resolver;

		public ProviderSpecificBootstrapImpl(Class<T> configurationType) {
			this.configurationType = configurationType;
		}

		/**
		 * Optionally define the provider resolver implementation used.
		 * If not defined, use the default ValidationProviderResolver
		 *
		 * @param resolver ValidationProviderResolver implementation used
		 *
		 * @return self
		 */
		public ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		/**
		 * Determine the provider implementation suitable for byProvider(Class)
		 * and delegate the creation of this specific Configuration subclass to the provider.
		 *
		 * @return a Configuration sub interface implementation
		 */
		public T configure() {
			if ( configurationType == null ) {
				throw new ValidationException(
						"builder is mandatory. Use Validation.byDefaultProvider() to use the generic provider discovery mechanism"
				);
			}
			if ( resolver == null ) {
				resolver = new DefaultValidationProviderResolver();
			}
			for ( ValidationProvider provider : resolver.getValidationProviders() ) {
				if ( provider.isSuitable( configurationType ) ) {
					GenericBootstrapImpl state = new GenericBootstrapImpl();
					state.providerResolver( resolver );
					return provider.createSpecializedConfiguration( state, configurationType );
				}
			}
			throw new ValidationException( "Unable to find provider: " + configurationType );
		}
	}

	//private class, not exposed
	private static class GenericBootstrapImpl implements GenericBootstrap, BootstrapState {

		private ValidationProviderResolver resolver;

		public GenericBootstrap providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		public ValidationProviderResolver getValidationProviderResolver() {
			return resolver;
		}

		public Configuration<?> configure() {
			ValidationProviderResolver resolver = this.resolver == null ?
					new DefaultValidationProviderResolver() :
					this.resolver;

			if ( resolver.getValidationProviders().size() == 0 ) {
				//FIXME looks like an assertion error almost
				throw new ValidationException( "Unable to find a default provider" );
			}
			return resolver.getValidationProviders().get( 0 ).createGenericConfiguration( this );
		}
	}
}
