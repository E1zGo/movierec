// MovieFilterDTO.java
package com.example.movierec.dto;

import lombok.Data;

@Data
public class MovieFilterDTO {
    private String genres; // 前端传递的逗号分隔类型，如 "Action,Drama"
    private String year;   // 前端传递的年份段，如 "2020s", "2010s"
}
