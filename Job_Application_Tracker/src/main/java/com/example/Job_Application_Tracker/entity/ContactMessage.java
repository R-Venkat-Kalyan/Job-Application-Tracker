package com.example.Job_Application_Tracker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contact_messages")
public class ContactMessage {

    @Id
    private String id;

    private String name;
    private String email;
    private String topic;     // Feedback / Issue / Feature / Other
    private String subject;
    private String message;

    public ContactMessage() {}

    public ContactMessage(String name, String email, String topic, String subject, String message) {
        this.name = name;
        this.email = email;
        this.topic = topic;
        this.subject = subject;
        this.message = message;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { this.email = email; }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) { this.topic = topic; }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) { this.message = message; }
}