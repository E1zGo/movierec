package com.example.movierec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户资料更新DTO（数据传输对象）
 * 用于接收前端发送的用户资料更新请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDto {
    private String username;
    private String email;
    private String phoneNumber;
    private Integer age;
    private String gender;
    private String avatarUrl;
    private String birthday;
}