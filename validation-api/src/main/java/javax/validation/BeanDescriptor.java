package javax.validation;

import java.util.Set;

/**
 * Describe a constrained Java Bean and the constraints associated to it.
 * 
 * @author Emmanuel Bernard
 */
public interface BeanDescriptor extends ElementDescriptor {
	/**
	 * return true if at least one constraint declaration is present for the given bean
	 * or if one property is marked for validation cascade
	 */
	boolean hasConstraints();

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
	 * return the property names having at least a constraint defined
	 */
	Set<String> getPropertiesWithConstraints();
}
