package az.CartEr.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"user"})
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String picture;
    private String category;
    private String price;
    private String location;
    private String type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
