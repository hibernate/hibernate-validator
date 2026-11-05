
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Size;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Size} constraint definition.
 * @author Hardy Ferentschik
 */
public class SizeDef extends ConstraintDef<SizeDef, Size> {

	public SizeDef() {
		super( Size.class );
	}

	public SizeDef min(int min) {
		addParameter( "min", min );
		return this;
	}

	public SizeDef max(int max) {
		addParameter( "max", max );
		return this;
	}
}
