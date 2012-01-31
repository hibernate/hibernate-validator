/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.traversableresolver;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.Valid;
import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
@GroupSequence( {Suit.class, Cloth.class })
public class Suit {
	@Max(value=50, groups = { Default.class, Cloth.class})
	@Min(1)
	private Integer size;
	@Valid private Trousers trousers;
	private Jacket jacket;

	public Trousers getTrousers() {
		return trousers;
	}

	public void setTrousers(Trousers trousers) {
		this.trousers = trousers;
	}

	@Valid
	public Jacket getJacket() {
		return jacket;
	}

	public void setJacket(Jacket jacket) {
		this.jacket = jacket;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
