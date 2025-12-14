package com.ecommerce.order.infrastructure.client;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean active;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String description, BigDecimal price, 
                      Integer stock, String category, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getCategory() { return category; }
    public Boolean getActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setCategory(String category) { this.category = category; }
    public void setActive(Boolean active) { this.active = active; }

    public static ProductDTOBuilder builder() { return new ProductDTOBuilder(); }

    public static class ProductDTOBuilder {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private String category;
        private Boolean active;

        public ProductDTOBuilder id(Long id) { this.id = id; return this; }
        public ProductDTOBuilder name(String name) { this.name = name; return this; }
        public ProductDTOBuilder description(String description) { this.description = description; return this; }
        public ProductDTOBuilder price(BigDecimal price) { this.price = price; return this; }
        public ProductDTOBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductDTOBuilder category(String category) { this.category = category; return this; }
        public ProductDTOBuilder active(Boolean active) { this.active = active; return this; }

        public ProductDTO build() { return new ProductDTO(id, name, description, price, stock, category, active); }
    }
}
