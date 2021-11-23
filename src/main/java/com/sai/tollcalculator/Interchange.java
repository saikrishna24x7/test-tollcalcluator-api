package com.sai.tollcalculator;

import lombok.Data;

import java.util.List;

@Data
public class Interchange {
    private String name;
    private Route next;
    private List<Route> routes;
}