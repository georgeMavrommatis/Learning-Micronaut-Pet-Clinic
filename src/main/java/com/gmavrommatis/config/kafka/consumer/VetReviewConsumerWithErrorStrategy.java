package com.gmavrommatis.config.kafka.consumer;

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static io.micronaut.configuration.kafka.annotation.OffsetStrategy.DISABLED;

import com.gmavrommatis.config.kafka.KafkaTopics;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.*;
import io.micronaut.configuration.kafka.exceptions.KafkaListenerException;
import io.micronaut.configuration.kafka.retry.ConditionalRetryBehaviourHandler;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/** Best practice Kafka consumer when ordering is required. */
@KafkaListener(
    /*if the consumer Group is not defined. Then the default one is micronaut.application.name*/
    groupId = ConsumerGroups.VET_REVIEW_ERROR_STRATEGY,
    /*TODO it is required to retrieve events one by one, to prevent processing more events in case of an error.*/
    batch = false,
    /*in case there is no commited offset in the partition we read, we start from the first event in this partition.*/
    offsetReset = EARLIEST,
    /*todo it is required to define manual commit of the offset received to prevent automatic commit on unprocessed events*/
    offsetStrategy = DISABLED,
    /*We can upgrade responsiveness of consumer by adjusting the waiting time to accept new events*/
    pollTimeout = "500ms",
    /*can be also set in app properties with kafka.consumers.myGroup.session.timeout.ms for consumer group*/
    /*Protects from false consumer Death unnecessary Rebalances*/
    properties = @Property(name = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, value = "10000"),
    /*Mandatory to define an error strategy to provide retrying for the failed event in case of error and implement an Outbox-Pattern*/
    errorStrategy =
        @ErrorStrategy(
            value = ErrorStrategyValue.RETRY_CONDITIONALLY_ON_ERROR,
            retryDelay = "50ms",
            retryCount = 3))
@Slf4j
public class VetReviewConsumerWithErrorStrategy implements ConditionalRetryBehaviourHandler {

  @Override
  public ConditionalRetryBehaviour conditionalRetryBehaviour(KafkaListenerException exception) {
    return shouldRetry(exception)
        ? ConditionalRetryBehaviour.RETRY
        : ConditionalRetryBehaviour.SKIP;
  }

  private boolean shouldRetry(KafkaListenerException exception) {
    exception
        .getConsumerRecord()
        .ifPresentOrElse(
            record ->
                log.error(
                    "Consumed Kafka event for Retry (from exception) -> group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={}, payload={}",
                    ConsumerGroups.VET_REVIEW_ERROR_STRATEGY,
                    record.topic(),
                    record.key(),
                    record.partition(),
                    record.offset(),
                    record.headers().lastHeader("My-Header"),
                    DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(record.timestamp())),
                    record.value()),
            () ->
                log.error(
                    "Kafka exception without ConsumerRecord -> group={}",
                    ConsumerGroups.VET_REVIEW_ERROR_STRATEGY,
                    exception));
    // todo OutboxPattern here. (requires db call. read/write)
    /*
    Retry handling strategy:
    1) Kafka does not expose whether the maximum retry attempts have been exhausted.
    2) On processing failure, persist the event in MongoDB with a retryCount + 1.
    3) On each re-delivery (retry or consumer rebalance), check MongoDB:
       - If the event exists and is already marked as processed, skip it.
       - Otherwise, attempt processing again and update the retry count if the max retries are not met.
    4) Unprocessed error events are re-attempted via a scheduled reprocessing job.
    */
    return true;
  }

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION)
  public void receiveVetReviewErrorStrategy(
      /* (1) Optional key, feasible also with @KafkaKey Optional<String> key*/
      @KafkaKey @Nullable String key,
      // (2)
      @MessageBody VetReviewNotificationEvent event,
      // (3)
      long offset,
      // (4)
      @KafkaPartition Integer partition,
      // (5)
      String topic,
      // (6)
      long timestamp,
      /*(7) Optional Header, feasible also with @MessageHeader("My-Header") Optional<String> myHeader*/
      @MessageHeader("My-Header") @Nullable String myHeader,
      Acknowledgement acknowledgement) {
    try {
      log.info(
          "At receiveVetReviewErrorStrategy Consumed Kafka event -> "
              + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
          ConsumerGroups.VET_REVIEW_ERROR_STRATEGY,
          topic,
          key,
          partition,
          offset,
          myHeader,
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
          event);

      // todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.
      // (requires db call.)

      if ("Peter Prince".equals(event.reviewer())) {
        throw new RuntimeException("Intentional Error");
      }

      if (key == null) {
        // todo potential validation exception here.
      }
      if (myHeader == null) {
        // todo potential validation exception here.
      }

      // todo implementation of event here.

    } catch (Exception ex) {
      log.error(
          "Intentional Error while consuming receiveVetReviewNotificationWithNullableGroupB for {} -> "
              + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
          event.reviewer(),
          ConsumerGroups.VET_REVIEW_ERROR_STRATEGY,
          topic,
          key,
          partition,
          offset,
          myHeader,
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
          event,
          ex);

      // todo potentially throw exception for Retry and ourtbox pattern if max retries not reached.
      // (requires db call.)
      throw ex;
      // todo else if max retries reached from Outboc-Pattern, skip this event. (requires db call.)
    }

    // manually commit batch offset
    acknowledgement.ack();
  }
}
