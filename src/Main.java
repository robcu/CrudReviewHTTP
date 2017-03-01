import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static HashMap<String, ArrayList<GroceryItem>> allGroceries = new HashMap<>();
    static HashMap<String, User> users = new HashMap<>();

    public static ArrayList<GroceryItem> unpackAllGroceries(){
        ArrayList<GroceryItem> tempArray = new ArrayList<>();
        ArrayList<GroceryItem> fullList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<GroceryItem>> entry : allGroceries.entrySet()) {
            tempArray = entry.getValue();
            fullList.addAll(tempArray);
        }
        return fullList;
    }

    public static void main(String[] args) {
        Spark.init();

        Spark.get("/", (request, response) -> {
                    Session session = request.session();
                    String accountName = session.attribute("accountName");
                    User user = users.get(accountName);
                    String pWord = session.attribute("password");
                    HashMap m = new HashMap();

                    m.put("allGroceries", unpackAllGroceries());

                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {

                        if (user.password.contentEquals(pWord)) {
                            m.put("groceries", user.groceries);
                            return new ModelAndView(m, "groceries.html");
                        } else {
                            return new ModelAndView(m, "login.html");
                        }
                    }
                },
                new MustacheTemplateEngine()
        );

        Spark.post("/createUser", (request, response) -> {
            String name = request.queryParams("acctName");
            String passWord = request.queryParams("passWord");

            users.putIfAbsent(name, new User(name, passWord));

            Session session = request.session();
            session.attribute("accountName", name);
            session.attribute("password", passWord);

            response.redirect("/");
            return "";
        });

        Spark.post("/create-groceryItem", (request, response) -> {
            String name = request.queryParams("groceryName");
            int quantity = Integer.parseInt(request.queryParams("groceryQuantity"));
            String dept = request.queryParams("groceryDept");
            Session session = request.session();
            User user = users.get(session.attribute("accountName"));
            GroceryItem groceryItem = new GroceryItem(name, quantity, dept, user.groceries.size());

            user.groceries.add(groceryItem);
            allGroceries.put(user.name, user.groceries);


            response.redirect("/");
            return "";
        });

        Spark.get("/edit-groceryItem", (request, response) -> {
                    Session session = request.session();
                    String accountName = session.attribute("accountName");
                    User user = users.get(accountName);
                    int id = Integer.parseInt(request.queryParams("Index"));
                    GroceryItem groceryItem = user.groceries.get(id);

                    HashMap m = new HashMap();
                    m.put("groceryItem", groceryItem);
                    return new ModelAndView(m, "edit.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post("/edit-groceryItem", (request, response) -> {
            Session session = request.session();
            String name = request.queryParams("groceryName");
            int quantity = Integer.parseInt(request.queryParams("groceryQuantity"));
            String dept = request.queryParams("groceryDept");
            int index = Integer.parseInt(request.queryParams("Index"));

            User user = users.get(session.attribute("accountName"));

            GroceryItem groceryItem = new GroceryItem(name, quantity, dept, index);
            user.groceries.set(index, groceryItem);

            allGroceries.put(user.name, user.groceries);

            response.redirect("/");
            return "";
        });

        Spark.get("/delete-groceryItem", (request, response) -> {
            Session session = request.session();
            int index = Integer.parseInt(request.queryParams("DeleteIndex"));
            User user = users.get(session.attribute("accountName"));
            user.groceries.remove(index);

            allGroceries.put(user.name, user.groceries);

            response.redirect("/");
            return "";
        });

        Spark.post("/logout", (request, response) -> {
            Session session = request.session();
            session.invalidate();

            response.redirect("/");
            return "";
        });
    }
}
