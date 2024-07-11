package az.CartEr.Service;

import az.CartEr.DTO.ProductDTO;
import az.CartEr.Model.ProductModel;
import az.CartEr.Model.User;
import az.CartEr.repository.GetProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class Product {
    @Autowired
    private GetProductRepository productRepository;

    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }
    public Optional<ProductModel> getProductById(int id) {
        return productRepository.findById(id);
    }
    public List<ProductModel> getNotesByUser(Optional<User> user) {
        return productRepository.findByUser(user);
    }
    public ProductModel createNoteForUser(ProductModel note, User user) {
        note.setUser(user);
        return productRepository.save(note);
    }
    public boolean deleteNoteById(Long noteId, String userEmail) {
        // Fetch the note by ID
        ProductModel note = productRepository.findById(Math.toIntExact(noteId)).orElse(null);

        if (note != null && note.getUser().getEmail().equals(userEmail)) {
            productRepository.deleteById(Math.toIntExact(noteId));
            return true;
        }
        return false;
    }

    public ProductModel updateNoteById(Long noteId, ProductDTO noteDTO, String userEmail) {
        // Fetch the note by ID
        ProductModel note = productRepository.findById(Math.toIntExact(noteId)).orElse(null);

        if (note != null && note.getUser().getEmail().equals(userEmail)) {
            // Update the note fields
            note.setTitle(noteDTO.getTitle());
            note.setDescription(noteDTO.getDescription());
            note.setCategory(noteDTO.getCategory());
            note.setPicture(noteDTO.getPicture());
            note.setLocation(noteDTO.getLocation());
            note.setPrice(noteDTO.getPrice());
            note.setType(noteDTO.getType());
            // Save the updated note
            return productRepository.save(note);
        }

        return null;
    }
}
