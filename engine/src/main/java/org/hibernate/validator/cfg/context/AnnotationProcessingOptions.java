/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * @author Hardy Ferentschik
 * @deprecated Since 6.0. Planned for removal. Use {@link AnnotationIgnoreOptions#ignoreAnnotations(boolean)} instead.
 */
@Deprecated
public interface AnnotationProcessingOptions<C extends AnnotationProcessingOptions<C>> {

	/**
	 * @deprecated Since 5.2. Planned for removal. Use {@link AnnotationIgnoreOptions#ignoreAnnotations(boolean)} instead.
	 */
	@Deprecated
	C ignoreAnnotations();
}
