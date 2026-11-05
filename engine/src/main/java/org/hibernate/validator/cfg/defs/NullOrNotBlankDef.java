
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NullOrNotBlank;

/**
 * Constraint definition for {@link NullOrNotBlank}.
 * @author Koen Aers
 * @since 9.2
 */
public class NullOrNotBlankDef extends ConstraintDef<NullOrNotBlankDef, NullOrNotBlank> {

	public NullOrNotBlankDef() {
		super( NullOrNotBlank.class );
	}
}
