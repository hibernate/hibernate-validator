package org.hibernate.validation.util;

import javax.validation.ValidatorFactory;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.MessageInterpolator;
import javax.validation.Configuration;
import javax.validation.Validation;

/**
 * This class lazily initialize the ValidatorFactory on the first usage
 * One benefit is that no domain class is loaded until the
 * ValidatorFactory is really needed.
 * Useful to avoid loading classes before JPA is initialized
 * and has enhanced its classes.
 *
 * Experimental, not considered a public API
 * @author Emmanuel Bernard
 */
public class LazyValidatorFactory implements ValidatorFactory {

	private final Configuration<?> configuration;
	private volatile ValidatorFactory delegate; //use as a barrier

	/**
	 * Use the default ValidatorFactory creation routine
	 */
	public LazyValidatorFactory() {
		this(null);
	}

	public LazyValidatorFactory(Configuration<?> configuration) {
		this.configuration = configuration;
	}

	public Validator getValidator() {
		if ( delegate == null ) {
			initFactory();
		}
		return delegate.getValidator();
	}

	//we can initialize several times that's ok
	private void initFactory() {
		if ( configuration == null ) {
			delegate = Validation.buildDefaultValidatorFactory();
		}
		else {
			delegate = configuration.buildValidatorFactory();
		}
	}

	public ValidatorContext usingContext() {
		if ( delegate == null ) {
			initFactory();
		}
		return delegate.usingContext();
	}

	public MessageInterpolator getMessageInterpolator() {
		if ( delegate == null ) {
			initFactory();
		}
		return delegate.getMessageInterpolator();
	}

	public <T> T unwrap(Class<T> clazz) {
		if ( delegate == null ) {
			initFactory();
		}
		return delegate.unwrap( clazz );
	}
}
