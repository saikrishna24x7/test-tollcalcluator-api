package com.sai.tollcalculator;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/toll")
@RequiredArgsConstructor
public class TollCalculatorController {

    private final TollCalculatorService tollCalculatorService;

    @GetMapping("/calculate")
    public TollCalculatorDto calculateToll(@RequestParam("from") String from, @RequestParam("to") String to) {
        return tollCalculatorService.calculateToll(from, to);
    }
}
