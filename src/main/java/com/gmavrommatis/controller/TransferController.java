package com.gmavrommatis.controller;

import static io.micronaut.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.gmavrommatis.model.request.TransferRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller("/api")
public class TransferController {

  @Post("/transfer")
  public HttpResponse<?> transferMoneyToVet(@Body TransferRequest dto) {
    log.info("Money transferred");
    return HttpResponse.noContent();
  }

  @Post(value = "/transfer/form", consumes = APPLICATION_FORM_URLENCODED)
  public HttpResponse<?> transferMoneyToVetWithForm(@Body TransferRequest dto) {
    log.info("Money transferred");
    return HttpResponse.noContent();
  }
}
