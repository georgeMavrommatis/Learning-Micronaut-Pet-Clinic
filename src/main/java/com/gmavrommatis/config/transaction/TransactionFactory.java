package com.gmavrommatis.config.transaction;

import io.micronaut.context.annotation.Factory;
import io.micronaut.data.mongodb.transaction.MongoTransactionOperations;
import io.micronaut.transaction.TransactionOperations;
import io.micronaut.transaction.hibernate.HibernateTransactionManager;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Factory that exposes named transaction operation beans for use with Micronaut’s
 * {@code @Transactional} annotation.
 *
 * <p>Defines:
 *
 * <ul>
 *   <li>{@code mongoTx} – the primary MongoDB transaction operations bean, for use with
 *       {@code @Transactional("mongoTx")}
 *   <li>{@code jpaTx} – the default Hibernate (JPA) transaction manager, for use with
 *       {@code @Transactional("jpaTx")}
 * </ul>
 *
 * @author Your Name
 */
@Factory
public class TransactionFactory {

  /**
   * Exposes the primary MongoDB transaction operations bean under the qualifier "mongoTx".
   *
   * <p>Allows services to annotate methods with {@code @Transactional("mongoTx")} to participate in
   * MongoDB transactions.
   *
   * @param defaultMongoOps the default MongoTransactionOperations provided by Micronaut
   * @return the same operations instance, qualified as "mongoTx"
   */
  @Singleton
  @Named("mongoTx")
  public MongoTransactionOperations mongoTx(MongoTransactionOperations defaultMongoOps) {
    return defaultMongoOps;
  }

  /**
   * Exposes the default Hibernate (JPA) transaction manager under the qualifier "jpaTx".
   *
   * <p>Allows services to annotate methods with {@code @Transactional("jpaTx")} to participate in
   * JPA transactions managed by HibernateTransactionManager.
   *
   * @param defaultManager the default HibernateTransactionManager provided by Micronaut/Spring
   * @return the same transaction manager instance, qualified as "jpaTx"
   */
  @Singleton
  @Named("jpaTx")
  public TransactionOperations<?> jpaTx(HibernateTransactionManager defaultManager) {
    return defaultManager;
  }
}
