/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.nodenameprovider;

/**
 * This interface is used to resolve the name of a property node when creating the property path.
 *
 * @author Damir Alibegovic
 * @since 6.1.0
 */
public interface PropertyNodeNameProvider {
	/**
	 * Returns resolved name of the property.
	 * <br><br>
	 * Depending on the subtype of the {@link Property},
	 * different strategy for name resolution should be applied, defaulting to {@link Property#getName()}. For example:
	 * <br>
	 *
	 * <pre>
	 * if (property instanceof {@link JavaBeanProperty}) {
	 *     // for example, read the name from the annotation element on a property
	 * } else {
	 *     return property.getName();
	 * }
	 * </pre>
	 *
	 * @param property who's name needs to be resolved
	 *
	 * @return String representing the resolved name
	 */
	String getName(Property property);
}
