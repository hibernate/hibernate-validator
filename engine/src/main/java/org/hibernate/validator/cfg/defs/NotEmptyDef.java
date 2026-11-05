package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NotEmpty;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NotEmpty} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NotEmptyDef extends ConstraintDef<NotEmptyDef, NotEmpty> {

	public NotEmptyDef() {
		super( NotEmpty.class );
	}
}
