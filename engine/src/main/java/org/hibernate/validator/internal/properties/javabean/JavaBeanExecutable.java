/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Signature;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class JavaBeanExecutable<T extends Executable> implements Callable, JavaBeanAnnotatedConstrainable {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	protected final T executable;
	private final Type typeForValidatorResolution;
	private final boolean hasReturnValue;
	private final Type type;
	private final List<JavaBeanParameter> parameters;

	JavaBeanExecutable(T executable, boolean hasReturnValue) {
		this.executable = executable;
		this.type = ReflectionHelper.typeOf( executable );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
		this.hasReturnValue = hasReturnValue;
		this.parameters = getParameters( executable );
	}

	@Override
	public boolean hasReturnValue() {
		return hasReturnValue;
	}

	@Override
	public boolean hasParameters() {
		return !parameters.isEmpty();
	}

	@Override
	public String getName() {
		return executable.getName();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex) {
		return parameterNameProvider.getParameterNames( executable ).get( parameterIndex );
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate( executable.getModifiers() );
	}

	@Override
	public Signature getSignature() {
		return ExecutableHelper.getSignature( executable );
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return executable.getDeclaredAnnotations();
	}

	@Override
	public boolean overrides(ExecutableHelper executableHelper, Callable superTypeMethod) {
		return executableHelper.overrides( ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable<?>) superTypeMethod ).executable ) );
	}

	@Override
	public boolean isResolvedToSameMethodInHierarchy(ExecutableHelper executableHelper, Class<?> mainSubType, Callable superTypeMethod) {
		return executableHelper.isResolvedToSameMethodInHierarchy( mainSubType, ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable<?>) superTypeMethod ).executable ) );
	}

	@Override
	public Type getGenericType() {
		return ReflectionHelper.typeOf( executable );
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return executable.getAnnotatedReturnType();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return executable.getAnnotation( annotationClass );
	}

	public List<JavaBeanParameter> getParameters() {
		return parameters;
	}

	@Override
	public Type getParameterGenericType(int index) {
		return parameters.get( index ).getGenericType();
	}

	@Override
	public int getParameterCount() {
		return parameters.size();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return executable.getParameterTypes();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeanExecutable<?> that = (JavaBeanExecutable<?>) o;

		if ( this.hasReturnValue != that.hasReturnValue ) {
			return false;
		}
		if ( !this.executable.equals( that.executable ) ) {
			return false;
		}
		if ( !this.typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}
		return this.type.equals( that.type );
	}

	@Override
	public int hashCode() {
		int result = this.executable.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		result = 31 * result + ( this.hasReturnValue ? 1 : 0 );
		result = 31 * result + this.type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return ExecutableHelper.getExecutableAsString(
				getDeclaringClass().getSimpleName() + "#" + executable.getName(),
				executable.getParameterTypes()
		);
	}

	private static List<JavaBeanParameter> getParameters(Executable executable) {
		if ( executable.getParameterCount() == 0 ) {
			return Collections.emptyList();
		}

		List<JavaBeanParameter> parameters = new ArrayList<>( executable.getParameterCount() );

		Parameter[] parameterArray = executable.getParameters();
		Class<?>[] parameterTypes = executable.getParameterTypes();
		// getGenericParameterTypes() does not include either the synthetic or the implicit parameters so we need to be
		// extra careful
		Type[] genericParameterTypes = executable.getGenericParameterTypes();

		if ( parameterTypes.length == genericParameterTypes.length ) {
			// this is the simple case where both arrays are consistent
			// we could do without it but at some point, the behavior of getGenericParameterTypes() might be changed in
			// Java and we'd better be ready.
			for ( int i = 0; i < parameterArray.length; i++ ) {
				parameters.add( new JavaBeanParameter( i, parameterArray[i], parameterTypes[i], getErasedTypeIfTypeVariable( genericParameterTypes[i] ) ) );
			}
		}
		else {
			// in this case, we have synthetic or implicit parameters

			// do we have the information about which parameter is synthetic/implicit?
			// (this metadata is only included when classes are compiled with the '-parameters' flag)
			boolean hasParameterModifierInfo = isAnyParameterCarryingMetadata( parameterArray );

			if ( ! hasParameterModifierInfo ) {
				LOG.missingParameterMetadataWithSyntheticOrImplicitParameters( executable );
			}

			int explicitlyDeclaredParameterIndex = 0;

			for ( int i = 0; i < parameterArray.length; i++ ) {
				if ( explicitlyDeclaredParameterIndex < genericParameterTypes.length // we might already be out of the bounds of generic params array
						&& isExplicit( parameterArray[i] )
						&& parameterTypesMatch( parameterTypes[i], genericParameterTypes[explicitlyDeclaredParameterIndex] ) ) {
					// in this case we have a parameter that is present and matches ("most likely") to the one in the generic parameter types list
					parameters.add( new JavaBeanParameter( i, parameterArray[i], parameterTypes[i],
							getErasedTypeIfTypeVariable( genericParameterTypes[explicitlyDeclaredParameterIndex] ) ) );
					explicitlyDeclaredParameterIndex++;
				}
				else {
					// in this case, the parameter is not present in genericParameterTypes, or the types doesn't match
					parameters.add( new JavaBeanParameter( i, parameterArray[i], parameterTypes[i], parameterTypes[i] ) );
				}
			}
		}

		return CollectionHelper.toImmutableList( parameters );
	}

	private static boolean isAnyParameterCarryingMetadata(Parameter[] parameterArray) {
		for ( Parameter parameter : parameterArray ) {
			if ( parameter.isSynthetic() || parameter.isImplicit() ) {
				return true;
			}
		}
		return false;
	}

	private static boolean parameterTypesMatch(Class<?> paramType, Type genericParamType) {
		return TypeHelper.getErasedType( genericParamType ).equals( paramType );
	}

	private static boolean isExplicit(Parameter parameter) {
		return !parameter.isSynthetic() && !parameter.isImplicit();
	}

	private static Type getErasedTypeIfTypeVariable(Type genericType) {
		if ( genericType instanceof TypeVariable ) {
			return TypeHelper.getErasedType( genericType );
		}

		return genericType;
	}
}
