package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.dto.UserProfileUpdateDto;
import com.example.movierec.entity.User;
import com.example.movierec.entity.UserMovieFavorite;
import com.example.movierec.repository.UserRepository;
import com.example.movierec.repository.UserMovieFavoriteRepository;
import com.example.movierec.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMovieFavoriteRepository userMovieFavoriteRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/{userId}/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(
            @PathVariable Integer userId,
            @RequestParam("avatar") MultipartFile file) {

        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            String avatarUrl = "/uploads/" + filename;

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);

            return ApiResponse.success(result);

        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ApiResponse<Map<String, Object>> getUserInfo(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("age", user.getAge());
        userInfo.put("gender", user.getGender());
        userInfo.put("registrationDate", user.getRegistrationDate());
        userInfo.put("status", user.getStatus());

        return ApiResponse.success(userInfo);
    }

    @GetMapping("/{userId}/favorites")
    public ApiResponse<List<Map<String, Object>>> getUserFavorites(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<UserMovieFavorite> favorites = userMovieFavoriteRepository.findByUserOrderByAddedAtDesc(user);

        List<Map<String, Object>> favoriteMovies = favorites.stream().map(favorite -> {
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("movieId", favorite.getMovie().getMovieId());
            movieData.put("title", favorite.getMovie().getTitle());
            movieData.put("posterUrl", favorite.getMovie().getPosterUrl());
            movieData.put("releaseDate", favorite.getMovie().getReleaseDate());
            movieData.put("avgRating", favorite.getMovie().getAvgRating());
            movieData.put("addedAt", favorite.getAddedAt());
            return movieData;
        }).collect(Collectors.toList());

        return ApiResponse.success(favoriteMovies);
    }

    @GetMapping("/{userId}/search-history")
    public ApiResponse<List<String>> getUserSearchHistory(@PathVariable Integer userId) {
        List<String> history = searchService.getUserSearchHistory(userId);
        return ApiResponse.success(history);
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<Map<String, Object>> getUserProfile(@PathVariable Integer userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", user.getUserId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("age", user.getAge());
            profile.put("gender", user.getGender());
            profile.put("avatarUrl", user.getAvatarUrl());
            profile.put("birthday", user.getBirthday());

            return ApiResponse.success(profile);
        } catch (Exception e) {
            return ApiResponse.error("获取用户资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户资料 - 只更新提供的非空字段
     */
    @PutMapping("/{userId}/profile")
    public ApiResponse<String> updateUserProfile(
            @PathVariable Integer userId,
            @RequestBody UserProfileUpdateDto updates) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 记录更新的字段数
            int updatedCount = 0;

            // 只更新非空且不为空字符串的字段
            if (updates.getUsername() != null && !updates.getUsername().trim().isEmpty()) {
                // 检查用户名是否已存在（排除当前用户）
                if (userRepository.existsByUsername(updates.getUsername().trim()) &&
                        !user.getUsername().equals(updates.getUsername().trim())) {
                    return ApiResponse.error("用户名已存在");
                }
                user.setUsername(updates.getUsername().trim());
                updatedCount++;
            }

            if (updates.getEmail() != null && !updates.getEmail().trim().isEmpty()) {
                // 检查邮箱是否已存在（排除当前用户）
                if (userRepository.existsByEmail(updates.getEmail().trim()) &&
                        !updates.getEmail().trim().equals(user.getEmail())) {
                    return ApiResponse.error("邮箱已存在");
                }
                user.setEmail(updates.getEmail().trim());
                updatedCount++;
            }

            if (updates.getPhoneNumber() != null && !updates.getPhoneNumber().trim().isEmpty()) {
                user.setPhoneNumber(updates.getPhoneNumber().trim());
                updatedCount++;
            }

            if (updates.getAge() != null && updates.getAge() > 0) {
                user.setAge(updates.getAge());
                updatedCount++;
            }

            if (updates.getGender() != null && !updates.getGender().trim().isEmpty()) {
                user.setGender(updates.getGender().trim());
                updatedCount++;
            }

            if (updates.getBirthday() != null && !updates.getBirthday().trim().isEmpty()) {
                user.setBirthday(updates.getBirthday().trim());
                updatedCount++;
            }

            // 头像URL通常不通过这个接口更新，而是通过单独的头像上传接口
            // 但如果需要支持，可以添加：
            // if (updates.getAvatarUrl() != null && !updates.getAvatarUrl().trim().isEmpty()) {
            //     user.setAvatarUrl(updates.getAvatarUrl().trim());
            //     updatedCount++;
            // }

            if (updatedCount == 0) {
                return ApiResponse.error("没有有效的更新数据");
            }

            userRepository.save(user);

            return ApiResponse.success("资料更新成功，共更新了 " + updatedCount + " 个字段");

        } catch (Exception e) {
            System.err.println("更新用户资料失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("资料更新失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{userId}/password")
    public ApiResponse<String> changePassword(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request) {

        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ApiResponse.error("旧密码不能为空");
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ApiResponse.error("新密码不能为空");
            }

            if (newPassword.length() < 6) {
                return ApiResponse.error("新密码长度不能少于6位");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证旧密码
            if (!user.getPassword().equals(oldPassword.trim())) {
                return ApiResponse.error("旧密码错误！");
            }

            // 检查新旧密码是否相同
            if (oldPassword.trim().equals(newPassword.trim())) {
                return ApiResponse.error("新密码不能与旧密码相同");
            }

            user.setPassword(newPassword.trim());
            userRepository.save(user);

            return ApiResponse.success("密码修改成功！");

        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("密码修改失败: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/search-history")
    public ApiResponse<String> saveSearchHistory(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request) {
        String query = request.get("query");
        searchService.saveSearchHistory(userId, query);
        return ApiResponse.success("搜索历史保存成功");
    }

    @DeleteMapping("/{userId}/search-history")
    public ApiResponse<String> clearSearchHistory(@PathVariable Integer userId) {
        searchService.clearUserSearchHistory(userId);
        return ApiResponse.success("搜索历史清空成功");
    }
}