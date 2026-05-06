package com.issuetracker.controller;

import com.issuetracker.dto.ApiResponse;
import com.issuetracker.model.Attachment;
import com.issuetracker.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    @Autowired private AttachmentService attachmentService;

    // Upload a file to an issue
    @PostMapping("/issues/{issueId}/attachments")
    public ResponseEntity<?> upload(@PathVariable Long issueId,
                                    @RequestParam("file") MultipartFile file,
                                    Authentication auth) {
        try {
            Attachment attachment = attachmentService.uploadFile(issueId, file, auth.getName());
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "File upload failed: " + e.getMessage()));
        }
    }

    // Get all attachments for an issue
    @GetMapping("/issues/{issueId}/attachments")
    public ResponseEntity<List<Attachment>> getAttachments(@PathVariable Long issueId) {
        return ResponseEntity.ok(attachmentService.getIssueAttachments(issueId));
    }

    // Download a file — serves the raw file bytes so the browser can open/save it
    @GetMapping("/files/{storedName}")
    public ResponseEntity<Resource> download(@PathVariable String storedName) {
        try {
            Path filePath = attachmentService.getFilePath(storedName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        try {
            attachmentService.deleteAttachment(id, auth.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Attachment deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
