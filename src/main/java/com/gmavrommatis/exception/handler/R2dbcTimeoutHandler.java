package com.gmavrommatis.exception.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.r2dbc.spi.R2dbcTimeoutException;
import jakarta.inject.Singleton;

/**
 * Exception handler that maps {@link R2dbcTimeoutException R2DBC timeout} errors to a {@link
 * HttpStatus#GATEWAY_TIMEOUT 504 Gateway Timeout} HTTP response.
 *
 * <p>This handler is registered as a {@link Singleton} and only loaded into the application context
 * if both {@code R2dbcTimeoutException} and Micronaut's {@link ExceptionHandler} classes are
 * present on the classpath (see {@link Requires}). That makes it safe to include in projects that
 * sometimes do not depend on R2DBC.
 *
 * <p>Behavior: when an {@link R2dbcTimeoutException} is thrown while handling a request, Micronaut
 * will delegate to this handler which returns a JSON-like body describing the error:
 *
 * <pre>
 * {
 *   "error": "DB connect timeout",
 *   "message": "&lt;exception message&gt;"
 * }
 * </pre>
 *
 * and an HTTP status of {@code 504 Gateway Timeout}.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>The handler is intentionally lightweight and designed for user-facing error messages; it
 *       does not log or rethrow the original exception. If you need logging or metrics, wrap or
 *       extend this handler to add that behavior.
 *   <li>Because it returns a generic {@link HttpResponse<?>}, you may adapt the body shape to fit
 *       your API error contract (e.g., include an error code or correlation id).
 * </ul>
 *
 * @since 1.0 (adjust version as appropriate)
 * @see R2dbcTimeoutException
 * @see ExceptionHandler
 */
@Produces
@Requires(classes = {R2dbcTimeoutException.class, ExceptionHandler.class})
@Singleton
public class R2dbcTimeoutHandler
    implements ExceptionHandler<R2dbcTimeoutException, HttpResponse<?>> {

  /**
   * Handle an {@link R2dbcTimeoutException} thrown while processing an HTTP request.
   *
   * <p>The returned response uses HTTP status {@link HttpStatus#GATEWAY_TIMEOUT GATEWAY_TIMEOUT}
   * and a simple map body with two entries:
   *
   * <ul>
   *   <li>{@code "error"} – a short, stable error label ("DB connect timeout")
   *   <li>{@code "message"} – the exception message (may contain driver-specific details)
   * </ul>
   *
   * <p>Example returned body: {@code {"error":"DB connect timeout","message":"Timeout while
   * connecting to ..."}}.
   *
   * @param request the current {@link HttpRequest} (may be {@code null} in some exceptional flows)
   * @param e the {@link R2dbcTimeoutException} thrown by the R2DBC driver
   * @return an {@link HttpResponse} with status {@link HttpStatus#GATEWAY_TIMEOUT} and a small map
   *     describing the error
   */
  @Override
  public HttpResponse<?> handle(HttpRequest request, R2dbcTimeoutException e) {
    return HttpResponse.status(HttpStatus.GATEWAY_TIMEOUT)
        .body(java.util.Map.of("error", "DB connect timeout", "message", e.getMessage()));
  }
}
