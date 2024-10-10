/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter07;

import java.util.OptionalInt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.valueextraction.Unwrapping;

import com.google.common.collect.Multimap;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.StringProperty;

@SuppressWarnings({ "restriction", "unused" })
public class Container {

	//tag::multimapValues[]
	private Multimap<String, @NotBlank String> map1;
	//end::multimapValues[]

	//tag::multimapKeysAndValues[]
	private Multimap<@NotBlank String, @NotBlank String> map2;
	//end::multimapKeysAndValues[]

	//tag::optionalIntUnwrap[]
	@Min(value = 5, payload = Unwrapping.Unwrap.class)
	private OptionalInt optionalInt1;
	//end::optionalIntUnwrap[]

	//tag::optionalInt[]
	@Min(5)
	private OptionalInt optionalInt2;
	//end::optionalInt[]

	//tag::optionalIntSkip[]
	@NotNull(payload = Unwrapping.Skip.class)
	@Min(5)
	private OptionalInt optionalInt3;
	//end::optionalIntSkip[]

	//tag::stringProperty[]
	@NotBlank
	private StringProperty stringProperty;
	//end::stringProperty[]

	//tag::longProperty[]
	@Min(5)
	private LongProperty longProperty;
	//end::longProperty[]

	//tag::listProperty[]
	@Size(min = 1)
	private ReadOnlyListProperty<@NotBlank String> listProperty;
	//end::listProperty[]
}
