package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "http://localhost:8081")
public class SupportController {

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SupportController() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * DTO: FAQ
     * 为了简化，这里使用内部类代替，实际项目中推荐使用专门的DTO类。
     */
    private class FaqDto {
        public String question;
        public String answer;

        public FaqDto(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    /**
     * GET /api/support/faqs
     * 获取常见问题列表。
     *
     * @return 包含常见问题和答案的列表。
     */
    @GetMapping("/faqs")
    public ApiResponse<List<FaqDto>> getFaqs() {
        List<FaqDto> faqs = new ArrayList<>();
        faqs.add(new FaqDto("如何注册账户？", "你可以在主页上找到注册按钮，填写必要信息即可创建账户。"));
        faqs.add(new FaqDto("忘记密码怎么办？", "在登录页面，点击'忘记密码'链接，输入你的注册邮箱，我们将发送重置密码的指引给你。"));
        faqs.add(new FaqDto("如何修改个人资料？", "登录后，进入个人主页，点击'编辑资料'按钮即可修改你的昵称、邮箱、电话、年龄、性别等信息。"));
        faqs.add(new FaqDto("如何上传或更换头像？", "在个人资料编辑页面，点击你的头像区域，选择一张新图片上传即可。请确保图片大小不超过5MB。"));
        faqs.add(new FaqDto("为什么我无法上传头像？", "请检查图片格式是否为JPG、PNG等常见格式，并确认文件大小是否超出限制（5MB）。如果问题持续，可能是网络原因，请稍后再试。"));
        faqs.add(new FaqDto("我的收藏电影去哪里了？", "你的所有收藏电影都会在个人主页的'收藏'标签下显示，你可以通过搜索功能快速查找。"));
        faqs.add(new FaqDto("如何取消收藏一部电影？", "进入电影详情页，如果电影已在你的收藏列表中，收藏按钮会显示为已收藏状态，再次点击即可取消。"));
        faqs.add(new FaqDto("为什么电影推荐不准确？", "我们的推荐算法会根据你的浏览历史和评分行为进行学习。多看、多评会使推荐结果越来越符合你的喜好。"));
        faqs.add(new FaqDto("如何联系人工客服？", "如果常见问题无法解决你的疑问，你可以尝试使用AI智能助手获取帮助，或稍后再次尝试。"));

        return ApiResponse.success(faqs);
    }

    /**
     * GET /api/support/config-test
     * 测试配置是否正确读取
     */
    @GetMapping("/config-test")
    public ApiResponse<Map<String, String>> testConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("apiKey", deepseekApiKey != null && !deepseekApiKey.trim().isEmpty() ? "已配置" : "未配置");
        config.put("apiUrl", deepseekApiUrl);
        config.put("keyLength", deepseekApiKey != null ? String.valueOf(deepseekApiKey.length()) : "0");
        return ApiResponse.success(config);
    }

    /**
     * POST /api/support/ai-chat
     * 处理 AI 智能助手聊天请求，接入DeepSeek API。
     * @param request 请求体，包含用户问题。
     * @return AI 助手的回复。
     */
    @PostMapping("/ai-chat")
    public ApiResponse<String> aiChat(@RequestBody Map<String, Object> request) {
        String userQuery = (String) request.get("query");

        if (userQuery == null || userQuery.trim().isEmpty()) {
            return ApiResponse.error("用户问题不能为空。");
        }

        try {
            // 调用 DeepSeek API
            String aiResponse = callDeepSeekApi(userQuery);
            return ApiResponse.success(aiResponse);
        } catch (Exception e) {
            // 记录详细的错误信息
            System.err.println("DeepSeek API调用失败: " + e.getMessage());
            e.printStackTrace();

            // 如果API调用失败，使用模拟回复作为后备
            String fallbackResponse = simulateAiResponse(userQuery);
            return ApiResponse.success(fallbackResponse + " (注：当前使用离线模式，错误: " + e.getMessage() + ")");
        }
    }

    /**
     * 调用 DeepSeek API 获取AI回复
     */
    private String callDeepSeekApi(String userQuery) throws Exception {
        // 检查API密钥
        if (deepseekApiKey == null || deepseekApiKey.trim().isEmpty() || "YOUR_DEEPSEEK_API_KEY".equals(deepseekApiKey)) {
            throw new RuntimeException("DeepSeek API key not configured properly. Current key: " +
                    (deepseekApiKey != null ? "***" + deepseekApiKey.substring(Math.max(0, deepseekApiKey.length() - 4)) : "null"));
        }

        System.out.println("准备调用DeepSeek API...");
        System.out.println("API URL: " + deepseekApiUrl);
        System.out.println("API Key length: " + deepseekApiKey.length());

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);

        // 构建系统提示和用户消息
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示，定义AI助手的角色和行为
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个电影推荐平台的智能客服助手。请友好、专业地回答用户关于平台使用的问题。" +
                "主要帮助用户解决注册、登录、密码重置、个人资料修改、头像上传、电影收藏、推荐算法等相关问题。" +
                "回答要简洁明了，语气友善。如果遇到超出平台功能范围的问题，请礼貌地告知用户并引导他们提出相关问题。");
        messages.add(systemMessage);

        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userQuery);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        // 创建请求实体
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        System.out.println("请求体: " + requestBodyJson);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson, headers);

        // 发送请求
        System.out.println("发送请求到DeepSeek API...");
        ResponseEntity<String> response = restTemplate.exchange(
                deepseekApiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        System.out.println("收到响应: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody());

        // 解析响应
        JsonNode responseNode = objectMapper.readTree(response.getBody());

        // 检查是否有错误
        if (responseNode.has("error")) {
            JsonNode errorNode = responseNode.get("error");
            String errorMessage = errorNode.has("message") ? errorNode.get("message").asText() : "Unknown error";
            throw new RuntimeException("DeepSeek API error: " + errorMessage);
        }

        JsonNode choices = responseNode.get("choices");

        if (choices != null && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                String content = message.get("content").asText();
                System.out.println("AI回复: " + content);
                return content;
            }
        }

        throw new RuntimeException("Invalid response format from DeepSeek API: " + response.getBody());
    }

    /**
     * 模拟 AI 模型的回复 (后备方案)
     * 当DeepSeek API不可用时使用
     */
    private String simulateAiResponse(String query) {
        String lowerCaseQuery = query.toLowerCase();

        if (lowerCaseQuery.contains("忘记密码")) {
            return "如果你忘记了密码，请在登录页面点击'忘记密码'链接，通过注册邮箱来重置。";
        } else if (lowerCaseQuery.contains("如何注册")) {
            return "你可以在主页找到注册按钮，填写邮箱、用户名和密码即可注册。";
        } else if (lowerCaseQuery.contains("修改资料") || lowerCaseQuery.contains("个人信息")) {
            return "你可以进入个人主页，点击'编辑资料'按钮来修改个人信息，包括昵称、邮箱、电话、年龄、性别等。";
        } else if (lowerCaseQuery.contains("联系客服")) {
            return "目前你正在使用AI智能助手服务。如果问题无法解决，你可以尝试使用不同的关键词来提问。";
        } else if (lowerCaseQuery.contains("头像")) {
            return "在个人资料编辑页面，点击你的头像区域，选择一张新图片上传即可。请确保图片大小不超过5MB。";
        } else if (lowerCaseQuery.contains("收藏电影")) {
            return "你的所有收藏电影都会在个人主页的'收藏'标签下显示。你也可以在搜索框中快速查找。";
        } else {
            return "这个问题超出了我的能力范围，请尝试描述得更具体一些。";
        }
    }
}