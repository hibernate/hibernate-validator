/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 */
public class ConstrainedParameter extends AbstractConstrainedElement {

	private final Type type;
	private final String name;
	private final int index;
	private final Set<MetaConstraint<?>> typeArgumentsConstraints;

	public ConstrainedParameter(ConfigurationSource source,
								ConstraintLocation location,
								Type type,
								int index,
								String name) {
		this(
				source,
				location,
				type,
				index,
				name,
				Collections.<MetaConstraint<?>>emptySet(),
				Collections.<MetaConstraint<?>>emptySet(),
				Collections.<Class<?>, Class<?>>emptyMap(),
				false,
				UnwrapMode.AUTOMATIC
		);
	}

	/**
	 * Creates a new parameter meta data object.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented method parameter.
	 * @param type the parameter type
	 * @param index the index of the parameter
	 * @param name The name of the represented parameter.
	 * @param constraints The constraints of the represented method parameter, if
	 * any.
	 * @param typeArgumentsConstraints Type arguments constraints, if any.
	 * @param groupConversions The group conversions of the represented method parameter, if any.
	 * @param isCascading Whether a cascaded validation of the represented method
	 * parameter shall be performed or not.
	 * @param unwrapMode Determines how the value of the parameter must be handled in regards to
	 * unwrapping prior to validation.
	 */
	public ConstrainedParameter(ConfigurationSource source,
								ConstraintLocation location,
								Type type,
								int index,
								String name,
								Set<MetaConstraint<?>> constraints,
								Set<MetaConstraint<?>> typeArgumentsConstraints,
								Map<Class<?>, Class<?>> groupConversions,
								boolean isCascading,
								UnwrapMode unwrapMode) {
		super(
				source,
				ConstrainedElementKind.PARAMETER,
				location,
				constraints,
				groupConversions,
				isCascading,
				unwrapMode
		);

		this.type = type;
		this.name = name;
		this.index = index;
		this.typeArgumentsConstraints = typeArgumentsConstraints != null ? Collections.unmodifiableSet(
				typeArgumentsConstraints
		) : Collections.<MetaConstraint<?>>emptySet();
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public Set<MetaConstraint<?>> getTypeArgumentsConstraints() {
		return this.typeArgumentsConstraints;
	}

	/**
	 * Creates a new constrained parameter object by merging this and the given
	 * other parameter.
	 *
	 * @param other The parameter to merge.
	 *
	 * @return A merged parameter.
	 */
	public ConstrainedParameter merge(ConstrainedParameter other) {
		ConfigurationSource mergedSource = ConfigurationSource.max( source, other.source );

		String mergedName;
		if ( source.getPriority() > other.source.getPriority() ) {
			mergedName = name;
		}
		else {
			mergedName = other.name;
		}

		// TODO - Is this the right way of handling the merge of unwrapMode? (HF)
		UnwrapMode mergedUnwrapMode;
		if ( source.getPriority() > other.source.getPriority() ) {
			mergedUnwrapMode = unwrapMode;
		}
		else {
			mergedUnwrapMode = other.unwrapMode;
		}

		Set<MetaConstraint<?>> mergedConstraints = newHashSet( constraints );
		mergedConstraints.addAll( other.constraints );

		Set<MetaConstraint<?>> mergedTypeArgumentsConstraints = newHashSet( typeArgumentsConstraints );
		mergedTypeArgumentsConstraints.addAll( other.typeArgumentsConstraints );

		Map<Class<?>, Class<?>> mergedGroupConversions = newHashMap( groupConversions );
		mergedGroupConversions.putAll( other.groupConversions );

		return new ConstrainedParameter(
				mergedSource,
				getLocation(),
				type,
				index,
				mergedName,
				mergedConstraints,
				mergedTypeArgumentsConstraints,
				mergedGroupConversions,
				isCascading || other.isCascading,
				mergedUnwrapMode
		);
	}

	@Override
	public String toString() {
		//display short annotation type names
		StringBuilder sb = new StringBuilder();

		for ( MetaConstraint<?> oneConstraint : getConstraints() ) {
			sb.append( oneConstraint.getDescriptor().getAnnotation().annotationType().getSimpleName() );
			sb.append( ", " );
		}

		String constraintsAsString = sb.length() > 0 ? sb.substring( 0, sb.length() - 2 ) : sb.toString();

		return "ParameterMetaData [location=" + getLocation() + "], name=" + name + "], constraints=["
				+ constraintsAsString + "], isCascading=" + isCascading() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
		result = prime * result + ( ( getLocation().getMember() == null ) ? 0 : getLocation().getMember().hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ConstrainedParameter other = (ConstrainedParameter) obj;
		if ( index != other.index ) {
			return false;
		}
		if ( getLocation().getMember() == null ) {
			if ( other.getLocation().getMember() != null ) {
				return false;
			}
		}
		else if ( !getLocation().getMember().equals( other.getLocation().getMember() ) ) {
			return false;
		}
		return true;
	}
}
