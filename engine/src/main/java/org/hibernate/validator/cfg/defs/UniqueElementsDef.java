
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * An {@link UniqueElements} constraint definition.
 *
 * @author Guillaume Smet
 * @since 6.0.5
 */
public class UniqueElementsDef extends ConstraintDef<UniqueElementsDef, UniqueElements> {

	public UniqueElementsDef() {
		super( UniqueElements.class );
	}
}
