/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.TypeVariable;
import java.util.Set;

import jakarta.validation.metadata.GroupConversionDescriptor;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;

/**
 * An aggregated view of the cascading validation metadata. Note that it also includes the cascading validation metadata
 * defined on the root element via the {@link ArrayElement} and {@link AnnotatedObject} pseudo type parameters.
 * <p>
 * To reduce the memory footprint, {@code CascadingMetaData} comes in 2 variants:
 * <ul>
 * <li>{@link NonContainerCascadingMetaData} dedicated to non containers: it is very lightweight;</li>
 * <li>{@link ContainerCascadingMetaData} used for containers: it is the full featured version.</li>
 * </ul>
 *
 * @author Guillaume Smet
 */
public interface CascadingMetaData {

	TypeVariable<?> getTypeParameter();

	boolean isCascading();

	boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements();

	Class<?> convertGroup(Class<?> originalGroup);

	Set<GroupConversionDescriptor> getGroupConversionDescriptors();

	boolean isContainer();

	<T extends CascadingMetaData> T as(Class<T> clazz);

	/**
	 * Add additional cascading metadata when:
	 * <ul>
	 * <li>the element is marked with {@code @Valid},</li>
	 * <li>the runtime type of the element is container based (e.g. collections, maps or arrays),</li>
	 * <li>and there are {@link ValueExtractor}s present for such container,</li>
	 * <li>and the declared type isn't container based.</li>
	 * </ul>
	 * <p>
	 * An example of this particular situation is: {@code @Valid private Object element = new ArrayList<String>()}.
	 * <p>
	 * Note that if the declared type is container based, the cascading information is directly included at bootstrap
	 * time.
	 */
	CascadingMetaData addRuntimeContainerSupport(ValueExtractorManager valueExtractorManager, Class<?> valueClass);
}
