public class Product {
    int productId;
    String productName;
    String material;
    Double price;
    String category;
    String type;
    String description;
    Artisan artisan;
    Product(int productId,String productName, String material, Double price, String category, String type, String description,
            Artisan artisan) {
        this.productId = productId;
        this.productName = productName;
        this.material = material;
        this.price = price;
        this.category = category;
        this.type = type;
        this.description = description;
        this.artisan = artisan;
    }

    @Override
    public String toString() {
        return "\nProduct id : " + productId +
                "\nName : " + productName +
                "\nMaterial : " + material +
                "\nPrice : " + price +
                "\nCategory : " + category +
                "\nType : " + type +
                "\nDescription : " + description +
                "\nMade by : " + artisan.name +
                "\nLocation : " + artisan.location;
    }
}