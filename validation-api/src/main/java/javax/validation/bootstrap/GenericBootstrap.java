package javax.validation.bootstrap;

import javax.validation.ValidationProviderResolver;
import javax.validation.Configuration;

/**
 * Defines the state used to bootstrap Bean Validation and
 * creates a provider agnostic Configuration.
 *
 * @author Emmanuel Bernard
 */
public interface GenericBootstrap {
	/**
	 * Defines the provider resolution strategy.
	 * This resolver returns the list of providers evaluated
	 * to build the Configuration
	 * <p/>
	 * If no resolver is defined, the default ValidationProviderResolver
	 * implementation is used.
	 *
	 * @return <code>this</code> following the chaining method pattern
	 */
	GenericBootstrap providerResolver(ValidationProviderResolver resolver);

	/**
	 * Returns a generic Configuration implementation.
	 * At this stage the provider used to build the ValidatorFactory is not defined.
	 * <p/>
	 * The Configuration implementation is provided by the first provider returned
	 * by the ValidationProviderResolver strategy.
	 *
	 * @return a Configuration implementation compliant with the bootstrap state
	 */
	Configuration<?> configure();
}
