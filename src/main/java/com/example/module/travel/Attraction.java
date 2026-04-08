package com.example.module.travel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 景点实体类
 */
@Data
@AllArgsConstructor
public class Attraction {

    private String name;

    private String description;

    private int price;

    private double rating;

}
