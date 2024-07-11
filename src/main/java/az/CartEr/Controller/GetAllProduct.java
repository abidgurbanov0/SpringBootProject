package az.CartEr.Controller;

import az.CartEr.DTO.GetAllProductDto;
import az.CartEr.Model.ProductModel;
import az.CartEr.Service.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class GetAllProduct {

    @Autowired
    private Product productService;

    @GetMapping("products/all")
    public List<GetAllProductDto> getAllProducts() {
        List<ProductModel> products = productService.getAllProducts();
        return products.stream().map(this::convertToProductDTO).collect(Collectors.toList());
    }
    @GetMapping("products/{id}")
    public ResponseEntity<GetAllProductDto> getProductById(@PathVariable int id) {
        Optional<ProductModel> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            GetAllProductDto productDTO = convertToProductDTO(productOptional.get());
            return ResponseEntity.ok(productDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private GetAllProductDto convertToProductDTO(ProductModel product) {
        GetAllProductDto productDTO = new GetAllProductDto();
        productDTO.setId(product.getId());
        productDTO.setTitle(product.getTitle());
        productDTO.setDescription(product.getDescription());
        productDTO.setPicture(product.getPicture());
        productDTO.setCategory(product.getCategory());
        productDTO.setPrice(product.getPrice());
        productDTO.setLocation(product.getLocation());
        productDTO.setType(product.getType());
        if (product.getUser() != null) {
            productDTO.setUserEmail(product.getUser().getEmail());
        }
        return productDTO;
    }

}
