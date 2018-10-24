/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.properties.Constrainable;

/**
 * @author Marko Bekhta
 */
public final class NonCascadingMetaDataBuilder implements CascadingMetaDataBuilder {

	public static final CascadingMetaDataBuilder INSTANCE = new NonCascadingMetaDataBuilder();

	@Override
	public boolean isCascading() {
		return false;
	}

	@Override
	public Map<Class<?>, Class<?>> getGroupConversions() {
		return Collections.emptyMap();
	}

	@Override
	public boolean hasContainerElementsMarkedForCascading() {
		return false;
	}

	@Override
	public boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements() {
		return false;
	}

	@Override
	public boolean hasGroupConversionsOnAnnotatedObjectOrContainerElements() {
		return false;
	}

	@Override
	public Map<TypeVariable<?>, CascadingMetaDataBuilder> getContainerElementTypesCascadingMetaData() {
		return Collections.emptyMap();
	}

	@Override
	public CascadingMetaDataBuilder merge(CascadingMetaDataBuilder otherCascadingTypeParameter) {
		return otherCascadingTypeParameter;
	}

	@Override
	public CascadingMetaData build(ValueExtractorManager valueExtractorManager, Constrainable context) {
		return NonContainerCascadingMetaData.of( false, Collections.emptyMap() );
	}
}
