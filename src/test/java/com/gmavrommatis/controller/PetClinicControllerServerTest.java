package com.gmavrommatis.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.gmavrommatis.config.r2dbc.repository.VetRepository;
import com.gmavrommatis.config.r2dbc.repository.VetSpecialtyRepository;
import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.model.response.VetResponse;
import com.gmavrommatis.service.SpecialtyService;
import com.gmavrommatis.service.VetService;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.r2dbc.spi.R2dbcTimeoutException;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * End-to-end(ish) tests for {@link PetClinicController} that exercise two distinct runtime
 * variants, switched per test via {@code @Property(name="test.variant", value=...)}:
 *
 * <ul>
 *   <li><b>Variant A</b> ({@code value="A"}): the real {@link VetService} is used, while {@link
 *       VetRepository} is mocked to emit a {@link R2dbcTimeoutException} on {@code
 *       findAllPaged(...)}. We drive the controller over HTTP and assert the application returns
 *       <em>504 Gateway Timeout</em> with the expected error body (as produced by the global R2DBC
 *       timeout handler).
 *   <li><b>Variant B</b> ({@code value="B"}): {@link VetService} is replaced by a Mockito mock (via
 *       {@link MockBean}). We stub it to return a material {@link PetClinicResponse}, invoke the
 *       HTTP endpoint, and assert a <em>200 OK</em> with expected headers and body fields.
 * </ul>
 *
 * <p>We set {@code rebuildContext = true} on {@link MicronautTest} so method-level {@link Property}
 * annotations rebuild the application context between tests, re-evaluating {@link Requires}-guarded
 * beans (our conditional {@code @MockBean}s).
 *
 * <p><b>Why only {@code VetRepository} is mocked in variant A:</b> once the repository emits a
 * {@link R2dbcTimeoutException}, the reactive pipeline fails early; code that would consult {@link
 * VetSpecialtyRepository} or {@link SpecialtyService} is never reached. Mocking them for this
 * variant is therefore unnecessary.
 */
@Slf4j
@MicronautTest(transactional = false, rebuildContext = true)
class PetClinicControllerServerTest {

  @Inject Environment env;

  @Inject
  @Client("/")
  HttpClient client;

  @Inject VetService vetService; // real for A, mocked for B
  @Inject VetRepository vetRepository; // always mocked
  @Inject VetSpecialtyRepository vetSpecialtyRepository; // always mocked
  @Inject SpecialtyService specialtyService; // always mocked

  /**
   * Provide a Mockito mock of {@link VetRepository} only for variant A.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>In variant B this mock is not active; if your test environment lacks a real R2DBC setup,
   *       keep this repository mocked in B as well (or avoid injecting it).
   *   <li>We avoid mocking {@code VetSpecialtyRepository} and {@code SpecialtyService} for A
   *       because the timeout short-circuits the flow before those are used.
   * </ul>
   */
  @MockBean(VetRepository.class)
  @Requires(property = "test.variant", value = "A")
  VetRepository vetRepositoryMock() {
    return Mockito.mock(VetRepository.class);
  }

  /*todo explain not needed vetSpecialtyRepositorydue to unreachable line of code when exception is thrown above*/
  /*@MockBean(VetSpecialtyRepository.class)
  @Requires(property="test.variant", value="A")
  VetSpecialtyRepository vetSpecRepoMock() { return Mockito.mock(VetSpecialtyRepository.class); }*/

  /*todo explain not needed SpecialtyService to unreachable line of code when exception is thrown above*/
  /*@MockBean(SpecialtyService.class)
  @Requires(property="test.variant", value="A")
  SpecialtyService specialtyServiceMock() { return Mockito.mock(SpecialtyService.class); }*/

  /**
   * Provide a Mockito mock of {@link VetService} only for variant B. The controller will use this
   * mock instead of the real service in that test.
   */
  @MockBean(VetService.class)
  @Requires(property = "test.variant", value = "B")
  VetService vetServiceMock() {
    return Mockito.mock(VetService.class);
  }

  /** Pre-built body returned by the mocked {@link VetService} in variant B. */
  int page = 0, size = 10;

  PetClinicResponse expected =
      PetClinicResponse.builder()
          .page(page)
          .size(size)
          .totalElements(42L)
          .totalPages(5)
          .vets(List.of(VetResponse.builder().firstName("TEST").lastName("TEST").build()))
          .build();

  /**
   * Per-test stubbing that depends on the active variant.
   *
   * <ul>
   *   <li><b>A</b>: Reset and stub the {@link VetRepository} mock to emit an {@link
   *       R2dbcTimeoutException} on {@code findAllPaged}. {@code countAll()} is stubbed for
   *       completeness but the flow fails early.
   *   <li><b>B</b>: Reset the {@link VetService} mock and stub {@link
   *       VetService#getVetsWithSpecialties(Pageable)} to return a material {@link
   *       PetClinicResponse} that can be serialized/deserialized over HTTP.
   * </ul>
   */
  @BeforeEach
  void setUp() {
    String variant = env.getProperty("test.variant", String.class).orElse("A");

    switch (variant) {
      case "A" -> { // only clear mockedBean that are Required for the property (variant) value ->
        // example here a vetService would fail
        /*clear stubbing and invocation history*/
        /*todo explain not needed vetSpecialtyRepositorydue,specialtyService to unreachable line of code when exception is thrown above*/
        Mockito.reset(vetRepository /*, vetSpecialtyRepository, specialtyService*/);
        // sanity: ensure this *is* a Mockito mock
        assert Mockito.mockingDetails(vetRepository).isMock();
        Mockito.when(vetRepository.findAllPaged(Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Flux.error(new R2dbcTimeoutException("simulated connect timeout")));
        Mockito.when(vetRepository.countAll()).thenReturn(Mono.just(1L));
        /*todo explain not needed due to unreachable line of code when exception is thrown above*/
        // Mockito.when(vetSpecialtyRepository.findByVetId(Mockito.anyLong())).thenReturn(Flux.empty());
        /*todo explain not needed due to unreachable line of code when exception is thrown above*/
        // Mockito.when(specialtyService.findById(Mockito.anyLong())).thenReturn(Mono.empty());

      }
      case "B" -> { // only clear mockedBean that are Required for the property (variant) value ->
        // example here a vetRepository would fail
        /*clear stubbing and invocation history*/
        Mockito.reset(vetService);

        Mockito.when(vetService.getVetsWithSpecialties(Mockito.any(Pageable.class)))
            .thenReturn(Mono.just(expected));
      }
      default -> throw new IllegalArgumentException("Unknown test.variant=" + variant);
    } // PetClinicControllerServerTest
  }

  /**
   * Variant A: drive the HTTP endpoint and assert that a repository timeout is translated to a
   * <b>504 Gateway Timeout</b> with the expected error payload by the global exception handler.
   *
   * <p>We use the non-throwing {@code exchange(req, Argument, Argument)} overload so we can inspect
   * the non-2xx response directly.
   */
  @Test
  @Property(name = "test.variant", value = "A")
  void petClinicDetails_timeout_withMockedVetRepository() {
    var req = HttpRequest.GET("/pet-clinic/details").basicAuth("u", "p");

    // Non-throwing because of class-level exception-on-error-status=false
    HttpResponse<String> resp =
        client.toBlocking().exchange(req, Argument.of(String.class), Argument.of(String.class));

    assertEquals(HttpStatus.GATEWAY_TIMEOUT, resp.getStatus());
    assertNotNull(resp.body());
    assertTrue(resp.body().contains("DB connect timeout"));
  }

  /**
   * Variant B: drive the HTTP endpoint with {@link VetService} mocked to return a material body,
   * assert a <b>200 OK</b>, paging headers, and equality of the core fields.
   *
   * <p>We build a concrete {@link PetClinicResponse} (not a Mockito mock) so it can serialize
   * through HTTP.
   */
  @Test
  @Property(name = "test.variant", value = "B")
  void petClinicDetails_ok_withMockedService() {

    HttpRequest<?> req = HttpRequest.GET("/pet-clinic/details").basicAuth("u", "p");

    HttpResponse<PetClinicResponse> resp =
        client.toBlocking().exchange(req, Argument.of(PetClinicResponse.class));

    assertEquals(HttpStatus.OK, resp.getStatus());
    assertEquals(String.valueOf(page), resp.getHeaders().get("page"));
    assertEquals(String.valueOf(size), resp.getHeaders().get("size"));

    PetClinicResponse body = resp.body();
    assertNotNull(body);
    // Don’t use assertSame — HTTP serializes/deserializes → different instance
    assertEquals(expected.getPage(), body.getPage());
    assertEquals(expected.getSize(), body.getSize());
    assertEquals(expected.getTotalElements(), body.getTotalElements());
    assertEquals(expected.getTotalPages(), body.getTotalPages());
    assertNotNull(body.getVets());
  }
}
