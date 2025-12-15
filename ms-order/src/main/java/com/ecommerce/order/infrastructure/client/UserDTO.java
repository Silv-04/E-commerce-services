package com.ecommerce.order.infrastructure.client;

import java.math.BigDecimal;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public UserDTO() {}

    public UserDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

    public void setId(Long id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }

    public static UserDTOBuilder builder() { return new UserDTOBuilder(); }

    public static class UserDTOBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public UserDTOBuilder id(Long id) { this.id = id; return this; }
        public UserDTOBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserDTOBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserDTOBuilder email(String email) { this.email = email; return this; }

        public UserDTO build() { return new UserDTO(id, firstName, lastName, email); }
    }
}
