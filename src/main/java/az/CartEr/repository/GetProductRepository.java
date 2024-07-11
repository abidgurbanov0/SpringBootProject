package az.CartEr.repository;

import az.CartEr.Model.ProductModel;
import az.CartEr.Model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GetProductRepository extends CrudRepository<ProductModel, Integer> {
    List<ProductModel> findByUser(Optional<User> user);
    List<ProductModel> findAll();

}
