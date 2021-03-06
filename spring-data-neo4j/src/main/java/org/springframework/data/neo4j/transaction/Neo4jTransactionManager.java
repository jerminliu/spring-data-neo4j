/*
 * Copyright (c)  [2011-2016] "Pivotal Software, Inc." / "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.springframework.data.neo4j.transaction;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * This class is a wrapper around the OGM TransactionManager.
 *
 * @author Vince Bickers
 * @deprecated See {@link org.springframework.ogm.neo4j.Neo4jTransactionManager}.
 */
@Deprecated
public class Neo4jTransactionManager implements PlatformTransactionManager {

	private final Logger logger = LoggerFactory.getLogger(Neo4jTransactionManager.class);
	private final Session session;

	public Neo4jTransactionManager(Session session) {
		this.session = session;
	}

	@Override
	public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) throws TransactionException {
		logger.debug("Requesting to create or join a transaction");
		return new Neo4jTransactionStatus(session, transactionDefinition);
	}

	@Override
	public void commit(TransactionStatus transactionStatus) throws TransactionException {
		Transaction tx = ((Neo4jTransactionStatus) transactionStatus).getTransaction();
		if (transactionStatus.isNewTransaction() && canCommit(tx)) {
			logger.debug("Commit requested: " + tx + ", status: " + tx.status().toString());
			tx.commit();
			tx.close();
		}
	}

	@Override
	public void rollback(TransactionStatus transactionStatus) throws TransactionException {
		Transaction tx = ((Neo4jTransactionStatus) transactionStatus).getTransaction();
		if (transactionStatus.isNewTransaction() && canRollback(tx)) {
			logger.debug("Rollback requested: " + tx + ", status: " + tx.status().toString());
			tx.rollback();
			tx.close();
		}
	}


	private boolean canCommit(Transaction tx) {
		switch (tx.status()) {
			case COMMIT_PENDING: return true;
			case OPEN: return true;
			default: return false;
		}
	}

	private boolean canRollback(Transaction tx) {
		switch (tx.status()) {
			case OPEN: return true;
			case ROLLBACK_PENDING:return true;
			default:return false;
		}
	}
}
