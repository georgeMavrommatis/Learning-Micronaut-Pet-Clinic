package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.PetClinicService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for fetching Pet Clinic details.
 *
 * @author GewrgiosMmavrommatis
 */
@Controller("/pet-clinic")
@Slf4j
public class PetClinicController {

  private final PetClinicService petClinicService;

  public PetClinicController(PetClinicService petClinicService) {
    this.petClinicService = petClinicService;
  }

  /**
   * Retrieves basic Pet Clinic details.
   *
   * @return the {@link PetClinicResponse}
   */
  @Get("/details")
  public HttpResponse<PetClinicResponse> petClinicDetails() {

    String threadName = Thread.currentThread().getName();
    String pool = threadName.contains("nioEventLoopGroup") ? "EVENT-LOOP" : "WORKER";
    log.info("→ executed on {}", pool);
    return HttpResponse.ok(petClinicService.getPetClinicDetails());
  }
}
