package org.hibernate.validator.internal.engine.messageinterpolation.el;

import jakarta.el.ELException;

public class DisabledFeatureELException extends ELException {

	DisabledFeatureELException(String message) {
		super( message );
	}
}
