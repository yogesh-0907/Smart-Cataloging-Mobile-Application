import java.util.ArrayList;

public class Artisan {

    int artisanId;
    String name;
    String location;
    String contact;
    int age;
    String experience;
    ArrayList<Product> products = new ArrayList<>();

    Artisan(int artisanId,String name, String location, String contact, int age, String experience) {
        this.artisanId = artisanId; 
        this.name = name;
        this.location = location;
        this.contact = contact;
        this.age = age;
        this.experience = experience;
    }

    @Override
    public String toString() {
        return "Artisan Id : " + artisanId + "\nName : " + name + "\nLocation : " + location + "\nContact : " + contact + "\nAge : " +
                age + "\nExperience : " + experience;
    }

}
