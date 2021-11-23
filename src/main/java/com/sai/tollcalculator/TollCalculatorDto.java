package com.sai.tollcalculator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TollCalculatorDto {
    private String from;
    private String to;
    private Double distance;
    private Double toll;
}