package com.example.module.travel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TravelPlanResult {

    private Map<String, Object> data;

    private long duration;

    private boolean parallel;

}
