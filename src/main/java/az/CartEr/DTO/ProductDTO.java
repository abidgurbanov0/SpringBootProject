package az.CartEr.DTO;

public class ProductDTO {

    private Long id;
    private String title;
    private String description;
    private String picture;
    private String category;
    private String price;
    private String location;
    private String type;

    // Constructors
    public ProductDTO(Long id, String title, String description, String picture, String category, String price, String location, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.category = category;
        this.price = price;
        this.location = location;
        this.type = type;
    }

    public ProductDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
