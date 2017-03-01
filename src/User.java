import java.util.ArrayList;

public class User {
    String name;
    String password;
    ArrayList<GroceryItem> groceries = new ArrayList<GroceryItem>();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
