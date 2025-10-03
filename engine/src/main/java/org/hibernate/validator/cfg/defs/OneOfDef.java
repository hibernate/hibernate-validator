/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.OneOf;

public class OneOfDef extends ConstraintDef<OneOfDef, OneOf> {

	public OneOfDef() {
		super( OneOf.class );
	}

	public OneOfDef enumClass(Class<? extends Enum<?>> enumClass) {
		addParameter( "enumClass", enumClass );
		return this;
	}

	public OneOfDef allowedIntegers(int[] allowedIntegers) {
		addParameter( "allowedIntegers", allowedIntegers );
		return this;
	}

	public OneOfDef allowedLongs(long[] allowedLongs) {
		addParameter( "allowedLongs", allowedLongs );
		return this;
	}

	public OneOfDef allowedFloats(float[] allowedFloats) {
		addParameter( "allowedFloats", allowedFloats );
		return this;
	}

	public OneOfDef allowedDoubles(double[] allowedDoubles) {
		addParameter( "allowedDoubles", allowedDoubles );
		return this;
	}

	public OneOfDef allowedValues(String[] allowedValues) {
		addParameter( "allowedValues", allowedValues );
		return this;
	}
}
