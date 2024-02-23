package com.springboot.webflux.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PredictionRequest {

    private String contents;
}
