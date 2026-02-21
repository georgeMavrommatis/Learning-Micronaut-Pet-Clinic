package com.gmavrommatis.config.kafka;

import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaPartition;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.kafka.clients.producer.ProducerConfig;
import reactor.core.publisher.Mono;

@KafkaClient(
    /* unique name for your Kafka producer client that helps  distinguish between multiple producers.*/
    id = "VetReviewClient",
    /* allow setting properties on our Micronaut clients, we set the max retries upon fail confirm of write from Kafka Broker.*/
    properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "5"),
    /* executor Bocking is required under Reactive environment ,
     *as Kafka I/O calls are blocking and we need to protect against blocking the Event Loop,
     * Thus we offload to worker threads.*/
    executor = TaskExecutors.BLOCKING)
public interface VetReviewNotificationProducer {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION)
  void sendReview(VetReviewNotificationEvent event);

  /*We can also pass collection of Header like: Collection<Header> headers*/
  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_HEADER)
  void sendReviewWithHeader(
      VetReviewNotificationEvent event, @MessageHeader("My-Header") String myHeader);

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY)
  void sendReviewWithEventKey(@KafkaKey String key, VetReviewNotificationEvent event);

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_ID)
  void sendReviewWithEventId(@KafkaPartition int id, VetReviewNotificationEvent event);

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_MONO)
  Mono<VetReviewNotificationEvent> sendReviewMono(VetReviewNotificationEvent event);
}
