/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.properties.Constrainable;

/**
 * @author Marko Bekhta
 */
public interface CascadingMetaDataBuilder {

	static CascadingMetaDataBuilder nonCascading() {
		return NonCascadingMetaDataBuilder.INSTANCE;
	}

	static CascadingMetaDataBuilder annotatedObject(Type cascadableType, boolean cascading, List<CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new SimpleBeanCascadingMetaDataBuilder( cascadableType, AnnotatedObject.INSTANCE, cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	static CascadingMetaDataBuilder annotatedObject(Type cascadableType, boolean cascading, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new SimpleBeanCascadingMetaDataBuilder( cascadableType, AnnotatedObject.INSTANCE, cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	static CascadingMetaDataBuilder typeArgument(Type cascadableType, TypeVariable<?> typeParameter, boolean cascading, List<CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new SimpleBeanCascadingMetaDataBuilder( cascadableType, typeParameter, cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	boolean isCascading();

	Map<Class<?>, Class<?>> getGroupConversions();

	boolean hasContainerElementsMarkedForCascading();

	boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements();

	boolean hasGroupConversionsOnAnnotatedObjectOrContainerElements();

	Map<TypeVariable<?>, CascadingMetaDataBuilder> getContainerElementTypesCascadingMetaData();

	CascadingMetaDataBuilder merge(CascadingMetaDataBuilder otherCascadingTypeParameter);

	CascadingMetaData build(ValueExtractorManager valueExtractorManager, Constrainable context);

}
