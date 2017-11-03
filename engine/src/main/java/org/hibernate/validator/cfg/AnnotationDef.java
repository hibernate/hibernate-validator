/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
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
 * @author Guillaume Smet
 */
public abstract class AnnotationDef<C extends AnnotationDef<C, A>, A extends Annotation> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	// Note on visibility of members: These members are intentionally made
	// protected and published by a sub-class for internal use. There aren't
	// public getters as they would pollute the fluent definition API.

	/**
	 * The annotation descriptor builder.
	 */
	private final AnnotationDescriptor.Builder<A> annotationDescriptorBuilder;

	/**
	 * A map with annotation parameters of this definition which are annotations
	 * on their own. Keys are property names of this definition's annotation
	 * type, values are annotation parameter values of the appropriate annotation
	 * types described via {@link AnnotationDef}. If an array of annotations is
	 * needed it should be represented via {@link java.util.List} of corresponding
	 * {@link AnnotationDef}s.
	 */
	private final Map<String, List<AnnotationDef<?, ?>>> annotationsAsParameters;

	/**
	 * A map of annotation types that are added to {@link AnnotationDef#annotationsAsParameters}.
	 * For the same key form {@link AnnotationDef#annotationsAsParameters} you'll get an annotation
	 * type represented by corresponding {@link AnnotationDef}
	 */
	private final Map<String, Class<?>> annotationsAsParametersTypes;

	protected AnnotationDef(Class<A> annotationType) {
		this.annotationDescriptorBuilder = new AnnotationDescriptor.Builder<>( annotationType );
		this.annotationsAsParameters = new HashMap<>();
		this.annotationsAsParametersTypes = new HashMap<>();
	}

	protected AnnotationDef(AnnotationDef<?, A> original) {
		this.annotationDescriptorBuilder = original.annotationDescriptorBuilder;
		this.annotationsAsParameters = original.annotationsAsParameters;
		this.annotationsAsParametersTypes = original.annotationsAsParametersTypes;
	}

	@SuppressWarnings("unchecked")
	private C getThis() {
		return (C) this;
	}

	protected C addParameter(String key, Object value) {
		annotationDescriptorBuilder.setAttribute( key, value );
		return getThis();
	}

	protected C addAnnotationAsParameter(String key, AnnotationDef<?, ?> value) {
		annotationsAsParameters.compute( key, ( k, oldValue ) -> {
			if ( oldValue == null ) {
				return Collections.singletonList( value );
			}
			else {
				List<AnnotationDef<?, ?>> resultingList = CollectionHelper.newArrayList( oldValue );
				resultingList.add( value );
				return resultingList;
			}
		} );
		annotationsAsParametersTypes.putIfAbsent( key, value.annotationDescriptorBuilder.getType() );
		return getThis();
	}

	private AnnotationDescriptor<A> createAnnotationDescriptor() {
		for ( Map.Entry<String, List<AnnotationDef<?, ?>>> annotationAsParameter : annotationsAsParameters.entrySet() ) {
			annotationDescriptorBuilder.setAttribute(
					annotationAsParameter.getKey(),
							toAnnotationParameterArray(
									annotationAsParameter.getValue(),
									annotationsAsParametersTypes.get( annotationAsParameter.getKey() )
							)
			);
		}

		try {
			return annotationDescriptorBuilder.build();
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}
	}

	private A createAnnotationProxy() {
		return createAnnotationDescriptor().getAnnotation();
	}

	@SuppressWarnings("unchecked")
	private <T> T[] toAnnotationParameterArray(List<AnnotationDef<?, ?>> list, Class<T> aClass) {
		return list.stream()
				.map( AnnotationDef::createAnnotationProxy )
				.toArray( n -> (T[]) Array.newInstance( aClass, n ) );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( this.getClass().getSimpleName() );
		sb.append( '{' );
		sb.append( annotationDescriptorBuilder );
		sb.append( '}' );
		return sb.toString();
	}
}
