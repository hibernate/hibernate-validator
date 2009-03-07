// $Id:$
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
package org.hibernate.validation.engine.groups;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public class Order implements Auditable {
	private String creationDate;
	private String lastUpdate;
	private String lastModifier;
	private String lastReader;
	private String orderNumber;

	public String getCreationDate() {
		return this.creationDate;
	}

	public String getLastUpdate() {
		return this.lastUpdate;
	}

	public String getLastModifier() {
		return this.lastModifier;
	}

	public String getLastReader() {
		return this.lastReader;
	}

	@NotNull
	@Size(min = 10, max = 10)
	public String getOrderNumber() {
		return this.orderNumber;
	}
}
