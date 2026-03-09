
## 1. Tổng quan
Các bước thực hiện demo CRUD với MySQL + JPA + Spring Boot + Thymeleaf.

---

## 2. Quy trình thực hiện
1. **Tạo Project Spring Boot** với các dependency cần thiết:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <scope>runtime</scope>
   </dependency>
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-thymeleaf</artifactId>
   </dependency>
   ```

2. **Cấu hình cơ sở dữ liệu** trong `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/javadb
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

3. **Tạo Entity trong package `model`**:

- Product.java
```java
@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String image;

    @Min(value = 0, message = "Giá sản phẩm không được âm")
    private Double price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
```

- Category.java
```java
@Data
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
}
```

---

## 4. Tạo Repository trong package `repository`

- ProductRepository.java
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> { }
```

- CategoryRepository.java
```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> { }
```

---

## 5. Tạo Service trong package `service`

- ProductService.java
```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }
}
```

- CategoryService.java
```java
@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public void deleteCategory(int id) {
        categoryRepository.deleteById(id);
    }
}
```

---

## 6. Tạo Controller trong package `controller`

- ProductController.java
```java
@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "product/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/add";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/add";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
```

---

## 7. Tạo Layout và Content page trong Thymeleaf (`templates`)

- `_layout.html` (layout chung)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Demo Product</title>
</head>
<body>
<div th:fragment="content">
    <div th:replace="${content}"></div>
</div>
</body>
</html>
```

- `list.html` (`templates/product/list.html`)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:replace="layout :: content">
<body>
<h2>Danh sách sản phẩm</h2>
<a th:href="@{/products/add}">Thêm mới</a>
<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Price</th>
            <th>Category</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="product : ${products}">
            <td th:text="${product.id}"></td>
            <td th:text="${product.name}"></td>
            <td th:text="${product.price}"></td>
            <td th:text="${product.category.name}"></td>
            <td>
                <a th:href="@{|/products/edit/${product.id}|}">Edit</a>
                <a th:href="@{|/products/delete/${product.id}|}">Delete</a>
            </td>
        </tr>
    </tbody>
</table>
</body>
</html>
```

- `add.html` (`templates/product/add.html`)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:replace="layout :: content">
<body>
<h2>Thêm/Sửa sản phẩm</h2>
<form th:action="@{/products/save}" th:object="${product}" method="post">
    <input type="hidden" th:field="*{id}"/>
    <div>
        <label>Name:</label>
        <input type="text" th:field="*{name}" />
    </div>
    <div>
        <label>Price:</label>
        <input type="number" th:field="*{price}" />
    </div>
    <div>
        <label>Category:</label>
        <select th:field="*{category}" th:object="${categories}">
            <option th:each="cat : ${categories}" th:value="${cat}" th:text="${cat.name}"></option>
        </select>
    </div>
    <div>
        <button type="submit">Save</button>
    </div>
</form>
</body>
</html>
```

### 8. Hoàn tất
- Chạy Spring Boot application.
- Truy cập `http://localhost:8080/products` để xem danh sách sản phẩm và thực hiện CRUD.

---

### Cách chạy (điều kiện)
1. **MySQL** phải đang chạy tại `localhost:3306`.
2. Tạo database: `CREATE DATABASE javadb;`
3. Trong `application.properties` đảm bảo đúng user/password MySQL (mặc định: `root` / password rỗng).
4. Chạy ứng dụng: `mvn spring-boot:run` (hoặc chạy class `ProductValidationApplication` từ IDE).
5. Mở trình duyệt: **http://localhost:8081** hoặc **http://localhost:8081/products**.

