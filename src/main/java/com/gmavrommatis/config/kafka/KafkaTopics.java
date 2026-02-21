package com.gmavrommatis.config.kafka;

public final class KafkaTopics {

  private KafkaTopics() {}

  public static final String VET_REVIEW_NOTIFICATION = "VetReviewNotification";
  public static final String VET_REVIEW_NOTIFICATION_WITH_HEADER =
      "VetReviewNotificationWithHeader";
  public static final String VET_REVIEW_NOTIFICATION_WITH_KEY = "VetReviewNotificationWithKey";
  public static final String VET_REVIEW_NOTIFICATION_WITH_ID = "VetReviewNotificationWithId";
  public static final String VET_REVIEW_NOTIFICATION_MONO = "VetReviewNotificationWithMono";
  public static final String VET_REVIEW_NOTIFICATION_FLUX_BATCH =
      "VetReviewNotificationWithFluxBatch";
  public static final String VET_REVIEW_NOTIFICATION_FLUX_BATCH_ORDERED =
      "VetReviewNotificationWithFluxBatchOrdered";
  public static final String VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL =
      "VetReviewNotificationWithKeyTransactional";
  public static final String VET_REVIEW_NOTIFICATION_WITH_KEY_TRANSACTIONAL_MANUAL =
      "VetReviewNotificationWithKeyTransactionalManual";
}
