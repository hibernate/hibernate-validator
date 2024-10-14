/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.build.enforcer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;

@Named("versionAlignRule") // rule name - must start with lowercase character
public class VersionAlignRule extends AbstractEnforcerRule {

	/**
	 * Rule parameter as list of items.
	 */
	private List<VersionAlignData> propertiesToCheck;

	// Inject needed Maven components
	@Inject
	private MavenSession session;

	public void execute() throws EnforcerRuleException {
		for ( VersionAlignData data : propertiesToCheck ) {
			boolean found = false;
			for ( Dependency dependency : session.getCurrentProject().getDependencyManagement().getDependencies() ) {
				if ( data.getArtifact().equals( dependency.getGroupId() + ":" + dependency.getArtifactId() ) ) {
					if ( !dependency.getVersion().equals( data.getProperty() ) ) {
						throw new EnforcerRuleException( "Property for version of " + dependency
								+ " is incorrect. Version property is set to " + data.getProperty() );
					}
					else {
						found = true;
					}
				}
			}
			if ( !found && data.isFailOnNotFound() ) {
				throw new EnforcerRuleException(
						"Wasn't able to find the `" + data.getArtifact() + "` among managed dependencies." );
			}
		}
	}

	@Override
	public String toString() {
		return "VersionAlignRule{" +
				"propertiesToCheck=" + propertiesToCheck +
				'}';
	}
}
