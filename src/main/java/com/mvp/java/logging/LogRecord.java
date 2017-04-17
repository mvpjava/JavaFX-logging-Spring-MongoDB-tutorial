package com.mvp.java.logging;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Used in Mapping the Log record from Java Object to Mongo DBObject and back.
 */
@Document
public class LogRecord {
    @Id private String mongoId;
    private String level;
    private String logger;
    private String thread;
    private String message;
    private String exception;
    private List<String> stacktrace;

    private Date timestamp;

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    @Override
    public String toString() {
        return "LogRecord{" + "mongoId=" + mongoId + ", level=" + level + ", logger=" + logger + ", thread=" + thread + ", message=" + message + ", exception=" + exception + ", stacktrace=" + stacktrace + ", timestamp=" + timestamp + '}';
    }
    
}
