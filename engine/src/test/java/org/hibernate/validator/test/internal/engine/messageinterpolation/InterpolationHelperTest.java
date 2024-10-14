/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.escapeMessageParameter;

import org.testng.annotations.Test;

public class InterpolationHelperTest {

	@Test
	public void testEscapeMessageParameter() {
		assertThat( escapeMessageParameter( null ) ).isNull();
		assertThat( escapeMessageParameter( "test" ) ).isEqualTo( "test" );
		assertThat( escapeMessageParameter( "{}" ) ).isEqualTo( "\\{\\}" );
		assertThat( escapeMessageParameter( "${\\}" ) ).isEqualTo( "\\$\\{\\\\\\}" );
	}

}
