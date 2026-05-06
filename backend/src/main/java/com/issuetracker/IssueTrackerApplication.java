package com.issuetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// @EnableAsync lets us use @Async on methods — used for sending emails in the background
// so the HTTP response doesn't wait for the email to finish sending
@SpringBootApplication
@EnableAsync
public class IssueTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(IssueTrackerApplication.class, args);
    }
}
