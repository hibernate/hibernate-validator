package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Future;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Future} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class FutureDef extends ConstraintDef<FutureDef, Future> {

	public FutureDef() {
		super( Future.class );
	}
}
