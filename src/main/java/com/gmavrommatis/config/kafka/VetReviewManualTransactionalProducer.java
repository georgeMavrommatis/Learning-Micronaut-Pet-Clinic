package com.gmavrommatis.config.kafka;

import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.context.annotation.Prototype;
import jakarta.annotation.PreDestroy;
import java.io.Closeable;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * Prototype-scoped wrapper for a transactional {@link KafkaProducer} used to manually produce
 * {@link VetReviewNotificationEvent} messages with full transactional guarantees.
 *
 * <p>Each instance of this class creates a new underlying {@code KafkaProducer} with a unique
 * transactional ID generated via {@link java.util.UUID#randomUUID()}. This is required because
 * Kafka enforces strict transactional semantics: a transactional producer instance must not be
 * shared or reused concurrently across multiple threads.
 *
 * <p>The bean is declared as {@link Prototype} so Micronaut creates a new instance every time it is
 * requested. Micronaut does not track prototype-scoped beans after creation, therefore they are not
 * automatically destroyed. The {@link #close()} method is annotated with {@link PreDestroy} so that
 * higher-level components (such as a producer pool manager) can invoke proper resource cleanup when
 * shutting down the application.
 *
 * <p>The underlying {@code KafkaProducer} is initialized with:
 *
 * <ul>
 *   <li>Idempotence enabled for exactly-once semantics
 *   <li>ACKS=all to ensure broker-level replication guarantees
 *   <li>RETRIES=Integer.MAX_VALUE for resilience
 *   <li>A unique transactional ID for safe isolation of producer sessions
 * </ul>
 *
 * <p>Instances of this class should be managed by a pool or created/destroyed per transaction,
 * depending on throughput requirements. They must be closed explicitly to flush pending writes,
 * release network resources, stop internal Kafka threads, and finalize transactional state.
 */
@Prototype
public class VetReviewManualTransactionalProducer implements Closeable {

  private final KafkaProducer<String, VetReviewNotificationEvent> producer;

  /**
   * Creates a new transactional Kafka producer instance with a unique transactional ID.
   *
   * <p>The producer is fully configured and {@link KafkaProducer#initTransactions()} is invoked
   * during construction to initialize transactional support with the Kafka broker.
   */
  public VetReviewManualTransactionalProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    props.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "com.gmavrommatis.config.kafka.serializer.VetReviewNotificationEventSerializer");
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "vet-review-tx-" + UUID.randomUUID());

    this.producer = new KafkaProducer<>(props);
    this.producer.initTransactions();
  }

  /**
   * Closes the underlying Kafka producer.
   *
   * <p>This method is marked with {@link PreDestroy}, but Micronaut will not automatically call it
   * for prototype-scoped beans. Instead, it is intended to be invoked manually by a managing
   * component (such as a pool manager) during application shutdown or controlled cleanup.
   *
   * <p>Closing the producer:
   *
   * <ul>
   *   <li>Flushes any pending messages
   *   <li>Releases network connections
   *   <li>Stops internal Kafka I/O threads
   *   <li>Properly finalizes transactional state on the broker
   * </ul>
   */
  @PreDestroy
  @Override
  public void close() {
    producer.close();
  }

  /**
   * Returns the underlying {@link KafkaProducer} instance for sending messages or initiating
   * transactional operations.
   *
   * @return the transactional Kafka producer
   */
  public KafkaProducer<String, VetReviewNotificationEvent> getProducer() {
    return producer;
  }
}
