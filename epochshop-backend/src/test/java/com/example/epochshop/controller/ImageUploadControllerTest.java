package com.example.epochshop.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    @InjectMocks
    private ImageUploadController imageUploadController;

    @Test
    void uploadImage_ShouldReturnImageUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        ResponseEntity<Map<String, String>> response = imageUploadController.uploadImage(file);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsKey("imageUrl");
        // 修改为实际返回的格式
        assertThat(response.getBody().get("imageUrl")).startsWith("http://localhost:8080/api/images/");
    }

    @Test
    void uploadImage_EmptyFile_ShouldReturnBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        ResponseEntity<Map<String, String>> response = imageUploadController.uploadImage(file);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).containsEntry("error", "請選擇檔案");
    }
}