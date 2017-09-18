/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * An aggregated view of the constraint related meta data for a given method or
 * constructors and in (case of methods) all the methods in the inheritance
 * hierarchy which it overrides or implements.
 * <p>
 * Instances are retrieved by creating a {@link Builder} and adding all required
 * {@link ConstrainedExecutable} objects to it. Instances are read-only after
 * creation.
 * </p>
 * <p>
 * Identity is solely based on the method's name and parameter types, hence sets
 * and similar collections of this type may only be created in the scope of one
 * Java type.
 * </p>
 *
 * @author Gunnar Morling
 */
public class ExecutableMetaData extends AbstractConstraintMetaData {

	private final Class<?>[] parameterTypes;
	@Immutable
	private final List<ParameterMetaData> parameterMetaDataList;
	@Immutable
	private final Set<MetaConstraint<?>> crossParameterConstraints;
	private final boolean isGetter;

	/**
	 * Set of signatures for storing this object in maps etc. Will only contain more than one entry in case this method
	 * overrides a super type method with generic parameters, in which case the signature of the super-type and the
	 * sub-type method will differ.
	 */
	private final Set<String> signatures;

	private final ReturnValueMetaData returnValueMetaData;
	private final ElementKind kind;

	private ExecutableMetaData(
			String name,
			Type returnType,
			Class<?>[] parameterTypes,
			ElementKind kind,
			Set<String> signatures,
			Set<MetaConstraint<?>> returnValueConstraints,
			Set<MetaConstraint<?>> returnValueContainerElementConstraints,
			List<ParameterMetaData> parameterMetaData,
			Set<MetaConstraint<?>> crossParameterConstraints,
			CascadingMetaData cascadingMetaData,
			boolean isConstrained,
			boolean isGetter) {
		super(
				name,
				returnType,
				returnValueConstraints,
				returnValueContainerElementConstraints,
				cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements(),
				isConstrained
		);

		this.parameterTypes = parameterTypes;
		this.parameterMetaDataList = CollectionHelper.toImmutableList( parameterMetaData );
		this.crossParameterConstraints = CollectionHelper.toImmutableSet( crossParameterConstraints );
		this.signatures = signatures;
		this.returnValueMetaData = new ReturnValueMetaData(
				returnType,
				returnValueConstraints,
				returnValueContainerElementConstraints,
				cascadingMetaData
		);
		this.isGetter = isGetter;
		this.kind = kind;
	}

	/**
	 * Returns meta data for the specified parameter of the represented executable.
	 *
	 * @param parameterIndex the index of the parameter
	 *
	 * @return Meta data for the specified parameter. Will never be {@code null}.
	 */
	public ParameterMetaData getParameterMetaData(int parameterIndex) {
		return parameterMetaDataList.get( parameterIndex );
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Returns the signature(s) of the method represented by this meta data object, based on the represented
	 * executable's name and its parameter types.
	 *
	 * @return The signatures of this meta data object. Will only contain more than one element in case the represented
	 * method represents a sub-type method overriding a super-type method using a generic type parameter in its
	 * parameters.
	 */
	public Set<String> getSignatures() {
		return signatures;
	}

	/**
	 * Returns the cross-parameter constraints declared for the represented
	 * method or constructor.
	 *
	 * @return the cross-parameter constraints declared for the represented
	 * method or constructor. May be empty but will never be
	 * {@code null}.
	 */
	public Set<MetaConstraint<?>> getCrossParameterConstraints() {
		return crossParameterConstraints;
	}

	public ValidatableParametersMetaData getValidatableParametersMetaData() {
		return new ValidatableParametersMetaData( parameterMetaDataList );
	}

	public ReturnValueMetaData getReturnValueMetaData() {
		return returnValueMetaData;
	}

	@Override
	public ExecutableDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ExecutableDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getCrossParameterConstraints() ),
				returnValueMetaData.asDescriptor(
						defaultGroupSequenceRedefined,
						defaultGroupSequence
				),
				parametersAsDescriptors( defaultGroupSequenceRedefined, defaultGroupSequence ),
				defaultGroupSequenceRedefined,
				isGetter,
				defaultGroupSequence
		);
	}

	private List<ParameterDescriptor> parametersAsDescriptors(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		List<ParameterDescriptor> parameterDescriptorList = newArrayList();

		for ( ParameterMetaData parameterMetaData : parameterMetaDataList ) {
			parameterDescriptorList.add(
					parameterMetaData.asDescriptor(
							defaultGroupSequenceRedefined,
							defaultGroupSequence
					)
			);
		}

		return parameterDescriptorList;
	}

	@Override
	public ElementKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		StringBuilder parameterBuilder = new StringBuilder();

		for ( Class<?> oneParameterType : getParameterTypes() ) {
			parameterBuilder.append( oneParameterType.getSimpleName() );
			parameterBuilder.append( ", " );
		}

		String parameters =
				parameterBuilder.length() > 0 ?
						parameterBuilder.substring( 0, parameterBuilder.length() - 2 ) :
						parameterBuilder.toString();

		return "ExecutableMetaData [executable=" + getType() + " " + getName() + "(" + parameters + "), isCascading=" + isCascading() + ", isConstrained="
				+ isConstrained() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode( parameterTypes );
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
		ExecutableMetaData other = (ExecutableMetaData) obj;
		if ( !Arrays.equals( parameterTypes, other.parameterTypes ) ) {
			return false;
		}
		return true;
	}

	/**
	 * Creates new {@link ExecutableMetaData} instances.
	 *
	 * @author Gunnar Morling
	 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
	 */
	public static class Builder extends MetaDataBuilder {
		private final Set<String> signatures = newHashSet();

		/**
		 * Either CONSTRUCTOR or METHOD.
		 */
		private final ConstrainedElement.ConstrainedElementKind kind;
		private final Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		private Executable executable;
		private final boolean isGetterMethod;
		private final Set<MetaConstraint<?>> crossParameterConstraints = newHashSet();
		private final Set<MethodConfigurationRule> rules;
		private boolean isConstrained = false;
		private CascadingMetaDataBuilder cascadingMetaDataBuilder;

		/**
		 * Holds a merged representation of the configurations for one method
		 * from the hierarchy contributed by the different meta data providers.
		 * Used to check for violations of the Liskov substitution principle by
		 * e.g. adding parameter constraints in sub type methods.
		 */
		private final Map<Class<?>, ConstrainedExecutable> executablesByDeclaringType = newHashMap();

		private final ExecutableHelper executableHelper;

		private final ExecutableParameterNameProvider parameterNameProvider;

		public Builder(
				Class<?> beanClass,
				ConstrainedExecutable constrainedExecutable,
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager,
				ExecutableParameterNameProvider parameterNameProvider,
				MethodValidationConfiguration methodValidationConfiguration) {
			super( beanClass, constraintHelper, typeResolutionHelper, executableHelper, valueExtractorManager );

			this.executableHelper = executableHelper;
			this.parameterNameProvider = parameterNameProvider;
			this.kind = constrainedExecutable.getKind();
			this.executable = constrainedExecutable.getExecutable();
			this.rules = methodValidationConfiguration.getConfiguredRuleSet();
			this.isGetterMethod = constrainedExecutable.isGetterMethod();

			add( constrainedExecutable );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( kind != constrainedElement.getKind() ) {
				return false;
			}

			Executable candidate = ( (ConstrainedExecutable) constrainedElement ).getExecutable();

			//are the locations equal (created by different builders) or
			//does one of the executables override the other one?
			return executable.equals( candidate ) ||
					overrides( executable, candidate ) ||
					overrides( candidate, executable );
		}

		private boolean overrides(Executable first, Executable other) {
			if ( first instanceof Constructor || other instanceof Constructor ) {
				return false;
			}

			return executableHelper.overrides( (Method) first, (Method) other );
		}

		@Override
		public final void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );
			ConstrainedExecutable constrainedExecutable = (ConstrainedExecutable) constrainedElement;

			signatures.add( ExecutableHelper.getSignature( constrainedExecutable.getExecutable() ) );

			constrainedExecutables.add( constrainedExecutable );
			isConstrained = isConstrained || constrainedExecutable.isConstrained();
			crossParameterConstraints.addAll( constrainedExecutable.getCrossParameterConstraints() );
			if ( cascadingMetaDataBuilder == null ) {
				cascadingMetaDataBuilder = constrainedExecutable.getCascadingMetaDataBuilder();
			}
			else {
				cascadingMetaDataBuilder = cascadingMetaDataBuilder.merge( constrainedExecutable.getCascadingMetaDataBuilder() );
			}

			addToExecutablesByDeclaringType( constrainedExecutable );

			// keep the "lowest" executable in hierarchy to make sure any type parameters declared on super-types (and
			// used in overridden methods) are resolved for the specific sub-type we are interested in
			if ( executable != null && overrides(
					constrainedExecutable.getExecutable(),
					executable
			) ) {
				executable = constrainedExecutable.getExecutable();
			}
		}

		/**
		 * Merges the given executable with the metadata contributed by other
		 * providers for the same executable in the hierarchy.
		 *
		 * @param executable The executable to merge.
		 */
		private void addToExecutablesByDeclaringType(ConstrainedExecutable executable) {
			Class<?> beanClass = executable.getExecutable().getDeclaringClass();
			ConstrainedExecutable mergedExecutable = executablesByDeclaringType.get( beanClass );

			if ( mergedExecutable != null ) {
				mergedExecutable = mergedExecutable.merge( executable );
			}
			else {
				mergedExecutable = executable;
			}

			executablesByDeclaringType.put( beanClass, mergedExecutable );
		}

		@Override
		public ExecutableMetaData build() {
			assertCorrectnessOfConfiguration();

			return new ExecutableMetaData(
					kind == ConstrainedElement.ConstrainedElementKind.CONSTRUCTOR ? executable.getDeclaringClass().getSimpleName() : executable.getName(),
					ReflectionHelper.typeOf( executable ),
					executable.getParameterTypes(),
					kind == ConstrainedElement.ConstrainedElementKind.CONSTRUCTOR ? ElementKind.CONSTRUCTOR : ElementKind.METHOD,
					kind == ConstrainedElement.ConstrainedElementKind.CONSTRUCTOR ? Collections.singleton( ExecutableHelper.getSignature( executable ) ) :
							CollectionHelper.toImmutableSet( signatures ),
					adaptOriginsAndImplicitGroups( getDirectConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() ),
					findParameterMetaData(),
					adaptOriginsAndImplicitGroups( crossParameterConstraints ),
					cascadingMetaDataBuilder.build( valueExtractorManager, executable ),
					isConstrained,
					isGetterMethod
			);
		}

		/**
		 * Finds the one executable from the underlying hierarchy with parameter
		 * constraints. If no executable in the hierarchy is parameter constrained,
		 * the parameter meta data from this builder's base executable is returned.
		 *
		 * @return The parameter meta data for this builder's executable.
		 */
		private List<ParameterMetaData> findParameterMetaData() {
			List<ParameterMetaData.Builder> parameterBuilders = null;

			for ( ConstrainedExecutable oneExecutable : constrainedExecutables ) {
				if ( parameterBuilders == null ) {
					parameterBuilders = newArrayList();

					for ( ConstrainedParameter oneParameter : oneExecutable.getAllParameterMetaData() ) {
						parameterBuilders.add(
								new ParameterMetaData.Builder(
										executable.getDeclaringClass(),
										oneParameter,
										constraintHelper,
										typeResolutionHelper,
										executableHelper,
										valueExtractorManager,
										parameterNameProvider
								)
						);
					}
				}
				else {
					int i = 0;
					for ( ConstrainedParameter oneParameter : oneExecutable.getAllParameterMetaData() ) {
						parameterBuilders.get( i ).add( oneParameter );
						i++;
					}
				}
			}

			List<ParameterMetaData> parameterMetaDatas = newArrayList();

			for ( ParameterMetaData.Builder oneBuilder : parameterBuilders ) {
				parameterMetaDatas.add( oneBuilder.build() );
			}

			return parameterMetaDatas;
		}

		/**
		 * <p>
		 * Checks the configuration of this method for correctness as per the
		 * rules outlined in the Bean Validation specification, section 4.5.5
		 * ("Method constraints in inheritance hierarchies").
		 * </p>
		 * <p>
		 * In particular, overriding methods in sub-types may not add parameter
		 * constraints and the return value of an overriding method may not be
		 * marked as cascaded if the return value is marked as cascaded already
		 * on the overridden method.
		 * </p>
		 *
		 * @throws javax.validation.ConstraintDeclarationException In case any of the rules mandated by the
		 * specification are violated.
		 */
		private void assertCorrectnessOfConfiguration() {
			for ( Entry<Class<?>, ConstrainedExecutable> entry : executablesByDeclaringType.entrySet() ) {
				for ( Entry<Class<?>, ConstrainedExecutable> otherEntry : executablesByDeclaringType.entrySet() ) {
					for ( MethodConfigurationRule rule : rules ) {
						rule.apply( entry.getValue(), otherEntry.getValue() );
					}
				}
			}
		}
	}
}
