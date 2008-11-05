package javax.validation.spi;

import javax.validation.ValidationProviderResolver;

/**
 * Defines the state used to bootstrap the ValidatorFactoryBuilder
 *
 * @author Emmanuel Bernard
 */
public interface BootstrapState {
	/**
	 * returns the user defined ValidationProviderResolver strategy instance or <code>null</code> if undefined.
	 *
	 * @return ValidationProviderResolver instance or null
	 */
	ValidationProviderResolver getValidationProviderResolver();
}
