/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ConstrainedParameter extends AbstractConstrainedElement {

	private final Executable executable;
	private final Type type;
	private final String name;
	private final int index;

	public ConstrainedParameter(ConfigurationSource source,
								Executable executable,
								Type type,
								int index,
								String name) {
		this(
				source,
				executable,
				type,
				index,
				name,
				Collections.<MetaConstraint<?>>emptySet(),
				Collections.<MetaConstraint<?>>emptySet(),
				Collections.<Class<?>, Class<?>>emptyMap(),
				Collections.<TypeVariable<?>>emptyList()
		);
	}

	/**
	 * Creates a new parameter meta data object.
	 *
	 * @param source The source of meta data.
	 * @param  executable The executable of the represented method parameter.
	 * @param type the parameter type
	 * @param index the index of the parameter
	 * @param name The name of the represented parameter.
	 * @param constraints The constraints of the represented method parameter, if
	 * any.
	 * @param typeArgumentConstraints Type arguments constraints, if any.
	 * @param groupConversions The group conversions of the represented method parameter, if any.
	 * @param cascadingTypeParameters The type parameters marked for cascaded validation, if any.
	 */
	public ConstrainedParameter(ConfigurationSource source,
								Executable executable,
								Type type,
								int index,
								String name,
								Set<MetaConstraint<?>> constraints,
								Set<MetaConstraint<?>> typeArgumentConstraints,
								Map<Class<?>, Class<?>> groupConversions,
								List<TypeVariable<?>> cascadingTypeParameters) {
		super(
				source,
				ConstrainedElementKind.PARAMETER,
				constraints,
				typeArgumentConstraints,
				groupConversions,
				cascadingTypeParameters
		);

		this.executable = executable;
		this.type = type;
		this.name = name;
		this.index = index;
	}

	public Type getType() {
		return type;
	}

	public Executable getExecutable() {
		return executable;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
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

		Set<MetaConstraint<?>> mergedConstraints = newHashSet( constraints );
		mergedConstraints.addAll( other.constraints );

		Set<MetaConstraint<?>> mergedTypeArgumentConstraints = new HashSet<>( typeArgumentConstraints );
		mergedTypeArgumentConstraints.addAll( other.typeArgumentConstraints );

		Map<Class<?>, Class<?>> mergedGroupConversions = newHashMap( groupConversions );
		mergedGroupConversions.putAll( other.groupConversions );

		List<TypeVariable<?>> mergedCascadingTypeParameters = new ArrayList<>( cascadingTypeParameters );
		mergedCascadingTypeParameters.addAll( other.cascadingTypeParameters );

		return new ConstrainedParameter(
				mergedSource,
				executable,
				type,
				index,
				mergedName,
				mergedConstraints,
				mergedTypeArgumentConstraints,
				mergedGroupConversions,
				mergedCascadingTypeParameters
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

		return "ParameterMetaData [executable=" + executable + "], name=" + name + "], constraints=["
				+ constraintsAsString + "], isCascading=" + isCascading() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
		result = prime * result + ( ( executable == null ) ? 0 : executable.hashCode() );
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
		if ( executable == null ) {
			if ( other.executable != null ) {
				return false;
			}
		}
		else if ( !executable.equals( other.executable ) ) {
			return false;
		}
		return true;
	}
}
