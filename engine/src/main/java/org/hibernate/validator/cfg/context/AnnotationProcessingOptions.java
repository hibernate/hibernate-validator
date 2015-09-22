/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to configure how existing annotation should be
 * treated.
 *
 * @author Hardy Ferentschik
 */
public interface AnnotationProcessingOptions<C extends AnnotationProcessingOptions<C>> {

	/**
	 * Specifies that annotations specified on the configured type or property should be ignored.
	 *
	 * @return Returns itself for method chaining.
	 * @deprecated Use {@link AnnotationIgnoreOptions#ignoreAnnotations(boolean)} instead.
	 */
	@Deprecated
	C ignoreAnnotations();
}
