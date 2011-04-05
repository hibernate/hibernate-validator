package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ConstraintsForProperty {

	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private final String property;
	private final ElementType elementType;

	public ConstraintsForProperty(Class<?> beanClass, String property, ElementType elementType, ConstraintMapping mapping) {
		this.beanClass = beanClass;
		this.mapping = mapping;
		this.property = property;
		this.elementType = elementType;
	}

	/**
	 * Adds a new constraint.
	 *
	 * @param definition The constraint definition class.
	 *
	 * @return A constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation, T extends ConstraintDef<T, A>> T constraint(Class<T> definition) {
		final Constructor<T> constructor = ReflectionHelper.getConstructor(
				definition, Class.class, String.class, ElementType.class, ConstraintMapping.class
		);

		final T constraintDefinition = ReflectionHelper.newConstructorInstance(
				constructor, beanClass, property, elementType, mapping
		);
		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	/**
	 * Adds a new constraint in a generic way.
	 * <p>
	 * The attributes of the constraint can later on be set by invoking
	 * {@link GenericConstraintDef#addParameter(String, Object)}.
	 * </p>
	 *
	 * @param <A> The annotation type of the constraint to add.
	 * @param definition The constraint to add.
	 *
	 * @return A generic constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation> GenericConstraintDef<A> genericConstraint(Class<A> definition) {
		final GenericConstraintDef<A> constraintDefinition = new GenericConstraintDef<A>(
				beanClass, definition, property, elementType, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}
}
