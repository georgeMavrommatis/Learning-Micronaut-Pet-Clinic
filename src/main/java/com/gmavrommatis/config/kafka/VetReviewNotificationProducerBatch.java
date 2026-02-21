package com.gmavrommatis.config.kafka;

import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.scheduling.TaskExecutors;
import java.util.List;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import reactor.core.publisher.Flux;

@KafkaClient(
    /* unique name for your Kafka producer client that helps  distinguish between multiple producers.*/
    id = "VetReviewClientBatch",
    /*Enables Batch processing*/
    batch = true,
    /* allow setting properties on our Micronaut clients, we set the max retries upon fail confirm of write from Kafka Broker.*/
    properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "5"),
    /* executor Bocking is required under Reactive environment ,
     *as Kafka I/O calls are blocking and we need to protect against blocking the Event Loop,
     * Thus we offload to worker threads.*/
    executor = TaskExecutors.BLOCKING)
public interface VetReviewNotificationProducerBatch {

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_FLUX_BATCH)
  Flux<RecordMetadata> sendReviewFluxBatch(List<VetReviewNotificationEvent> event);

  @Topic(KafkaTopics.VET_REVIEW_NOTIFICATION_FLUX_BATCH_ORDERED)
  Flux<RecordMetadata> sendReviewFluxBatchOrdered(
      @KafkaKey String key, Flux<VetReviewNotificationEvent> event);
}
