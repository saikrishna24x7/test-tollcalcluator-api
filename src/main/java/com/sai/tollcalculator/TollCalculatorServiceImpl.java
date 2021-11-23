package com.sai.tollcalculator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TollCalculatorServiceImpl implements TollCalculatorService {

    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Map<Long, Interchange> interchangesMap = new HashMap<>();
    Map<String, Long> idsMap = new HashMap<>();

    private final Resource interchangesFile;

    public TollCalculatorServiceImpl(@Value("classpath:/interchanges.json") Resource interchangesFile) {
        this.interchangesFile = interchangesFile;
    }

    @PostConstruct
    private void loadDataFromFile() {
        try {
            Map<String, Object> result = (Map<String, Object>) mapper.readValue(interchangesFile.getFile(), Map.class).get("locations");
            interchangesMap = result.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> Long.parseLong(entry.getKey()), entry -> getObject(entry.getValue())))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> updateNext(entry.getKey(), entry.getValue())));
            idsMap = interchangesMap.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getValue().getName(), Map.Entry::getKey));
        } catch (Exception e) {
            log.error("Error occurred:{}", e.getMessage());
        }
    }

    @Override
    public TollCalculatorDto calculateToll(String from, String to) {
        return TollCalculatorDto.builder()
                .from(from)
                .to(to)
                .distance(getTotalDistance(from, to))
                .toll(calculate(from, to))
                .build();
    }

    private double getTotalDistance(String fromInterchange, String toInterchange) {
        double distance = 0.0;
        if (idsMap.containsKey(fromInterchange) && idsMap.containsKey(toInterchange)) {
            long fromId = idsMap.get(fromInterchange);
            long toId = idsMap.get(toInterchange);
            if (fromId > toId) {
                long temp = fromId;
                fromId = toId;
                toId = temp;
            }
            for (long i=fromId; i<toId; i++) {
                if (interchangesMap.containsKey(i)) {
                    distance = distance + interchangesMap.get(i).getNext().getDistance();
                }
            }
        }
        return distance;
    }

    private double calculate(String fromInterchange, String toInterchange) {
        double distance = getTotalDistance(fromInterchange, toInterchange);
        double COST = 0.25;
        return new BigDecimal(distance * COST).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    private Interchange getObject(Object object) {
        try {
            return mapper.readValue(mapper.writeValueAsString(object), Interchange.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new Interchange();
    }

    private Interchange updateNext(long id, Interchange interchange) {
        Route next = interchange.getRoutes().stream().filter(route -> route.getToId() > id).findFirst().get();
        interchange.setNext(next);
        return interchange;
    }
}
