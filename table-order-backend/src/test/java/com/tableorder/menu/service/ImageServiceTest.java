package com.tableorder.menu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageServiceTest {

    private ImageService imageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageService = new ImageService();
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());
    }

    // UT-IMG-001: JPG 이미지 업로드 성공
    @Test
    @DisplayName("유효한 JPG 파일을 업로드하면 이미지 URL을 반환한다")
    void uploadJpgSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[1024]);

        String url = imageService.upload(file);

        assertThat(url).startsWith("/api/images/").endsWith(".jpg");
    }

    // UT-IMG-002: PNG 이미지 업로드 성공
    @Test
    @DisplayName("유효한 PNG 파일을 업로드하면 이미지 URL을 반환한다")
    void uploadPngSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[2048]);

        String url = imageService.upload(file);

        assertThat(url).startsWith("/api/images/").endsWith(".png");
    }

    // UT-IMG-003: 5MB 초과 파일 업로드 거부
    @Test
    @DisplayName("5MB를 초과하는 파일 업로드 시 예외가 발생한다")
    void rejectOversizedFile() {
        byte[] oversized = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", oversized);

        assertThatThrownBy(() -> imageService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");
    }

    // UT-IMG-004: 허용되지 않은 파일 형식 거부
    @Test
    @DisplayName("허용되지 않은 파일 형식(GIF)은 업로드가 거부된다")
    void rejectInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "anim.gif", "image/gif", new byte[1024]);

        assertThatThrownBy(() -> imageService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPG, PNG");
    }
}
