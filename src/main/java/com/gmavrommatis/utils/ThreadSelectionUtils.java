package com.gmavrommatis.utils;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ThreadSelectionUtils {
  private static final Pattern EVENT_LOOP_PATTERN =
      Pattern.compile("EventLoop", Pattern.CASE_INSENSITIVE);

  public static void logThreadName(String threadName) {
    String pool = EVENT_LOOP_PATTERN.matcher(threadName).find() ? "EVENT-LOOP" : "WORKER";
    log.info("→ executed on {}", pool);
  }
}
