package com.example.module.travel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 酒店实体类
 */
@Data
@AllArgsConstructor
public class Hotel {

    private String name;

    private String location;

    private int price;

     double rating;
}
