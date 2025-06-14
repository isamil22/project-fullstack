package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductListDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductSpecification productSpecification;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, List<MultipartFile> images) throws IOException {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));

        Product product = productMapper.toEntity(productDTO);
        product.setCategory(category);
        product.setBrand(productDTO.getBrand());
        product.setBestseller(productDTO.isBestseller());
        product.setNewArrival(productDTO.isNewArrival());

        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = saveImage(image);
                    imagePaths.add("/images/" + fileName);
                }
            }
            product.setImages(imagePaths);
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, List<MultipartFile> images) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setCategory(category);
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setBestseller(productDTO.isBestseller());
        existingProduct.setNewArrival(productDTO.isNewArrival());

        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = saveImage(image);
                    imagePaths.add("/images/" + fileName);
                }
            }
            existingProduct.setImages(imagePaths);
        }
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDTO(updatedProduct);
    }

    // --- MODIFIED START ---
    public Page<ProductListDTO> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Cannot find products for a non-existent category with ID: " + categoryId);
        }
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(this::convertToProductListDTO);
    }

    public Page<ProductListDTO> getBestsellers(Pageable pageable) {
        Page<Product> products = productRepository.findByBestsellerIsTrue(pageable);
        return products.map(this::convertToProductListDTO);
    }

    public Page<ProductListDTO> getNewArrivals(Pageable pageable) {
        Page<Product> products = productRepository.findByNewArrivalIsTrue(pageable);
        return products.map(this::convertToProductListDTO);
    }
    // --- MODIFIED END ---

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }


    public Page<ProductListDTO> getAllProducts(String search, BigDecimal minPrice, BigDecimal maxPrice, String brand, Boolean bestseller, Boolean newArrival, Long categoryId, Pageable pageable) {
        Specification<Product> spec = productSpecification.getProducts(search, minPrice, maxPrice, brand, bestseller, newArrival, categoryId);
        return productRepository.findAll(spec, pageable)
                .map(this::convertToProductListDTO);
    }

    private String saveImage(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();

        Path path = Paths.get(uploadDir + "/images/" + fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());
        return fileName;
    }

    public String saveImageAndGetUrl(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        String fileName = saveImage(image);
        return "/images/" + fileName;
    }

    // --- HELPER METHOD ADDED ---
    private ProductListDTO convertToProductListDTO(Product product) {
        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0)
                : null;

        return new ProductListDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                imageUrl,
                product.getBrand()
        );
    }
}