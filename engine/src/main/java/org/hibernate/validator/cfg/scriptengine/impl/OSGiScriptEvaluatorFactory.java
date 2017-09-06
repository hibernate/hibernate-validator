/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.scriptengine.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.validation.ValidationException;

import org.hibernate.validator.cfg.scriptengine.ScriptEvaluator;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * {@link org.hibernate.validator.cfg.scriptengine.ScriptEvaluatorFactory} suitable for OSGi environments. It is created
 * based on {@code BundleContext} which is used to iterate through {@code Bundle}s and find all {@link ScriptEngineFactory}
 * candidates.
 *
 * @author Marko Bekhta
 */
public class OSGiScriptEvaluatorFactory extends AbstractCacheableScriptEvaluatorFactory {

	private final List<ScriptEngineManager> scriptEngineManagers;

	public OSGiScriptEvaluatorFactory(BundleContext context) {
		this.scriptEngineManagers = CollectionHelper.toImmutableList( findManagers( context ) );
	}

	@Override
	protected ScriptEvaluator createNewScriptEvaluator(final String languageName) throws ScriptException {
		return scriptEngineManagers.stream()
				.map( manager -> manager.getEngineByName( languageName ) )
				.filter( Objects::nonNull )
				.map( engine -> new ScriptEvaluatorImpl( engine ) )
				.findFirst()
				.orElseThrow( () -> new ValidationException( String.format( "Wasn't able to find script evaluator for '%s'.", languageName ) ) );
	}


	private List<ScriptEngineManager> findManagers(BundleContext context) {
		return findFactoryCandidates( context ).stream()
				.map( className -> {
					try {
						return new ScriptEngineManager( Class.forName( className ).getClassLoader() );
					}
					catch (ClassNotFoundException e) {
						throw new ValidationException( "Wasn't able to instantiate '" + className + "' based engine factory manager.", e );
					}
				} ).collect( Collectors.toList() );
	}


	/**
	 * Iterates through all bundles to get the available {@link ScriptEngineFactory} classes
	 *
	 * @return the names of the available ScriptEngineFactory classes
	 *
	 * @throws IOException
	 */
	private List<String> findFactoryCandidates(BundleContext context) {
		return Arrays.stream( context.getBundles() )
				.filter( Objects::nonNull )
				.filter( bundle -> !"system.bundle".equals( bundle.getSymbolicName() ) )
				.flatMap( this::toStreamOfResourcesURL )
				.filter( Objects::nonNull )
				.flatMap( url -> toListOfFactoryCandidates( url ).stream() )
				.collect( Collectors.toList() );
	}

	private Stream<URL> toStreamOfResourcesURL(Bundle bundle) {
		Enumeration<URL> entries = bundle.findEntries(
				"META-INF/services",
				"javax.script.ScriptEngineFactory",
				false
		);
		return entries != null ? Collections.list( entries ).stream() : Stream.empty();
	}

	private List<String> toListOfFactoryCandidates(URL url) {
		try ( BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) ) ) {
			return reader.lines()
					.map( String::trim )
					.filter( line -> !line.isEmpty() )
					.filter( line -> !line.startsWith( "#" ) )
					.collect( Collectors.toList() );
		}
		catch (IOException e) {
			throw new ValidationException( "Wasn't able to read a ScripFactory candidate resource file", e );
		}
	}

}
