package com.gmavrommatis.config.kafka.consumer;

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static io.micronaut.configuration.kafka.annotation.OffsetStrategy.DISABLED;

import com.gmavrommatis.config.kafka.KafkaTopics;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.messaging.Acknowledgement;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@KafkaListener(
    /*if the consumer Group is not defined. Then the default one is micronaut.application.name*/
    groupId = ConsumerGroups.VET_REVIEW_GROUP_BATCH,
    /*we are not batch consuming the batch retrieved from kafka.*/
    batch = true,
    /*in case there is no commited offset in the partition we read, we start from the first event in this partition.*/
    offsetReset = EARLIEST,
    /*we define an auto commit of the offset received with Micronaut automatically commit offsets periodically*/
    offsetStrategy = DISABLED,
    /*We can upgrade responsiveness of consumer by adjusting the waiting time to accept new events*/
    pollTimeout = "500ms",
    /*can be also set in app properties with kafka.consumers.myGroup.session.timeout.ms for consumer group*/
    /*Protects from false consumer Death unnecessary Rebalances*/
    properties = @Property(name = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, value = "10000"))
@Slf4j
public class VetReviewConsumerBatch {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL)
  public void receiveVetReviewNotificationSynchronousBatch(
      List<ConsumerRecord<String, VetReviewNotificationEvent>> records,
      Acknowledgement acknowledgement) {
    log.info(
        "Receive next Batch for receiveVetReviewNotificationSynchronousBatch Batch size: {}",
        records.size());

    for (ConsumerRecord<String, VetReviewNotificationEvent> record : records) {
      try {
        log.info(
            "At receiveVetReviewNotificationSynchronousBatch Consumed Kafka event -> "
                + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
            ConsumerGroups.VET_REVIEW_GROUP_BATCH,
            record.topic(),
            record.key(),
            record.partition(),
            record.offset(),
            record.headers().headers("My-Header"),
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(record.timestamp())),
            record.value());
        // todo potential implementation of Out-Box pattern Read/Write here in case we need to
        // skip.

        if ("Peter Prince".equals(record.value().reviewer())) {
          throw new RuntimeException("Intentional Error");
        }

        if (record.key() == null) {
          // todo potential validation exception here.
        }
        if (record.headers().headers("My-Header") == null) {
          // todo potential validation exception here.
        }
        // todo implementation of event here.

      } catch (Exception ex) {
        log.error(
            "Intentional Error while consuming receiveVetReviewNotificationSynchronousBatch -> "
                + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
            ConsumerGroups.VET_REVIEW_GROUP_BATCH,
            record.topic(),
            record.key(),
            record.partition(),
            record.offset(),
            record.headers().headers("My-Header"),
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(record.timestamp())),
            record.value(),
            ex);
        // todo potential implementation of Out-Box pattern Write here to save failed event to
        // DLT.
      }
    }

    // manually commit batch offset
    acknowledgement.ack();

    // a next batch is only polled when previous completes.
  }

  /* @Blocking annotation here instructs Micronaut to subscribe to the returned reactive type in a blocking manner,
  which will result in blocking the poll loop, preventing  Micronaut poll loop to continue, potentially before the doOnSuccess is called*/
  @Blocking
  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_FLUX_BATCH_ORDERED)
  public Flux<ConsumerRecord<String, VetReviewNotificationEvent>>
      receiveVetReviewNotificationReactiveBatchFlux(
          Flux<ConsumerRecord<String, VetReviewNotificationEvent>> consumerRecordFlux,
          Acknowledgement acknowledgement) {

    return consumerRecordFlux
        .flatMap(
            record ->
                Mono.fromRunnable(
                        () -> {
                          log.info(
                              "At receiveVetReviewNotificationReactiveBatchFlux Consumed Kafka event -> "
                                  + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={}, payload={}",
                              ConsumerGroups.VET_REVIEW_GROUP_BATCH,
                              record.topic(),
                              record.key(),
                              record.partition(),
                              record.offset(),
                              record.headers().lastHeader("My-Header"),
                              Instant.ofEpochMilli(record.timestamp()),
                              record.value());

                          /*todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.*/

                          if ("Peter Prince".equals(record.value().reviewer())) {
                            throw new RuntimeException("Intentional Error");
                          }

                          if (record.key() == null) {
                            // todo potential validation exception here.
                          }
                          if (record.headers().headers("My-Header") == null) {
                            // todo potential validation exception here.
                          }

                          // todo implementation of event here.
                        })
                    .onErrorResume(
                        ex -> {
                          log.error(
                              "Intentional Error while consuming receiveVetReviewNotificationReactiveBatchFlux -> "
                                  + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
                              ConsumerGroups.VET_REVIEW_GROUP_BATCH,
                              record.topic(),
                              record.key(),
                              record.partition(),
                              record.offset(),
                              record.headers().headers("My-Header"),
                              DateTimeFormatter.ISO_INSTANT.format(
                                  Instant.ofEpochMilli(record.timestamp())),
                              record.value(),
                              ex);

                          /*todo potential implementation of Out-Box pattern Write here to save failed event to DLT.*/

                          return Mono.empty(); // continue with next record
                        })
                    .thenReturn(record))
        .doOnComplete(acknowledgement::ack);

    // a next batch poll is only initiated when previous completes even in Mono or Flux.
  }
}
