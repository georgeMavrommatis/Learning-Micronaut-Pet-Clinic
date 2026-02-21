#Once in the shell of container execute:
for t in VetReviewNotification \
VetReviewNotificationWithFluxBatch \
VetReviewNotificationWithFluxBatchOrdered \
VetReviewNotificationWithHeader \
VetReviewNotificationWithId \
VetReviewNotificationWithKey \
VetReviewNotificationWithKeyTransactional \
VetReviewNotificationWithKeyTransactionalManual \
VetReviewNotificationWithMono; do
  kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic "$t" --partitions 5 --replication-factor 1;
done


#Navigate from browser to http://localhost:8081/
# select topics to view them.