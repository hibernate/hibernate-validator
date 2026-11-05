package org.hibernate.validator.internal.metadata.descriptor;

import java.util.List;
import java.util.Set;

import jakarta.validation.metadata.CrossParameterDescriptor;

/**
 * Describes cross-parameters.
 *
 * @author Gunnar Morling
 */
public class CrossParameterDescriptorImpl extends ElementDescriptorImpl implements CrossParameterDescriptor {

	public CrossParameterDescriptorImpl(Set<ConstraintDescriptorImpl<?>> constraintDescriptors, boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		super( Object[].class, constraintDescriptors, defaultGroupSequenceRedefined, defaultGroupSequence );
	}
}
