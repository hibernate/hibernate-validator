package javax.validation;

import java.util.Set;

/**
 * Describe a constrained Java Bean and the constraints associated to it.
 * 
 * @author Emmanuel Bernard
 */
public interface BeanDescriptor extends ElementDescriptor {
	/**
	 * Returns true if the bean involves validation:
	 *  - a constraint is hosted on the bean itself
	 *  - a constraint is hosted on one of the bean properties
	 *  - or a bean property is marked for cascade (@Valid)
	 *
	 * @return true if the bean nvolves validation
	 *
	 */
	boolean isBeanConstrained();

	/**
	 * Return the property level constraints for a given propertyName
	 * or null if either the property does not exist or has no constraint
	 * The returned object (and associated objects including ConstraintDescriptors)
     * are immutable.
	 *
	 * @param propertyName property evaludated
	 */
	PropertyDescriptor getConstraintsForProperty(String propertyName);

	/**
	 * return the property names having at least a constraint defined or marked
	 * as cascaded (@Valid)
	 */
	Set<String> getConstrainedProperties();
}
