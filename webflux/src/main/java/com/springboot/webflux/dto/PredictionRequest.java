package com.springboot.webflux.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionRequest {

    private String contents;
}
