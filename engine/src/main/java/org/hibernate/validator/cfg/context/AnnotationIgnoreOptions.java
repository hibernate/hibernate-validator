/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to configure how existing annotation should be treated.
 *
 * @author Gunnar Morling
 */
public interface AnnotationIgnoreOptions<C extends AnnotationIgnoreOptions<C>> {

	/**
	 * Specifies whether annotations at the given element should be ignored or not, overriding any setting given for
	 * parent elements. E.g. the setting given for a method parameter overrides the setting given for the method
	 * declaring that parameter.
	 *
	 * @param ignoreAnnotations Whether to ignore annotation-based constraints or not.
	 * @return This context for method chaining.
	 */
	C ignoreAnnotations(boolean ignoreAnnotations);
}
