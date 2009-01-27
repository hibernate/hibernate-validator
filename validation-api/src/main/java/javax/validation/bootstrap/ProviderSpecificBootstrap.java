package javax.validation.bootstrap;

import javax.validation.ValidationProviderResolver;
import javax.validation.Configuration;

/**
 * Defines the state used to bootstrap Bean Validation and
 * creates a provider specific Configuration. The specific Configuration
 * sub interface uniquely identifying a provider.
 * <p/>
 * The requested provider is the first provider suitable for T (as defined in
 * {@link javax.validation.spi.ValidationProvider#isSuitable(Class)}). The
 * list of providers evaluated is returned by {@link ValidationProviderResolver}.
 * If no ValidationProviderResolver is defined, the
 * default ValidationProviderResolver strategy is used.
 *
 * @author Emmanuel Bernard
 */
public interface ProviderSpecificBootstrap<T extends Configuration<T>> {

	/**
	 * Optionally define the provider resolver implementation used.
	 * If not defined, use the default ValidationProviderResolver
	 *
	 * @param resolver ValidationProviderResolver implementation used
	 *
	 * @return self
	 */
	public ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver resolver);

	/**
	 * Determine the provider implementation suitable for configurationType and delegate
	 * the creation of this specific Configuration subclass to the provider.
	 *
	 * @return a Configuration sub interface implementation
	 */
	public T configure();
}
