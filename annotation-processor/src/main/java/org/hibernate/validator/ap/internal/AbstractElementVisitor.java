/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal;

import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.ElementVisitor;
import javax.lang.model.util.ElementKindVisitor8;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.Configuration;
import org.hibernate.validator.ap.internal.util.MessagerAdapter;

/**
 * An abstract {@link ElementVisitor} that should be used for implementation
 * of any other element visitors. The only method present in this class ({@link AbstractElementVisitor#reportIssues(Collection)}
 * is used to report found {@link ConstraintCheckIssue}s. Each {@link ConstraintCheckIssue} occurred will be reported using the
 * {@link javax.annotation.processing.Messager} API.
 *
 * @author Marko Bekhta
 */
public class AbstractElementVisitor<T, V> extends ElementKindVisitor8<T, V> {

	protected final MessagerAdapter messager;

	protected final boolean verbose;

	public AbstractElementVisitor(
			MessagerAdapter messager,
			Configuration configuration) {
		this.messager = messager;
		this.verbose = configuration.isVerbose();

	}

	/**
	 * Reports provided issues using {@link javax.annotation.processing.Messager} API based on their
	 * kind ({@link ConstraintCheckIssue.IssueKind}).
	 *
	 * @param foundIssues a collection of issues to be reported
	 */
	protected void reportIssues(Collection<ConstraintCheckIssue> foundIssues) {
		Set<ConstraintCheckIssue> warnings = CollectionHelper.newHashSet();
		Set<ConstraintCheckIssue> errors = CollectionHelper.newHashSet();

		for ( ConstraintCheckIssue issue : foundIssues ) {
			if ( issue.isError() ) {
				errors.add( issue );
			}
			else if ( issue.isWarning() ) {
				warnings.add( issue );
			}
		}

		messager.reportErrors( errors );
		messager.reportWarnings( warnings );
	}
}

