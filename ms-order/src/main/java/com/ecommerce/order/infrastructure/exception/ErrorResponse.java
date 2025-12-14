package com.ecommerce.order.infrastructure.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    public ErrorResponse() {}

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setStatus(int status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setPath(String path) { this.path = path; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

    public static ErrorResponseBuilder builder() { return new ErrorResponseBuilder(); }

    public static class ErrorResponseBuilder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<FieldError> fieldErrors;

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ErrorResponseBuilder status(int status) { this.status = status; return this; }
        public ErrorResponseBuilder error(String error) { this.error = error; return this; }
        public ErrorResponseBuilder message(String message) { this.message = message; return this; }
        public ErrorResponseBuilder path(String path) { this.path = path; return this; }
        public ErrorResponseBuilder fieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, message, path, fieldErrors);
        }
    }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }

        public void setField(String field) { this.field = field; }
        public void setMessage(String message) { this.message = message; }

        public static FieldErrorBuilder builder() { return new FieldErrorBuilder(); }

        public static class FieldErrorBuilder {
            private String field;
            private String message;

            public FieldErrorBuilder field(String field) { this.field = field; return this; }
            public FieldErrorBuilder message(String message) { this.message = message; return this; }

            public FieldError build() { return new FieldError(field, message); }
        }
    }
}
