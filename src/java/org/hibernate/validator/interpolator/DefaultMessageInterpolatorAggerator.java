//$Id: $
package org.hibernate.validator.interpolator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.Serializable;

import org.hibernate.validator.MessageInterpolator;
import org.hibernate.validator.Validator;
import org.hibernate.AssertionFailure;

/**
 * @author Emmanuel Bernard
 */
public class DefaultMessageInterpolatorAggerator implements MessageInterpolator, Serializable {
	private Map<Validator, DefaultMessageInterpolator> interpolators = new HashMap<Validator, DefaultMessageInterpolator>();
	//transient but repopulated by the object owing a reference to the interpolator
	private transient ResourceBundle messageBundle;
	//transient but repopulated by the object owing a reference to the interpolator
	private transient ResourceBundle defaultMessageBundle;

	//not an interface method
	public void initialize(ResourceBundle messageBundle, ResourceBundle defaultMessageBundle) {
		this.messageBundle = messageBundle;
		this.defaultMessageBundle = defaultMessageBundle;
		//useful when we deserialize
		for ( DefaultMessageInterpolator interpolator : interpolators.values() ) {
			interpolator.initialize( messageBundle, defaultMessageBundle );
		}
	}

	public void addInterpolator(Annotation annotation, Validator validator) {
		DefaultMessageInterpolator interpolator = new DefaultMessageInterpolator();
		interpolator.initialize(messageBundle, defaultMessageBundle );
		interpolator.initialize( annotation, null );
		interpolators.put( validator, interpolator );
	}

	public String interpolate(String message, Validator validator, MessageInterpolator defaultInterpolator) {
		DefaultMessageInterpolator defaultMessageInterpolator = interpolators.get( validator );
		if (defaultMessageInterpolator == null) {
			return message;
		}
		else {
			return defaultMessageInterpolator.interpolate( message, validator, defaultInterpolator );
		}
	}

	public String getAnnotationMessage(Validator validator) {
		DefaultMessageInterpolator defaultMessageInterpolator = interpolators.get( validator );
		String message = defaultMessageInterpolator != null ? defaultMessageInterpolator.getAnnotationMessage() : null;
		if (message == null) throw new AssertionFailure("Validator not registred to the messageInterceptorAggregator");
		return message;
	}
}
