package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.PetClinicService;
import com.gmavrommatis.utils.ThreadSelectionUtils;
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
  // @NonBlocking // is essentially a marker annotation telling Micronaut This method does not block
  // and is safe to run on the Netty event-loop.
  // @ExecuteOn(TaskExecutors.IO) // offload work on the dedicated blocking (I/O) thread-pool
  // instead of blocking Netty event-loop
  // @ExecuteOn(TaskExecutors.BLOCKING) // offload work on the dedicated blocking thread-pool
  // instead of blocking Netty event-loop
  // @ExecuteOn(TaskExecutors.VIRTUAL) // offload work on the dedicated blocking VIRTUAL thread-pool
  // instead of blocking Netty event-loop
  @Get("/details")
  public HttpResponse<PetClinicResponse> petClinicDetails() {

    ThreadSelectionUtils.logThreadName(Thread.currentThread().getName());
    return HttpResponse.ok(petClinicService.getPetClinicDetails());
  }
}
