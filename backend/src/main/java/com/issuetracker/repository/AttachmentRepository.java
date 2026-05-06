package com.issuetracker.repository;

import com.issuetracker.model.Attachment;
import com.issuetracker.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // All files attached to an issue
    List<Attachment> findByIssue(Issue issue);

    // Look up by the stored filename on disk (used for serving/deleting the file)
    Optional<Attachment> findByStoredName(String storedName);
}
