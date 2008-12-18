// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.tck;

import java.util.Arrays;
import java.util.Collection;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import org.hibernate.tck.annotations.SpecAssertion;
import org.hibernate.tck.annotations.SpecVersion;


/**
 * @author Hardy Ferentschik
 */
public class TCKAnnotationProcessorFactory implements AnnotationProcessorFactory {

	// Process any set of annotations
	private static final Collection<String> supportedAnnotations
			= unmodifiableCollection(
			Arrays.asList(
					SpecAssertion.class.getCanonicalName(),
					SpecVersion.class.getCanonicalName()
			)
	);

	// No supported options
	private static final Collection<String> supportedOptions = emptySet();


	public Collection<String> supportedOptions() {
		return supportedOptions;
	}

	public Collection<String> supportedAnnotationTypes() {
		return supportedAnnotations;
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> annotationTypeDeclarations, AnnotationProcessorEnvironment annotationProcessorEnvironment) {
		return new TCKAnnotationProcessor( annotationProcessorEnvironment );

	}
}
