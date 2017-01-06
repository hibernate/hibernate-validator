/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.jandex.model;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.test.internal.metadata.jandex.JandexMetaDataProviderTest;

/**
 * Simple bean class to be used in {@link JandexMetaDataProviderTest}.
 *
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilderModel {

	@Max(value = 1, groups = { Group1.class })
	@ConvertGroup(from = Group1.class, to = Group2.class)
	private String var1;

	@Min(value = 2, groups = Group3.class)
	@ConvertGroup(from = Group1.class, to = Group2.class)
	private String var2;

	private Optional<@NotBlank String> optional;

	@Valid
	private List<@NotNull String> list;

	public ConstrainedFieldJandexBuilderModel(@Max(value = 1, groups = { Group1.class }) String var1, @Min(value = 2, groups = Group3.class) String var2) {
		this.var1 = var1;
		this.var2 = var2;
	}

	@Valid
	@NotNull
	public String someMethod1(@NotNull String param1, @Valid @NotNull @Size(min = 5) List<@NotNull String> param2) {
		return "";
	}

	private interface Group1 {

	}

	private interface Group2 {

	}

	private interface Group3 {

	}

}
