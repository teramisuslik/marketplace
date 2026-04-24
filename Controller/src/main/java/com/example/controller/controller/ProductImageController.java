package com.example.controller.controller;

import com.example.controller.DTO.Role;
import com.example.controller.client.UserClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class ProductImageController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final UserClient userClient;

    @Value("${app.upload.dir:${user.home}/.marketplace-uploads}")
    private String uploadDir;

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping(value = "/seller/product_image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadProductImage(
            @RequestHeader("Authorization") String token, @RequestPart("file") MultipartFile file) {
        if (userClient.getRole(token) != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sellers can upload images");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }
        String original = file.getOriginalFilename();
        String ext = extensionOf(original);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed: jpg, png, gif, webp");
        }
        String storedName = UUID.randomUUID() + "." + ext;
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize().resolve("products");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
        return Map.of("relativeUrl", "/api/files/products/" + storedName);
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        return ext;
    }
}
