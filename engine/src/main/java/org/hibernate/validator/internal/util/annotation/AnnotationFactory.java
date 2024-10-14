/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.annotation;

import java.lang.annotation.Annotation;

import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.NewProxyInstance;

/**
 * Creates live annotations (actually {@link AnnotationProxy} instances) from {@code AnnotationDescriptor}s.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Hardy Ferentschik
 * @see AnnotationProxy
 */
public class AnnotationFactory {

	private AnnotationFactory() {
	}

	public static <T extends Annotation> T create(AnnotationDescriptor<T> descriptor) {
		return NewProxyInstance.action(
				GetClassLoader.fromClass( descriptor.getType() ),
				descriptor.getType(),
				new AnnotationProxy( descriptor )
		);
	}
}
