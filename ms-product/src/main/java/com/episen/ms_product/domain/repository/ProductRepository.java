package com.episen.ms_product.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.episen.ms_product.domain.entity.Product;
import com.episen.ms_product.domain.enumerate.Category;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    public List<Product> findByName(String name);

    public List<Product> findByCategory(Category category);

    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    public List<Product> findAvailable();

    @Query("SELECT COUNT(p) FROM Product p")
    public long count();

    long countByStockLessThan(int amount);
}
