package com.gmavrommatis.config.kafka.consumer;

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static io.micronaut.configuration.kafka.annotation.OffsetStrategy.AUTO;

import com.gmavrommatis.config.kafka.KafkaTopics;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.KafkaPartition;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;

@KafkaListener(
    /*if the consumer Group is not defined. Then the default one is micronaut.application.name*/
    groupId = ConsumerGroups.VET_REVIEW_GROUP,
    /*we are not batch consuming the batch retrieved from kafka.*/
    batch = false,
    /*in case there is no commited offset in the partition we read, we start from the first event in this partition.*/
    offsetReset = EARLIEST,
    /*we define an auto commit of the offset received with Micronaut automatically commit offsets periodically*/
    offsetStrategy = AUTO,
    /*We can upgrade responsiveness of consumer by adjusting the waiting time to accept new events*/
    pollTimeout = "500ms",
    /*can be also set in app properties with kafka.consumers.myGroup.session.timeout.ms for consumer group*/
    /*Protects from false consumer Death unnecessary Rebalances*/
    properties = @Property(name = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, value = "10000"))
@Slf4j
public class VetReviewConsumer {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL)
  public void receiveVetReviewNotification(
      /* (1) Permitted only if Partition event contains a Key, else event immediately fails*/
      @KafkaKey String key,
      // (2)
      @MessageBody VetReviewNotificationEvent event,
      // (3)
      long offset,
      // (4)
      @KafkaPartition Integer partition,
      // (5)
      String topic,
      // (6) milliseconds since epoch
      long timestamp,
      /*(7) Is treated as mandatory and if missing the event is retried forever. Alternatively we can mark it as Nullable*/
      @MessageHeader("My-Header") String myHeader) {
    log.info(
        "At receiveVetReviewNotification Consumed Kafka event -> "
            + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
        ConsumerGroups.VET_REVIEW_GROUP,
        topic,
        key,
        partition,
        offset,
        myHeader,
        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
        event);

    // event will be auto commited after this method completes
  }

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION)
  public void receiveVetReviewNotificationWithNullable(
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
      @MessageHeader("My-Header") @Nullable String myHeader) {

    // todo potential implementation of Out-Box pattern Read/Write here in case we need to skip.
    log.info(
        "At receiveVetReviewNotificationWithNullable Consumed Kafka event -> "
            + "group={}, topic={}, key={}, partition={}, offset={}, header={}, timestamp={},  payload={}",
        ConsumerGroups.VET_REVIEW_GROUP,
        topic,
        key,
        partition,
        offset,
        myHeader,
        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp)),
        event);

    if (key == null) {
      // todo potential validation exception here.
    }
    if (myHeader == null) {
      // todo potential validation exception here.
    }

    // todo implementation of event here.

    // event will be auto commited after this method completes
  }
}
