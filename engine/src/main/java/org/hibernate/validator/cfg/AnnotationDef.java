/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all annotation definition types.
 * <p>
 * Note that any protected member in this type and its subtypes are not part of the public API and are only meant for internal use.
 *
 * @param <C> The type of a concrete sub type. Following to the
 * "self referencing generic type" pattern each sub type has to be
 * parametrized with itself.
 * @param <A> The constraint annotation type represented by a concrete sub type.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public abstract class AnnotationDef<C extends AnnotationDef<C, A>, A extends Annotation> {

	private static final Log LOG = LoggerFactory.make();

	// Note on visibility of members: These members are intentionally made
	// protected and published by a sub-class for internal use. There aren't
	// public getters as they would pollute the fluent definition API.

	/**
	 * The constraint annotation type of this definition.
	 */
	protected final Class<A> annotationType;

	/**
	 * A map with the annotation parameters of this definition. Contains only parameters
	 * of non annotation types. Keys are property names of this definition's annotation
	 * type, values are annotation parameter values of the appropriate types.
	 */
	protected final Map<String, Object> parameters;

	/**
	 * A map with annotation parameters of this definition which are annotations
	 * on their own. Keys are property names of this definition's annotation
	 * type, values are annotation parameter values of the appropriate annotation
	 * types described via {@link AnnotationDef}. If an array of annotations is
	 * needed it should be represented via {@link java.util.List} of corresponding
	 * {@link AnnotationDef}s.
	 */
	protected final Map<String, List<AnnotationDef<?, ?>>> annotationsAsParameters;

	/**
	 * A map of annotation types that are added to {@link AnnotationDef#annotationsAsParameters}.
	 * For the same key form {@link AnnotationDef#annotationsAsParameters} you'll get an annotation
	 * type represented by corresponding {@link AnnotationDef}
	 */
	private final Map<String, Class<?>> annotationsAsParametersTypes;

	protected AnnotationDef(Class<A> annotationType) {
		this.annotationType = annotationType;
		this.parameters = new HashMap<>();
		this.annotationsAsParameters = new HashMap<>();
		this.annotationsAsParametersTypes = new HashMap<>();
	}

	protected AnnotationDef(AnnotationDef<?, A> original) {
		this.annotationType = original.annotationType;
		this.parameters = original.parameters;
		this.annotationsAsParameters = original.annotationsAsParameters;
		this.annotationsAsParametersTypes = original.annotationsAsParametersTypes;
	}

	@SuppressWarnings("unchecked")
	private C getThis() {
		return (C) this;
	}

	protected C addParameter(String key, Object value) {
		parameters.put( key, value );
		return getThis();
	}

	protected C addAnnotationAsParameter(String key, AnnotationDef<?, ?> value) {
		annotationsAsParameters.compute( key, ( k, oldValue ) -> {
			if ( oldValue == null ) {
				return Arrays.asList( value );
			}
			else {
				List<AnnotationDef<?, ?>> resultingList = CollectionHelper.newArrayList( oldValue );
				resultingList.add( value );
				return resultingList;
			}
		} );
		annotationsAsParametersTypes.putIfAbsent( key, value.annotationType );
		return getThis();
	}

	protected A createAnnotationProxy() {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<>( annotationType );
		for ( Map.Entry<String, Object> parameter : parameters.entrySet() ) {
			annotationDescriptor.setValue( parameter.getKey(), parameter.getValue() );
		}

		for ( Map.Entry<String, List<AnnotationDef<?, ?>>> annotationAsParameter : annotationsAsParameters.entrySet() ) {
			annotationDescriptor.setValue(
					annotationAsParameter.getKey(),
							toAnnotationParameterArray(
									annotationAsParameter.getValue(),
									annotationsAsParametersTypes.get( annotationAsParameter.getKey() )
							)
			);
		}

		try {
			return AnnotationFactory.create( annotationDescriptor );
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T[] toAnnotationParameterArray(List<AnnotationDef<?, ?>> list, Class<T> aClass) {
		return list.stream()
				.map( AnnotationDef::createAnnotationProxy )
				.toArray( n -> (T[]) Array.newInstance( aClass, n ) );
	}

	@SuppressWarnings("unchecked")
	protected <T> T toAnnotationParameter(AnnotationDef<?, ?> annotationDef, Class<T> aClass) {
		return (T) annotationDef.createAnnotationProxy();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( this.getClass().getName() );
		sb.append( ", annotationType=" ).append( StringHelper.toShortString( annotationType ) );
		sb.append( ", parameters=" ).append( parameters );
		sb.append( '}' );
		return sb.toString();
	}
}
