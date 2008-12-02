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
package javax.validation.spi;

import javax.validation.ValidatorFactoryBuilder;
import javax.validation.ValidatorFactory;

/**
 * Contract between the validation bootstrap mechanism and the provider engine.
 * <p/>
 * Implementations must have a public no-arg constructor. The construction of a provider
 * should be as "lightweight" as possible.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ValidationProvider {
	/**
	 * This sub interface uniquely identify a provider.
	 * 
	 * @param builderClass targeted builder class.
	 *
	 * @return <code>true</code> if <code>builderClass</code> is the Bean Validation Provider sub
	 *         interface for ValidatorFactoryBuilder
	 */
	boolean isSuitable(Class<? extends ValidatorFactoryBuilder<?>> builderClass);

	/**
	 * Returns a ValidatorFactoryBuilder instance implementing the
	 * <code>builderType</code> interface.
	 * The ValidatorFactoryBuilder instance uses the current provider (<code>this</code>)
	 * to build the ValidatorFactory instance.
	 * <p/>
	 * This method can only be called on providers returning true on <
	 * code>#isSuitable(builderType)</code>
	 *
	 * @param builderClass the Builder class type
	 * @param state bootstrap state
	 *
	 * @return specific validator builder implementation
	 */
	<T extends ValidatorFactoryBuilder<T>> T createSpecializedValidatorFactoryBuilder(
			BootstrapState state,
			Class<T> builderClass);

	/**
	 * Returns a ValidatorFactoryBuilder instance. This instance is not bound to
	 * use the current provider. The choice of provider follows the algorithm described
	 * in {@link javax.validation.ValidatorFactoryBuilder}
	 * <p/>
	 * The ValidationProviderResolver used is provided by <code>state</code>.
	 * If null, the default ValidationProviderResolver is used.
	 *
	 * @param state bootstrap state
	 *
	 * @return validator builder implementation
	 */
	ValidatorFactoryBuilder<?> createGenericValidatorFactoryBuilder(BootstrapState state);

	/**
	 * Build a ValidatorFactory using the current provider implementation. The
	 * ValidatorFactory is assembled and follow the configuration passed
	 * using ValidatorFactoryConfiguration.
	 * <p>
	 * The returned ValidatorFactory is properly initialized and ready for use.
	 * </p>
	 *
	 * @param configuration the configuration descriptor
	 *
	 * @return the instanciated ValidatorFactory
	 */
	ValidatorFactory buildValidatorFactory(ValidatorFactoryConfiguration configuration);
}