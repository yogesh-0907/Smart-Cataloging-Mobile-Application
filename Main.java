public class Main {

    public static void main(String args[]) {
        ArtisanService artisanService = new ArtisanService();

        Artisan artisan = new Artisan(
                101,
                "Yogesh",
                "Bheemili",
                "9059XXXXXX",
                20,
                "2 years of experience"
        );

        artisanService.addArtisan(artisan);

        artisan.products.add(
                new Product(
                        503,
                        "Basket",
                        "Bamboo",
                        99.99,
                        "Hand-Made",
                        "Vegetable Basket",
                        "This is an hand made bamboo vegetable basket",
                        artisan
                )
        );

        artisan.products.add(
                new Product(
                        502,
                        "Flower Pot",
                        "Clay",
                        149.99,
                        "Hand-Made",
                        "Pot",
                        "This is a hand made clay flower pot",
                        artisan
                )
        );

        artisan.products.add(
                new Product(
                        501,
                        "Chair",
                        "Cane",
                        799.99,
                        "Hand-Made",
                        "Chair",
                        "This is a hand made cane chair",
                        artisan
                )
        );

        System.out.println("Artisan details : \n" + artisan);

        for (int i = 0; i < artisan.products.size(); i++) {
            System.out.println("\nProduct Added Successfully");
            System.out.println("\n" + artisan.products.get(i));
        }

        Artisan foundArtisan = artisanService.getArtisanById(101);
        System.out.println("\nArtisan Found : \n" + foundArtisan);
    }
}