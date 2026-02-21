package com.gmavrommatis.config.kafka.consumer;

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static io.micronaut.configuration.kafka.annotation.OffsetStrategy.DISABLED;

import com.gmavrommatis.config.kafka.KafkaTopics;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.KafkaPartition;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.core.publisher.Mono;

@KafkaListener(
    /*if the consumer Group is not defined. Then the default one is micronaut.application.name*/
    groupId = ConsumerGroups.VET_REVIEW_GROUP_B,
    /*we are not batch consuming the batch retrieved from kafka.*/
    batch = false,
    /*in case there is no commited offset in the partition we read, we start from the first event in this partition.*/
    offsetReset = EARLIEST,
    /*we DISABLED offset auto commit to take full control of commit strategy.*/
    offsetStrategy = DISABLED,
    /*We can upgrade responsiveness of consumer by adjusting the waiting time to accept new events*/
    pollTimeout = "500ms",
    /*can be also set in app properties with kafka.consumers.myGroup.session.timeout.ms for consumer group*/
    /*Protects from false consumer Death unnecessary Rebalances*/
    properties = @Property(name = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, value = "10000"))
@Slf4j
public class VetReviewConsumerGroupB {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION)
  public void receiveVetReviewNotificationWithNullableGroupB(
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
          "At receiveVetReviewNotificationWithNullableGroupB Consumed Kafka event -> "
              + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
          ConsumerGroups.VET_REVIEW_GROUP_B,
          topic,
          key,
          partition,
          offset,
          myHeader,
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
          event);

      // todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.

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
          ConsumerGroups.VET_REVIEW_GROUP_B,
          topic,
          key,
          partition,
          offset,
          myHeader,
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
          event,
          ex);
      // todo potential implementation of Out-Box pattern Write here to save failed event to DLT.
    }

    // manually commit batch offset
    acknowledgement.ack();
  }

  /*Consumer reading partitions across 2 topics*/
  @Topic({
    KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL,
    KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL
  })
  public void receiveVetReviewNotificationWithNullableGroupBConsolidatedMultipleTopics(
      ConsumerRecord<String, VetReviewNotificationEvent> consumerRecord,
      Acknowledgement acknowledgement) {
    try {
      log.info(
          "At receiveVetReviewNotificationWithNullableGroupBConsolidatedMultipleTopics Consumed Kafka event -> "
              + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
          ConsumerGroups.VET_REVIEW_GROUP_B,
          consumerRecord.topic(),
          consumerRecord.key(),
          consumerRecord.partition(),
          consumerRecord.offset(),
          consumerRecord.headers().headers("My-Header"),
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(consumerRecord.timestamp())),
          consumerRecord.value());

      /*topic based implementation*/
      switch (consumerRecord.topic()) {
        case KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL -> {
          /*todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.*/
          /*Todo implementation to topic specific*/
        }
        case KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL -> {
          // todo potential implementation of Out-Box pattern Read/Write here in case we need to
          // skip.
          /*Todo implementation to topic specific*/
        }
        /*todo guard against future unwanted topic implementation*/
        default -> throw new RuntimeException("Unexpected Topic: " + consumerRecord.topic());
      }

      // manually commit offset
      acknowledgement.ack();

    } catch (Exception ex) {
      log.error(
          "Intentional Error while consuming receiveVetReviewNotificationWithNullableGroupBConsolidated -> "
              + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
          ConsumerGroups.VET_REVIEW_GROUP_B,
          consumerRecord.topic(),
          consumerRecord.key(),
          consumerRecord.partition(),
          consumerRecord.offset(),
          consumerRecord.headers().headers("My-Header"),
          DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(consumerRecord.timestamp())),
          consumerRecord.value(),
          ex);

      // todo potential implementation of Out-Box pattern Write here to save failed event to DLT.
    }
  }

  /* @Blocking annotation here instructs Micronaut to subscribe to the returned reactive type in a blocking manner,
  which will result in blocking the poll loop, preventing  Micronaut poll loop to continue, potentially before the doOnSuccess is called*/
  @Blocking
  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_MONO)
  public Mono<VetReviewNotificationEvent> receiveVetReviewNotificationReactiveMono(
      Mono<VetReviewNotificationEvent> eventMono, Acknowledgement acknowledgement) {

    return eventMono
        .flatMap(
            event ->
                Mono.fromRunnable(
                        () -> {
                          log.info(
                              "At receiveVetReviewNotificationReactiveMono Consumed Kafka event -> "
                                  + "group={},  payload={}",
                              ConsumerGroups.VET_REVIEW_GROUP_B,
                              event);

                          /*todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.*/
                          if ("Peter Prince".equals(event.reviewer())) {
                            throw new RuntimeException("Intentional Error");
                          }

                          // todo implementation of event here.
                        })
                    .thenReturn(event)
                    .onErrorResume(
                        ex -> {
                          log.error(
                              "Intentional Error while consuming receiveVetReviewNotificationReactiveMono -> "
                                  + "group={},  payload={}",
                              ConsumerGroups.VET_REVIEW_GROUP_B,
                              event,
                              ex);

                          /*todo potential implementation of Out-Box pattern Write here to save failed event to DLT.*/

                          return Mono.empty(); // continue safely
                        }))
        .doOnSuccess(r -> acknowledgement.ack());

    // a next poll is only initiated when previous completes even in Mono or Flux.
  }
}
