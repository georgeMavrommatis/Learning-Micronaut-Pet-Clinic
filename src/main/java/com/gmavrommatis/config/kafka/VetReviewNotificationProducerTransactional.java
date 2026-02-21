package com.gmavrommatis.config.kafka;

import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.kafka.clients.producer.ProducerConfig;

/*Transactional producers cannot be shared among threads.
Thus, due to Transactional we need A NEW instance every time it is injected or fetched, instead of using the Singleton bean.
 This is achieved with @Prototype*/
@Prototype
@KafkaClient(
    /* unique name for your Kafka producer client that helps  distinguish between multiple producers.*/
    id = "VetReviewClient",
    /*each instance of our producer gets a different transactional ID.
     * only protects against producer event send to kafka fails related.*/
    transactionalId = "vet-review-tx-id-${random.uuid}",
    /*Requires acknowledgement from all Replicate Brokers that received the Send of the Event*/
    acks = KafkaClient.Acknowledge.ALL,
    /* allow setting properties on our Micronaut clients, we set the max retries upon fail confirm of write from Kafka Broker.*/
    properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "5"),
    /* executor Bocking is required under Reactive environment ,
     *as Kafka I/O calls are blocking, and we need to protect against blocking the Event Loop,
     * Thus we offload to worker threads.*/
    executor = TaskExecutors.BLOCKING)
public interface VetReviewNotificationProducerTransactional {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL)
  void sendReviewWithEventKeyTransactional(@KafkaKey String key, VetReviewNotificationEvent event);
}
