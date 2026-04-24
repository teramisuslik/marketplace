package com.example.serviceforproduct.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.serviceforproduct.DTO.ProductDTO;
import com.example.serviceforproduct.entity.Product;
import com.example.serviceforproduct.exception.NotFoundException;
import com.example.serviceforproduct.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .countOfProduct(10)
                .rating(4.5f)
                .sellerId(100L)
                .price(1990.0)
                .imageUrl("https://example.com/p.png")
                .build();

        productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setCountOfProduct(product.getCountOfProduct());
        productDTO.setRating(product.getRating());
        productDTO.setSellerId(product.getSellerId());
        productDTO.setPrice(product.getPrice());
        productDTO.setImageUrl(product.getImageUrl());
    }

    @Test
    void createProduct_ShouldSaveProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.createProduct(product);

        // Then
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAll_ShouldReturnListOfProductDTOs() {
        // Given
        List<Product> products = List.of(product);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDTO> result = productService.findAll();

        // Then
        assertThat(result).hasSize(1);
        ProductDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(product.getId());
        assertThat(dto.getName()).isEqualTo(product.getName());
        assertThat(dto.getDescription()).isEqualTo(product.getDescription());
        assertThat(dto.getCountOfProduct()).isEqualTo(product.getCountOfProduct());
        assertThat(dto.getRating()).isEqualTo(product.getRating());
        assertThat(dto.getSellerId()).isEqualTo(product.getSellerId());
        assertThat(dto.getPrice()).isEqualTo(product.getPrice());
        assertThat(dto.getImageUrl()).isEqualTo(product.getImageUrl());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findByWord_ShouldReturnMatchingProductDTOs() {
        // Given
        String keyword = "test";
        List<Product> products = List.of(product);
        when(productRepository.findByWord(keyword)).thenReturn(products);

        // When
        List<ProductDTO> result = productService.findByWord(keyword);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(product.getId());
        assertThat(result.get(0).getName()).isEqualTo(product.getName());
        verify(productRepository, times(1)).findByWord(keyword);
    }

    @Test
    void findById_ShouldReturnProduct_WhenExists() {
        // Given
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // When
        Product found = productService.findById(id);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void findById_ShouldReturnNull_WhenNotExists() {
        // Given
        Long id = 999L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Product found = productService.findById(id);

        // Then
        assertThat(found).isNull();
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void findByName_ShouldReturnProduct_WhenExists() {
        // Given
        String name = "Test Product";
        when(productRepository.findByName(name)).thenReturn(Optional.of(product));

        // When
        Product found = productService.findByName(name);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(name);
        verify(productRepository, times(1)).findByName(name);
    }

    @Test
    void findByName_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        String name = "Nonexistent";
        when(productRepository.findByName(name)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.findByName(name))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
        verify(productRepository, times(1)).findByName(name);
    }
}
