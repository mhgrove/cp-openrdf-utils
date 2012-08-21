/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.openrdf;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;

import org.openrdf.repository.sail.SailRepository;

import org.openrdf.sail.memory.MemoryStore;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * <p>Utility methods for working with the OpenRDF Sesame API.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	0.7
 */
public final class OpenRdfUtil {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenRdfUtil.class);

	/**
	 * No instances
	 */
	private OpenRdfUtil() {
	}

	/**
	 * Create a simple in-memory {@link Repository}
	 * @return an in memory Repository
	 */
	public static ExtRepository createInMemoryRepo() {
		try {
			Repository aRepo = new SailRepository(new MemoryStore());

			aRepo.initialize();

			return new ExtRepository(aRepo);
		}
		catch (RepositoryException e) {
			// impossible?
			throw new RuntimeException(e);
		}
	}

	/**
	 * Quietly close the connection object
	 * @param theConn the connection to close
	 */
	public static void close(RepositoryConnection theConn) {
		if (theConn != null) {
			try {
				theConn.commit();

				theConn.close();
			}
			catch (RepositoryException e) {
				LOGGER.error("There was an error while closing the RepositoryConnection.", e);
			}
		}
	}
}
