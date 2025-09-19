    // src/main/java/com/example/movierec/dto/CommentRequestDto.java

    package com.example.movierec.dto;

    import com.fasterxml.jackson.annotation.JsonProperty;
    import lombok.Data;

    @Data
    public class CommentRequestDto {
        private Integer movieId;
        private Integer userId;
        private String content;

        @JsonProperty("parentId")
        private Integer parentId;
    }