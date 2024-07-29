/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.build.enforcer;

public class VersionAlignData {

	private String property;
	private String artifact;
	private boolean failOnNotFound = false;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public boolean isFailOnNotFound() {
		return failOnNotFound;
	}

	public void setFailOnNotFound(boolean failOnNotFound) {
		this.failOnNotFound = failOnNotFound;
	}

	@Override
	public String toString() {
		return "{" +
				"property='" + property + '\'' +
				", artifact='" + artifact + '\'' +
				", failOnNotFound=" + failOnNotFound +
				'}';
	}

}
