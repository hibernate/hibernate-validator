/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.executable.ExecutableType;

import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Wrapper class for the bootstrap parameters defined in <i>validation.xml</i>
 *
 * @author Hardy Ferentschik
 */
public class BootstrapConfigurationImpl implements BootstrapConfiguration {

	/**
	 * The executable types validated by default.
	 */
	@Immutable
	private static final Set<ExecutableType> DEFAULT_VALIDATED_EXECUTABLE_TYPES =
			Collections.unmodifiableSet(
					EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS )
			);

	/**
	 * The validated executable types, when ALL is given.
	 */
	@Immutable
	private static final Set<ExecutableType> ALL_VALIDATED_EXECUTABLE_TYPES =
			Collections.unmodifiableSet(
					EnumSet.complementOf(
							EnumSet.of(
									ExecutableType.ALL,
									ExecutableType.NONE,
									ExecutableType.IMPLICIT
							)
					)
			);

	private static final BootstrapConfiguration DEFAULT_BOOTSTRAP_CONFIGURATION = new BootstrapConfigurationImpl();

	private final String defaultProviderClassName;
	private final String constraintValidatorFactoryClassName;
	private final String messageInterpolatorClassName;
	private final String traversableResolverClassName;
	private final String parameterNameProviderClassName;
	private final String clockProviderClassName;
	private final String scriptEvaluatorFactoryClassName;
	private final Set<String> valueExtractorClassNames;
	private final Set<String> constraintMappingResourcePaths;
	private final Map<String, String> properties;
	private final Set<ExecutableType> validatedExecutableTypes;
	private final boolean isExecutableValidationEnabled;

	private BootstrapConfigurationImpl() {
		this.defaultProviderClassName = null;
		this.constraintValidatorFactoryClassName = null;
		this.messageInterpolatorClassName = null;
		this.traversableResolverClassName = null;
		this.parameterNameProviderClassName = null;
		this.clockProviderClassName = null;
		this.scriptEvaluatorFactoryClassName = null;
		this.valueExtractorClassNames = new HashSet<>();
		this.validatedExecutableTypes = DEFAULT_VALIDATED_EXECUTABLE_TYPES;
		this.isExecutableValidationEnabled = true;
		this.constraintMappingResourcePaths = new HashSet<>();
		this.properties = new HashMap<>();
	}

	public BootstrapConfigurationImpl(String defaultProviderClassName,
									  String constraintValidatorFactoryClassName,
									  String messageInterpolatorClassName,
									  String traversableResolverClassName,
									  String parameterNameProviderClassName,
									  String clockProviderClassName,
									  String scriptEvaluatorFactoryClassName,
									  Set<String> valueExtractorClassNames,
									  EnumSet<ExecutableType> validatedExecutableTypes,
									  boolean isExecutableValidationEnabled,
									  Set<String> constraintMappingResourcePaths,
									  Map<String, String> properties) {
		this.defaultProviderClassName = defaultProviderClassName;
		this.constraintValidatorFactoryClassName = constraintValidatorFactoryClassName;
		this.messageInterpolatorClassName = messageInterpolatorClassName;
		this.traversableResolverClassName = traversableResolverClassName;
		this.parameterNameProviderClassName = parameterNameProviderClassName;
		this.clockProviderClassName = clockProviderClassName;
		this.scriptEvaluatorFactoryClassName = scriptEvaluatorFactoryClassName;
		this.valueExtractorClassNames = valueExtractorClassNames;
		this.validatedExecutableTypes = prepareValidatedExecutableTypes( validatedExecutableTypes );
		this.isExecutableValidationEnabled = isExecutableValidationEnabled;
		this.constraintMappingResourcePaths = constraintMappingResourcePaths;
		this.properties = properties;
	}

	public static BootstrapConfiguration getDefaultBootstrapConfiguration() {
		return DEFAULT_BOOTSTRAP_CONFIGURATION;
	}

	private Set<ExecutableType> prepareValidatedExecutableTypes(EnumSet<ExecutableType> validatedExecutableTypes) {
		if ( validatedExecutableTypes == null ) {
			return DEFAULT_VALIDATED_EXECUTABLE_TYPES;
		}
		if ( validatedExecutableTypes.contains( ExecutableType.ALL ) ) {
			return ALL_VALIDATED_EXECUTABLE_TYPES;
		}
		else if ( validatedExecutableTypes.contains( ExecutableType.NONE ) ) {
			return EnumSet.noneOf( ExecutableType.class );
		}
		else {
			return validatedExecutableTypes;
		}
	}

	@Override
	public String getDefaultProviderClassName() {
		return defaultProviderClassName;
	}

	@Override
	public String getConstraintValidatorFactoryClassName() {
		return constraintValidatorFactoryClassName;
	}

	@Override
	public String getMessageInterpolatorClassName() {
		return messageInterpolatorClassName;
	}

	@Override
	public String getTraversableResolverClassName() {
		return traversableResolverClassName;
	}

	@Override
	public String getParameterNameProviderClassName() {
		return parameterNameProviderClassName;
	}

	@Override
	public String getClockProviderClassName() {
		return clockProviderClassName;
	}

	public String getScriptEvaluatorFactoryClassName() {
		return scriptEvaluatorFactoryClassName;
	}

	@Override
	public Set<String> getValueExtractorClassNames() {
		return new HashSet<>( valueExtractorClassNames );
	}

	@Override
	public Set<String> getConstraintMappingResourcePaths() {
		return new HashSet<>( constraintMappingResourcePaths );
	}

	@Override
	public boolean isExecutableValidationEnabled() {
		return isExecutableValidationEnabled;
	}

	@Override
	public Set<ExecutableType> getDefaultValidatedExecutableTypes() {
		return new HashSet<>( validatedExecutableTypes );
	}

	@Override
	public Map<String, String> getProperties() {
		return new HashMap<>( properties );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BootstrapConfigurationImpl" );
		sb.append( "{defaultProviderClassName='" ).append( defaultProviderClassName ).append( '\'' );
		sb.append( ", constraintValidatorFactoryClassName='" )
				.append( constraintValidatorFactoryClassName )
				.append( '\'' );
		sb.append( ", messageInterpolatorClassName='" ).append( messageInterpolatorClassName ).append( '\'' );
		sb.append( ", traversableResolverClassName='" ).append( traversableResolverClassName ).append( '\'' );
		sb.append( ", parameterNameProviderClassName='" ).append( parameterNameProviderClassName ).append( '\'' );
		sb.append( ", clockProviderClassName='" ).append( clockProviderClassName ).append( '\'' );
		sb.append( ", scriptEvaluatorFactoryClassName='" ).append( scriptEvaluatorFactoryClassName ).append( '\'' );
		sb.append( ", validatedExecutableTypes='" ).append( validatedExecutableTypes ).append( '\'' );
		sb.append( ", constraintMappingResourcePaths=" ).append( constraintMappingResourcePaths ).append( '\'' );
		sb.append( ", properties=" ).append( properties );
		sb.append( '}' );
		return sb.toString();
	}
}
