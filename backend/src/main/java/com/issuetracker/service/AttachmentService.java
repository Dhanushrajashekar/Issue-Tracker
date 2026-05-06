package com.issuetracker.service;

import com.issuetracker.model.Attachment;
import com.issuetracker.model.Issue;
import com.issuetracker.model.User;
import com.issuetracker.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

// Handles file uploads and downloads.
// In dev: files are stored in the "uploads/" directory next to the JAR.
// In prod: this can be swapped to upload to AWS S3 instead.
@Service
public class AttachmentService {

    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private IssueService issueService;
    @Autowired private UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public Attachment uploadFile(Long issueId, MultipartFile file, String uploaderEmail) throws IOException {
        Issue issue = issueService.findById(issueId);
        User uploader = userService.findByEmail(uploaderEmail);

        // Create the upload directory if it doesn't exist yet
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Use a UUID as the filename so two files with the same name don't overwrite each other
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath);

        Attachment attachment = new Attachment();
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setStoredName(storedName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setIssue(issue);
        attachment.setUploader(uploader);

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getIssueAttachments(Long issueId) {
        Issue issue = issueService.findById(issueId);
        return attachmentRepository.findByIssue(issue);
    }

    // Returns the file path on disk so the controller can stream it to the client
    public Path getFilePath(String storedName) {
        return Paths.get(uploadDir).resolve(storedName);
    }

    public Attachment findById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public void deleteAttachment(Long attachmentId, String requesterEmail) throws IOException {
        Attachment attachment = findById(attachmentId);
        User requester = userService.findByEmail(requesterEmail);

        boolean isUploader = attachment.getUploader().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole().name().equals("ROLE_ADMIN");

        if (!isUploader && !isAdmin) {
            throw new RuntimeException("You can only delete your own attachments");
        }

        // Delete the actual file from disk
        Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredName());
        Files.deleteIfExists(filePath);

        attachmentRepository.delete(attachment);
    }
}
