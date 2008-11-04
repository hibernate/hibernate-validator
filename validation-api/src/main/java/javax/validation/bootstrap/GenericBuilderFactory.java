package javax.validation.bootstrap;

import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactoryBuilder;

/**
 * Defines the state used to bootstrap Bean Validation and create an appropriate
 * ValidatorFactoryBuilder
 *
 * @author Emmanuel Bernard
 */
public interface GenericBuilderFactory {
	/**
	 * Defines the provider resolution strategy.
	 * This resolver returns the list of providers evaluated
	 * to build the ValidationBuilder
	 * <p/>
	 * If no resolver is defined, the default ValidationProviderResolver
	 * implementation is used.
	 *
	 * @return <code>this</code> following the chaining method pattern
	 */
	GenericBuilderFactory providerResolver(ValidationProviderResolver resolver);

	/**
	 * Returns a generic ValidatorFactoryBuilder implementation.
	 * At this stage the provider used to build the ValidationFactory is not defined.
	 * <p/>
	 * The ValidatorFactoryBuilder implementation is provided by the first provider returned
	 * by the ValidationProviderResolver strategy.
	 *
	 * @return a ValidatorFactoryBuilder implementation compliant with the bootstrap state
	 */
	ValidatorFactoryBuilder<?> getBuilder();
}
