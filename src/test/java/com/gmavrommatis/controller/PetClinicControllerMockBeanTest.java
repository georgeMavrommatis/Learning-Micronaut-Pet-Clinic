package com.gmavrommatis.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.gmavrommatis.config.r2dbc.repository.VetRepository;
import com.gmavrommatis.config.r2dbc.repository.VetSpecialtyRepository;
import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.SpecialtyService;
import com.gmavrommatis.service.VetService;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.r2dbc.spi.R2dbcTimeoutException;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration-style test for {@link PetClinicController} that demonstrates two testing variants:
 *
 * <ul>
 *   <li><b>Variant A</b> — repository-level failures are mocked (VetRepository,
 *       VetSpecialtyRepository, SpecialtyService). The test asserts behaviour when the repository
 *       call times out.
 *   <li><b>Variant B</b> — the higher-level VetService is mocked. The test asserts successful
 *       controller behaviour when the service returns a valid {@code PetClinicResponse}.
 * </ul>
 *
 * <p>The class uses {@code @MicronautTest(transactional = false, rebuildContext = true)} so the
 * Micronaut context is started for tests. Mocks are provided using {@code @MockBean} and gated by
 * {@code @Requires(property = "test.variant", value = "...")} so we can switch behaviour per test
 * using {@code @Property(name = "test.variant", value = "...")}.
 */
@Slf4j
@MicronautTest(transactional = false, rebuildContext = true)
class PetClinicControllerMockBeanTest {

  // Micronaut environment used to read the test.variant property at runtime
  @Inject Environment env;

  // Test HTTP client (not used directly in the shown tests, but available for endpoint calls)
  @Inject
  @Client("/")
  HttpClient client;

  // Real controller under test. We exercise controller methods directly, bypassing HTTP layer here.
  @Inject PetClinicController controller; // real controller

  // Injected beans (some will be replaced by @MockBean depending on test.variant)
  @Inject VetService vetService; // real for variant A, mocked for variant B
  @Inject VetRepository vetRepository; // always mocked in variant A
  @Inject VetSpecialtyRepository vetSpecialtyRepository; // always mocked in variant A
  @Inject SpecialtyService specialtyService; // always mocked in variant A

  // ---------------------------------------------------------------------
  // Mock bean definitions
  // ---------------------------------------------------------------------

  /**
   * Mock replacement for {@link VetRepository} used for test variant A.
   *
   * <p>Gated with {@code @Requires(property = "test.variant", value = "A")} so the mock is only
   * created when the test sets {@code test.variant=A}.
   */
  @MockBean(VetRepository.class)
  @Requires(property = "test.variant", value = "A")
  VetRepository vetRepositoryMock() {
    return Mockito.mock(VetRepository.class);
  }

  /** Mock replacement for {@link VetSpecialtyRepository} used for test variant A. */
  @MockBean(VetSpecialtyRepository.class)
  @Requires(property = "test.variant", value = "A")
  VetSpecialtyRepository vetSpecRepoMock() {
    return Mockito.mock(VetSpecialtyRepository.class);
  }

  /** Mock replacement for {@link SpecialtyService} used for test variant A. */
  @MockBean(SpecialtyService.class)
  @Requires(property = "test.variant", value = "A")
  SpecialtyService specialtyServiceMock() {
    return Mockito.mock(SpecialtyService.class);
  }

  /**
   * Mock replacement for {@link VetService} used for test variant B. When {@code test.variant=B}
   * this mock will replace the real {@code VetService} bean.
   */
  @MockBean(VetService.class)
  @Requires(property = "test.variant", value = "B")
  VetService vetServiceMock() {
    return Mockito.mock(VetService.class);
  }

  // A mock response object returned by the mocked VetService in variant B
  private final PetClinicResponse body = Mockito.mock(PetClinicResponse.class);

  /**
   * Prepare mocks based on the active {@code test.variant}.
   *
   * <p>Variant A: configure repository mocks to simulate a timeout when fetching vets and to return
   * expected counts / empty specialty streams. Variant B: configure the VetService mock to return a
   * pre-created {@code PetClinicResponse}.
   */
  @BeforeEach
  void setUp() {
    // determine which variant is active (default to "A" if missing)
    String variant = env.getProperty("test.variant", String.class).orElse("A");

    switch (variant) {
      case "A" -> {
        // Reset mocks that exist for variant A. This clears prior stubbings and invocation history.
        Mockito.reset(vetRepository, vetSpecialtyRepository, specialtyService);

        // Sanity assertion: ensure the injected repository is actually a Mockito mock.
        assert Mockito.mockingDetails(vetRepository).isMock();

        // Simulate a repository-level timeout when fetching paged vets.
        Mockito.when(vetRepository.findAllPaged(Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Flux.error(new R2dbcTimeoutException("simulated connect timeout")));

        // Provide stable behaviour for count and specialties to keep controller logic predictable.
        Mockito.when(vetRepository.countAll()).thenReturn(Mono.just(1L));
        Mockito.when(vetSpecialtyRepository.findByVetId(Mockito.anyLong()))
            .thenReturn(Flux.empty());
        Mockito.when(specialtyService.findById(Mockito.anyLong())).thenReturn(Mono.empty());
      }
      case "B" -> {
        // Reset the VetService mock (variant B provides the mock)
        Mockito.reset(vetService);

        // When the controller delegates to vetService.getVetsWithSpecialties(...) return the mocked
        // PetClinicResponse wrapped in a Mono.
        Mockito.when(vetService.getVetsWithSpecialties(Mockito.any(Pageable.class)))
            .thenReturn(Mono.just(body));
      }
      default -> throw new IllegalArgumentException("Unknown test.variant=" + variant);
    }
  }

  /**
   * Variant A test: verify controller handles repository timeout by propagating the
   * R2dbcTimeoutException.
   *
   * <p>This test calls the controller method directly (returns {@code
   * Mono<HttpResponse<PetClinicResponse>>}) and expects the reactive pipeline to error with {@link
   * R2dbcTimeoutException}.
   */
  @Test
  @Property(name = "test.variant", value = "A")
  void petClinicDetails_timeout_withMockedVetRepository() {
    // controller.petClinicDetails returns Mono<HttpResponse<PetClinicResponse>>
    StepVerifier.create(controller.petClinicDetails(0, 10))
        .expectError(R2dbcTimeoutException.class)
        .verify();
  }

  /**
   * Variant B test: verify controller returns OK and the same body supplied by the mocked
   * VetService.
   *
   * <p>Assertions:
   *
   * <ul>
   *   <li>HTTP status code is 200
   *   <li>response headers include page/size
   *   <li>the response body instance is the same mock instance provided by the service
   * </ul>
   */
  @Test
  @Property(name = "test.variant", value = "B")
  void petClinicDetails_ok_withMockedService() {
    int page = 0, size = 10;

    StepVerifier.create(controller.petClinicDetails(page, size))
        .assertNext(
            resp -> {
              // HTTP-level assertions
              assertEquals(200, resp.getStatus().getCode());
              assertEquals(String.valueOf(page), resp.getHeaders().get("page"));
              assertEquals(String.valueOf(size), resp.getHeaders().get("size"));

              // Ensure the controller returned the exact body instance the mocked service produced
              assertSame(body, resp.body());
            })
        .verifyComplete();
  }
}
