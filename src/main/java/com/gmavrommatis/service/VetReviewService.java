package com.gmavrommatis.service;

import com.gmavrommatis.config.kafka.*;
import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.config.mongo.operations.VetReviewMongoClient;
import com.gmavrommatis.config.mongo.repository.VetReviewRepository;
import com.gmavrommatis.mapper.CreateVetReviewRequestMapper;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewScore;
import com.mongodb.reactivestreams.client.ClientSession;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.netty.channel.EventLoopGroup;
import jakarta.inject.Singleton;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for managing veterinarian reviews in a reactive, non-blocking manner using MongoDB
 * transactions.
 *
 * <p>Provides methods to paginate review retrieval, calculate average review scores, and save new
 * reviews with transactional consistency.
 *
 * @author Your Name
 * @version 1.0
 */
@Singleton
@Slf4j
public class VetReviewService {

  private final ReactiveTransactionOperations<ClientSession> mongoTx;
  private final VetReviewRepository vetReviewRepository;
  private final VetReviewMongoClient vetReviewMongoClient;
  private final VetService vetService;
  private final CreateVetReviewRequestMapper mapper;
  private final VetReviewNotificationProducer vetReviewNotificationProducer;
  private final VetReviewNotificationProducerBatch vetReviewNotificationProducerBatch;
  private final TransactionalProducerPoolManager transactionalProducerPoolManager;
  private VetReviewNotificationProducerTransactional producer = null;
  private VetReviewManualTransactionalProducer manualWrapper = null;

  private final EventLoopGroup eventLoopGroup;

  public VetReviewService(
      ReactiveTransactionOperations<ClientSession> mongoTx,
      VetReviewRepository vetReviewRepository,
      VetReviewMongoClient vetReviewMongoClient,
      VetService vetService,
      CreateVetReviewRequestMapper mapper,
      VetReviewNotificationProducer vetReviewNotificationProducer,
      VetReviewNotificationProducerBatch vetReviewNotificationProducerBatch,
      TransactionalProducerPoolManager transactionalProducerPoolManager,
      EventLoopGroup eventLoopGroup) {
    this.mongoTx = mongoTx;
    this.vetReviewRepository = vetReviewRepository;
    this.vetReviewMongoClient = vetReviewMongoClient;
    this.vetService = vetService;
    this.mapper = mapper;
    this.vetReviewNotificationProducer = vetReviewNotificationProducer;
    this.vetReviewNotificationProducerBatch = vetReviewNotificationProducerBatch;
    this.transactionalProducerPoolManager = transactionalProducerPoolManager;
    this.eventLoopGroup = eventLoopGroup;
  }

  /**
   * Retrieves all {@link VetReview} documents within a read-only transaction.
   *
   * <p>Logs the operation and then wraps the repository call in a {@link
   * TransactionDefinition#READ_ONLY} transaction.
   *
   * @return a {@link Flux} emitting all {@code VetReview} instances
   */
  public Flux<VetReview> findAll() {
    vetReviewRepository.count();
    log.info("findAll");
    return Flux.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY, mongoStatus -> vetReviewRepository.findAll()));
  }

  public Mono<Long> count() {

    return Mono.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            mongoStatus -> vetReviewRepository.count().defaultIfEmpty(0L)));
  }

  /**
   * Retrieves a batch of {@link VetReview} documents within a read-only transaction.
   *
   * <p>Performs an offset/limit query via the {@link VetReviewMongoClient}. The operation is
   * executed in a {@link TransactionDefinition#READ_ONLY} transaction.
   *
   * @param offset the number of documents to skip (zero-based)
   * @param limit the maximum number of documents to retrieve
   * @return a {@link Flux} emitting up to {@code limit} {@code VetReview} instances
   */
  public Flux<VetReview> findBatch(int offset, int limit) {
    return Flux.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            mongoStatus -> vetReviewMongoClient.findBatch(offset, limit)));
  }

  /**
   * Retrieves a paginated list of {@link VetReview} documents.
   *
   * <p>Executes within a read-only MongoDB transaction and returns a {@code Mono<Page<VetReview>>}
   * containing the requested page.
   *
   * @param pageable pagination parameters (zero-based page index and page size)
   * @return a {@link Mono} emitting a {@link Page} of {@link VetReview} documents
   */
  public Mono<Page<VetReview>> findAll(Pageable pageable) {
    return Mono.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY, mongoStatus -> vetReviewRepository.findAll(pageable)));
  }

  /**
   * Computes the average review score for the veterinarian identified by the given first and last
   * name, in a read-only MongoDB transaction.
   *
   * <p>Fetches the vet, retrieves all their reviews, and calculates the average rating.
   *
   * @param firstName the first name of the reviewer whose scores to calculate
   * @param lastName the last name of the reviewer whose scores to calculate
   * @return a {@link Mono} emitting a {@link VetReviewScore} with the computed average, or empty if
   *     the vet has no reviews
   */
  public Mono<VetReviewScore> findVetRatingReactive(String firstName, String lastName) {
    return Mono.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            mongoStatus -> // Mongo transaction
            vetService
                    .findByFirstAndLastName(firstName, lastName) // Mono<Vet>
                    .flatMapMany(
                        vet ->
                            vetReviewRepository
                                .findAllByVetId(vet.getId()) // Flux<VetReview>
                                .map(VetReview::getRating) // Flux<Integer>
                        )
                    .collectList() // Mono<List<Integer>>
                    .map(
                        ratings -> { // compute average
                          double avg =
                              ratings.stream().mapToInt(Short::intValue).average().orElse(0d);
                          return VetReviewScore.builder()
                              .firstName(firstName)
                              .lastName(lastName)
                              .averageRating(avg)
                              .build();
                        })));
  }

  /**
   * Saves a new {@link VetReview} document in a MongoDB transaction.
   *
   * <p>Ensures the vet exists, maps the request to an entity, and persists it. If the reviewer
   * equals "Mike berman", throws a runtime exception to simulate a rollback scenario.
   *
   * @param request the {@link CreateVetReviewRequest} containing review data
   * @return a {@link Mono} emitting the saved {@link VetReview}
   */
  public Mono<VetReview> saveReviewReactive(CreateVetReviewRequest request) {
    log.info("saveReviewReactive");
    return Mono.from(
            mongoTx.withTransaction(
                TransactionDefinition.DEFAULT,
                mongoStatus -> // Mongo Default transaction
                vetService
                        .findByFirstAndLastName(request.getFirstName(), request.getLastName())
                        .flatMap(
                            vet -> {
                              log.info("creating a review");
                              VetReview r = mapper.toVetReview(request);
                              r.setVetId(vet.getId());
                              return Mono.just(r);
                            })
                        .doOnNext(saved -> log.info("saving a review"))
                        /* this is intentionally offloaded to worker threads by Micronaut Mongo transactionality.*/
                        .flatMap(vetReviewRepository::save)
                        .doOnNext(saved -> log.info("saved a review"))
                        /* thus, because we have more reactive event loop related work, is best to return the chain back to event loop.*/
                        .publishOn(Schedulers.fromExecutor(eventLoopGroup.next()))
                        .doOnNext(saved -> log.info("lets get directed back to event loop for now"))
                        .flatMap(
                            saved -> {
                              log.info("Checking for intentional error");
                              if ("Mike berman".equals(saved.getReviewer())) {
                                // any exception here will trigger a rollback
                                return Mono.error(new RuntimeException("Intentional fail"));
                              } else {
                                return Mono.just(saved);
                              }
                            })))
        .doOnNext(saved -> log.info("What thread am i running in?"))
        // At this point Mongo transaction has COMMITTED successfully
        .flatMap(
            saved ->
                Mono.fromRunnable(() -> sendAllKafkaEvents(saved, request))
                    /*here subscribeOn is safe as the entire upstream is the Runnable not the chain, Downstream is everything else */
                    .subscribeOn(
                        Schedulers.boundedElastic()) // run Kafka blocking work off event-loop
                    .thenReturn(saved) // return saved entity AFTER Kafka sends finish
            )
        .doOnNext(saved -> log.info("What thread did Kafka producers send me?"))
        /*The above flatMap executed an offloaded work, now we need to ensure the work is returned back to Event-Loop*/
        .publishOn(Schedulers.fromExecutor(eventLoopGroup.next()))
        .doOnNext(saved -> log.info("finished saveReviewReactive"));
  }

  private void sendAllKafkaEvents(VetReview saved, CreateVetReviewRequest request) {

    log.info("Producing Kafka review notifications...");

    // 1) Basic Event
    vetReviewNotificationProducer.sendReview(mapper.toVetReviewNotificationEvent(request));

    // 2) Event with Header
    vetReviewNotificationProducer.sendReviewWithHeader(
        mapper.toVetReviewNotificationEvent(request), "CustomHeader");

    // 3) Partition ID
    vetReviewNotificationProducer.sendReviewWithEventId(
        1, mapper.toVetReviewNotificationEvent(request));

    // 4) Event Key
    vetReviewNotificationProducer.sendReviewWithEventKey(
        KafkaKeys.VET_REVIEW_NOTIFICATION_KEY, mapper.toVetReviewNotificationEvent(request));

    // 5) Mono send
    vetReviewNotificationProducer
        .sendReviewMono(mapper.toVetReviewNotificationEvent(request))
        .subscribe();

    // 6) Flux batch
    vetReviewNotificationProducerBatch
        .sendReviewFluxBatch(
            Stream.generate(() -> mapper.toVetReviewNotificationEvent(request)).limit(20).toList())
        .subscribe();

    // 7) Flux ordered batch
    vetReviewNotificationProducerBatch
        .sendReviewFluxBatchOrdered(
            KafkaKeys.VET_REVIEW_NOTIFICATION_KEY,
            Flux.fromIterable(
                Stream.generate(() -> mapper.toVetReviewNotificationEvent(request))
                    .limit(20)
                    .toList()))
        .subscribe();

    // 8) Transactional send using pool
    try {
      producer = transactionalProducerPoolManager.acquireTransactional();
      // producer is effectively final, as it is only instantiated once with not null value.
      producer.sendReviewWithEventKeyTransactional(
          KafkaKeys.VET_REVIEW_NOTIFICATION_KEY, mapper.toVetReviewNotificationEvent(request));

    } catch (Exception ex) {
      log.error("Transactional Kafka send failed", ex);
    } finally {
      if (producer != null) {
        transactionalProducerPoolManager.releaseTransactional(producer);
      }
    }

    // 9) Manual producer transaction
    try {
      manualWrapper = transactionalProducerPoolManager.acquireManualTransactional();
      KafkaProducer<String, VetReviewNotificationEvent> manualProducer =
          manualWrapper.getProducer();

      manualProducer.beginTransaction();

      ProducerRecord<String, VetReviewNotificationEvent> record =
          new ProducerRecord<>(
              KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL,
              KafkaKeys.VET_REVIEW_NOTIFICATION_KEY,
              mapper.toVetReviewNotificationEvent(request));

      manualProducer.send(record);

      if ("Fail Kafka".equals(saved.getReviewer())) {
        throw new RuntimeException("Intentional fail");
      }

      manualProducer.commitTransaction();

    } catch (Exception ex) {
      if (manualWrapper != null) {
        manualWrapper.getProducer().abortTransaction();
      }
      log.error("Kafka manual transaction rolled back", ex);

    } finally {
      if (manualWrapper != null) {
        transactionalProducerPoolManager.releaseManualTransactional(manualWrapper);
      }
    }
  }
}
