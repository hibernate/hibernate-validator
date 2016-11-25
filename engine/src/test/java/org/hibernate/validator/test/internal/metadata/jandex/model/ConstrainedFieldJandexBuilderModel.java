/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.jandex.model;


import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.test.internal.metadata.jandex.ConstrainedFieldJandexBuilderTest;

/**
 * Simple bean class to be used in {@link ConstrainedFieldJandexBuilderTest}.
 *
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilderModel {

	@Max(1)
	@ConvertGroup(from = Group1.class, to = Group2.class)
	private String var1;

	@Min(2)
	@ConvertGroup.List({ @ConvertGroup(from = Group1.class, to = Group2.class), @ConvertGroup(from = Group3.class, to = Group2.class) })
	private String var2;

	private Optional<String> optional;

	private List<String> list;

	private interface Group1 {
	}

	private interface Group2 {
	}

	private interface Group3 {
	}

}
