package com.gmavrommatis.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Pure unit test for {@code PetClinicController} (no Micronaut context).
 *
 * <p>Goal: verify that the controller
 *
 * <ol>
 *   <li>calls {@link VetService#getVetsWithSpecialties(Pageable)} with the expected {@link
 *       Pageable} (page/size)
 *   <li>wraps the service response into an {@code HttpResponse} with status 200 and the pagination
 *       headers ("page", "size")
 * </ol>
 *
 * <p>Tools used:
 *
 * <ul>
 *   <li>{@link MockitoExtension} to create/inject the Mockito mock
 *   <li>{@link StepVerifier} (reactor-test) to assert the reactive {@code Mono}
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
class PetClinicControllerTest {

  @Mock VetService vetService;
  @InjectMocks PetClinicController controller;

  /**
   * Asserts that the controller maps the service result to a 200 OK response and sets "page" and
   * "size" headers to the values passed by the caller.
   */
  @Test
  void petClinicDetails_mapsResponseAndHeaders() {
    log.info("petClinicDetails_mapsResponseAndHeaders execution");
    // Arrange
    int page = 1, size = 5;

    // Use a concrete instance (not a Mockito mock) unless you only compare by identity;
    // here we assert identity so a mock is OK.
    PetClinicResponse mockBody = org.mockito.Mockito.mock(PetClinicResponse.class);

    // Expect the service to be called with the same Pageable (page/size) the controller receives
    when(vetService.getVetsWithSpecialties(argThat(pageable(page, size))))
        .thenReturn(Mono.just(mockBody));

    // Act + Assert (reactively)
    StepVerifier.create(controller.petClinicDetails(page, size))
        .assertNext(
            resp -> {
              assertEquals(200, resp.getStatus().getCode(), "status");
              assertEquals(String.valueOf(page), resp.getHeaders().get("page"), "page header");
              assertEquals(String.valueOf(size), resp.getHeaders().get("size"), "size header");
              // Same instance we stubbed above (controller should pass through the body untouched)
              assertSame(mockBody, resp.body(), "response body instance");
            })
        .verifyComplete();
  }

  /**
   * Builds a Mockito {@link ArgumentMatcher} for {@link Pageable} that checks both number and size.
   */
  private ArgumentMatcher<Pageable> pageable(int page, int size) {
    return p -> p != null && p.getNumber() == page && p.getSize() == size;
  }
}
