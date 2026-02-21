package com.gmavrommatis.config.kafka.producer;

import io.micronaut.context.BeanProvider;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages a fixed-size pool of transactional Kafka producer instances to achieve deterministic,
 * safe, and high-performance transactional publishing.
 *
 * <p>Micronaut {@code @Prototype} beans are not reused or tracked automatically. This pool manager
 * explicitly instantiates a predefined number of transactional producers at application startup and
 * stores them in {@link BlockingQueue} structures. These queues enforce exclusive access: each
 * producer instance can be checked out by only one thread at a time. If all producers are currently
 * in use, additional requests will block until a producer becomes available.
 *
 * <p>Each transactional producer instance is assigned a unique {@code transactional.id} when it is
 * created. This is required by Kafka’s transactional protocol to prevent producer fencing and
 * ensures that:
 *
 * <ul>
 *   <li>No two threads ever share the same transactional producer concurrently.
 *   <li>Each producer provides consistent isolation for its transactional boundary.
 *   <li>Multiple producers can run in parallel safely and independently.
 *   <li>The application can scale transactional throughput without recreating producers.
 * </ul>
 *
 * <p>The benefits of this pool include:
 *
 * <ul>
 *   <li><strong>Controlled concurrency</strong> – maximum parallel transactions limited to pool
 *       size.
 *   <li><strong>Reusable resources</strong> – avoids the heavy cost of reinitializing KafkaProducer
 *       instances.
 *   <li><strong>Performance</strong> – producers are initialized once and reused efficiently.
 *   <li><strong>Safety</strong> – blocking queue guarantees exclusive access to each transactional
 *       producer.
 * </ul>
 *
 * <p><strong>Important:</strong> Prototype-scoped producers are not destroyed by Micronaut
 * automatically. The {@link PreDestroy} method explicitly closes all producers during application
 * shutdown to flush pending messages, release network resources, and prevent resource leaks.
 */
@Slf4j
@Singleton
public class TransactionalProducerPoolManager {

  private static final int POOL_SIZE = 10;

  private final BlockingQueue<VetReviewNotificationProducerTransactional> transactionalPool;
  private final BlockingQueue<VetReviewManualTransactionalProducer> manualTransactionalPool;

  public TransactionalProducerPoolManager(
      BeanProvider<VetReviewNotificationProducerTransactional> transactionalProvider,
      BeanProvider<VetReviewManualTransactionalProducer> manualTransactionalProvider) {

    // Initialize Micronaut-managed transactional producers
    this.transactionalPool = new ArrayBlockingQueue<>(POOL_SIZE);
    IntStream.range(0, POOL_SIZE).forEach(i -> transactionalPool.add(transactionalProvider.get()));

    // Initialize manually configured transactional producers
    this.manualTransactionalPool = new ArrayBlockingQueue<>(POOL_SIZE);
    IntStream.range(0, POOL_SIZE)
        .forEach(i -> manualTransactionalPool.add(manualTransactionalProvider.get()));
  }

  /**
   * Acquires a unique annotation-based transactional producer. Blocks if all producers are in use.
   */
  public VetReviewNotificationProducerTransactional acquireTransactional()
      throws InterruptedException {
    return transactionalPool.take(); // waits for availability
  }

  /** Releases the annotation-based producer back to the pool. */
  public void releaseTransactional(VetReviewNotificationProducerTransactional producer) {
    try {
      transactionalPool.put(producer); // blocks until space is available
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while returning manual transactional producer to pool", e);
    }
  }

  /**
   * Acquires a unique manually configured transactional producer. Blocks if all producers are in
   * use.
   */
  public VetReviewManualTransactionalProducer acquireManualTransactional()
      throws InterruptedException {
    return manualTransactionalPool.take();
  }

  /** Releases the manually configured producer back to the pool. */
  public void releaseManualTransactional(VetReviewManualTransactionalProducer producer) {
    try {
      manualTransactionalPool.put(producer); // blocks until space is available
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while returning manual transactional producer to pool", e);
    }
  }

  /** Closes all manually configured producers at application shutdown. */
  @PreDestroy
  public void closeAll() {
    manualTransactionalPool.forEach(
        p -> {
          try {
            p.close();
          } catch (Exception e) {
            log.error("Failed to close manual producer", e);
          }
        });
  }
}
