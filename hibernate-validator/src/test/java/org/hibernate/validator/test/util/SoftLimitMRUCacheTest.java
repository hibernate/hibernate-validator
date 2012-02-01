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
package org.hibernate.validator.test.util;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.hibernate.validator.util.SoftLimitMRUCache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Sanity checks for {@code SoftLimitMRUCacheTest}
 *
 * @author Hardy Ferentschik
 */
public class SoftLimitMRUCacheTest {
	private static final ExecutorService pool = Executors.newCachedThreadPool();
	private static final int THREAD_COUNT = 10;
	private static final int MAX_KEY = 10;
	private static final int LOOP_COUNT = 10000;

	private static final Random random = new Random();
	private CyclicBarrier barrier;

	@AfterClass
	public void afterClass() {
		pool.shutdown();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGetNullKey() {
		SoftLimitMRUCache cache = new SoftLimitMRUCache<Integer, String>( 1, 10 );
		cache.get( null );
	}

	@Test()
	public void testEmpty() {
		SoftLimitMRUCache cache = new SoftLimitMRUCache<Integer, String>( 1, 10 );
		assertEquals( cache.size(), 0 );
		assertEquals( cache.softSize(), 0 );

		cache.put( 1, "foo" );

		assertEquals( cache.size(), 1 );

		cache.clear();
		assertEquals( cache.size(), 0 );
		assertEquals( cache.softSize(), 0 );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidHardAndSoftLimit() {
		new SoftLimitMRUCache<Integer, String>( 0, 0 );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testHardLimitGreaterThanSoftLimit() {
		new SoftLimitMRUCache<String, String>( 10, 1 );
	}

	@Test
	public void testConcurrentAccess() {
		SoftLimitMRUCache cache = new SoftLimitMRUCache<Integer, String>( 1, 10 );
		barrier = new CyclicBarrier( THREAD_COUNT + 1 );

		try {
			for ( int i = 0; i < THREAD_COUNT; i++ ) {
				pool.execute( new CacheClient( cache ) );
			}
			barrier.await(); // wait for all threads to be ready
			barrier.await(); // wait for threads to finish
			assertTrue( cache.size() == 1 );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}

	}

	class CacheClient implements Runnable {
		private SoftLimitMRUCache cache;

		CacheClient(SoftLimitMRUCache cache) {
			this.cache = cache;
		}

		public void run() {
			try {
				barrier.await();
				for ( int i = 0; i < LOOP_COUNT; i++ ) {
					Integer key = random.nextInt( MAX_KEY );
					if ( cache.get( key ) == null ) {
						if ( random.nextFloat() <= 0.6f ) {
							cache.put( key, key.toString() );
						}
					}
				}
				barrier.await();
			}
			catch ( Exception e ) {
				throw new RuntimeException( e );
			}
		}
	}
}


