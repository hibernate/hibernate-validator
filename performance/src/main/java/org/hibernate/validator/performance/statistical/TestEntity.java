/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.performance.statistical;

import java.util.Date;
import java.util.Random;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public class TestEntity {
	public static final int MAX_DEPTH = 32;

	public TestEntity(Random random, int depth) {
		if ( depth <= MAX_DEPTH ) {
			int randomNumber = random.nextInt( 2 );
			if ( randomNumber == 1 ) {
				depth++;
				testEntity = new TestEntity( random, depth );
			}
		}
	}

	@Size
	private String value1;

	@Min(0)
	private Integer value2;

	@Max(100)
	private Integer value3;

	@Past
	private Date value4;

	@Future
	private Date value5;

	@Null
	private String value6;

	@NotNull
	private String value7;

	@Valid
	private TestEntity testEntity;
}


