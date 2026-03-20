# Hướng dẫn demo Spring Security (theo file `bai 6_ spring security.pdf`)

> Tài liệu này được viết lại theo hướng **dễ làm theo**, bám sát nội dung slide:
> - Giới thiệu Spring Security
> - Xác thực người dùng
> - Phân quyền ứng dụng
> - Phân quyền ứng dụng với CSDL

---

## 1. Mục tiêu demo

Sau khi hoàn thành, bạn sẽ làm được:

1. Thêm Spring Security vào dự án Spring Boot.
2. Hiểu cơ chế đăng nhập mặc định của Spring Security.
3. Tạo xác thực người dùng bằng **in-memory user**.
4. Phân quyền theo endpoint với vai trò `USER`, `ADMIN`.
5. Tích hợp xác thực + phân quyền bằng **CSDL** với các bảng `account`, `role`, `account_role`.

---

## 2. Tổng quan Spring Security

Spring Security là framework bảo mật cho ứng dụng Spring, hỗ trợ:

- **Authentication**: xác thực người dùng
- **Authorization**: phân quyền truy cập
- Bảo vệ trước các tấn công phổ biến như:
  - CSRF
  - Session Fixation
  - XSS
- Hỗ trợ nhiều cơ chế như JWT, OAuth2, LDAP...

### 2.1. Cơ chế hoạt động

Spring Security hoạt động dựa trên **Filter Chain**.  
Khi client gửi request, request sẽ đi qua các bộ lọc bảo mật để:

1. Kiểm tra đăng nhập
2. Kiểm tra quyền truy cập
3. Kiểm tra các chính sách bảo mật khác

### 2.2. Các thành phần chính

- `DelegatingFilterProxy`
- `SecurityFilterChain`
- `AuthenticationManager`
- `AuthenticationProvider`
- `UserDetailsService`
- `PasswordEncoder`
- `SecurityContextHolder`
- `FilterSecurityInterceptor`

---

## 3. Bước 1 - Thêm dependency Spring Security

Nếu dùng Maven, thêm dependency sau vào `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 3.1. Hành vi mặc định sau khi thêm dependency

Ngay khi thêm Spring Security vào project:

- Tất cả endpoint sẽ bị yêu cầu đăng nhập
- Spring Security tự sinh form login mặc định
- Username mặc định là:

```text
user
```

- Password mặc định sẽ được in ra trong **console**

Ví dụ log:

```text
Using generated security password: 1f2b3c4d-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

> Đây là bước kiểm tra nhanh để xác nhận Spring Security đã hoạt động.

---

## 4. Bước 2 - Demo xác thực người dùng bằng in-memory

Phần này bám sát ví dụ trong slide: tạo 2 user:

- `user / 123456`
- `admin / admin123`

Và tạo endpoint `/home` để kiểm tra đăng nhập thành công.

### 4.1. Tạo controller kiểm tra đăng nhập

Tạo file `HomeController.java`:

```java
package com.example.demo.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/home")
    public String home(Principal principal) {
        return "Hello, " + principal.getName();
    }
}
```

### 4.2. Tạo `SecurityConfig` cho xác thực in-memory

Tạo file `SecurityConfig.java`:

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("123456"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/home").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```

### 4.3. Chạy thử

1. Chạy ứng dụng
2. Truy cập:

```text
http://localhost:8080/home
```

3. Hệ thống sẽ chuyển đến trang login mặc định
4. Đăng nhập bằng:
   - `user / 123456`
   - hoặc `admin / admin123`
5. Nếu thành công, kết quả trả về:

```text
Hello, user
```

hoặc:

```text
Hello, admin
```

---

## 5. Bước 3 - Phân quyền ứng dụng theo endpoint

Trong slide, ví dụ phân quyền như sau:

| Endpoint | Quyền truy cập |
|---|---|
| `/products` | `USER`, `ADMIN` |
| `/products/add` | `ADMIN` |
| `/products/edit/**` | `ADMIN` |
| `/products/delete/**` | `ADMIN` |
| `/order` | `USER` |

> Nghĩa là:
> - Người dùng thường (`USER`) chỉ xem danh sách sản phẩm, đặt hàng
> - Quản trị viên (`ADMIN`) được thêm/sửa/xóa sản phẩm

### 5.1. Cập nhật `SecurityConfig`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("123456"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/products/add", "/products/edit/**", "/products/delete/**")
                    .hasRole("ADMIN")
                .requestMatchers("/products")
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/order")
                    .hasRole("USER")
                .anyRequest()
                    .authenticated()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```

### 5.2. Kết quả mong đợi

- Đăng nhập bằng `admin`
  - vào được `/products`
  - vào được `/products/add`
  - vào được `/products/edit/...`
  - vào được `/products/delete/...`

- Đăng nhập bằng `user`
  - vào được `/products`
  - vào được `/order`
  - **không** vào được `/products/add`, `/products/edit/...`, `/products/delete/...`

---

## 6. Bước 4 - Tích hợp Spring Security với CSDL

Phần này là nội dung quan trọng nhất trong slide: không dùng user hard-code nữa mà đọc tài khoản từ DB.

### 6.1. Mô hình dữ liệu

Tạo 3 bảng:

- `account`
- `role`
- `account_role`

### 6.2. Gợi ý cấu trúc bảng

```sql
CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login_name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE account_role (
    account_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_account_role_account
        FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_account_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
);
```

### 6.3. Lưu ý về role

Theo nội dung slide:

- Trong DB có thể lưu role là:
  - `ADMIN`
  - `USER`

- Nhưng khi map sang Spring Security thì thường phải chuyển thành:
  - `ROLE_ADMIN`
  - `ROLE_USER`

Cách xử lý đơn giản nhất là trong `AccountService`, khi tạo quyền cho user thì thêm prefix `ROLE_`.

---

## 7. Bước 5 - Tạo entity `Account` và `Role`

### 7.1. `Role.java`

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<Account> users = new HashSet<>();
}
```

### 7.2. `Account.java`

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "login_name", nullable = false, unique = true)
    private String loginName;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "account_role",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

---

## 8. Bước 6 - Tạo `AccountRepository`

Tạo file `AccountRepository.java` trong package `repository`:

```java
package com.example.demo.repository;

import com.example.demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByLoginName(String loginName);
}
```

> Repository này dùng để tìm tài khoản theo `login_name` khi người dùng đăng nhập.

---

## 9. Bước 7 - Tạo `AccountService` implements `UserDetailsService`

Đây là bước kết nối dữ liệu tài khoản trong DB với Spring Security.

Tạo file `AccountService.java`:

```java
package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByLoginName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user: " + username));

        Set<SimpleGrantedAuthority> authorities = account.getRoles().stream()
                .map(Role::getName)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toSet());

        return User.builder()
                .username(account.getLoginName())
                .password(account.getPassword())
                .authorities(authorities)
                .build();
    }
}
```

### 9.1. Ý nghĩa của đoạn code trên

- Tìm `Account` theo `loginName`
- Lấy toàn bộ role của account
- Chuyển mỗi role thành `GrantedAuthority`
- Trả về đối tượng `UserDetails` cho Spring Security dùng khi xác thực

---

## 10. Bước 8 - Cấu hình bảo mật dùng dữ liệu từ DB

Bây giờ thay cấu hình in-memory bằng cấu hình đọc từ DB.

Tạo/ghi đè file `SecurityConfig.java` như sau:

```java
package com.example.demo.config;

import com.example.demo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountService accountService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(accountService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/products/add", "/products/edit/**", "/products/delete/**")
                    .hasRole("ADMIN")
                .requestMatchers("/products")
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/order")
                    .hasRole("USER")
                .anyRequest()
                    .authenticated()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/products", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }
}
```

---

## 11. Bước 9 - Tạo dữ liệu mẫu trong DB

Vì `password` phải được mã hóa bằng BCrypt, bạn có 2 cách:

### Cách A - Tự tạo hash bằng Java rồi insert vào DB

Ví dụ tạo class test nhanh:

```java
package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("123456 -> " + encoder.encode("123456"));
        System.out.println("admin123 -> " + encoder.encode("admin123"));
    }
}
```

Chạy class này, copy kết quả hash rồi insert vào DB.

Ví dụ SQL:

```sql
INSERT INTO role(name) VALUES ('ADMIN');
INSERT INTO role(name) VALUES ('USER');

INSERT INTO account(login_name, password)
VALUES ('admin', '$2a$10$YOUR_BCRYPT_HASH_FOR_admin123');

INSERT INTO account(login_name, password)
VALUES ('user1', '$2a$10$YOUR_BCRYPT_HASH_FOR_123456');

INSERT INTO account_role(account_id, role_id) VALUES (1, 1); -- admin -> ADMIN
INSERT INTO account_role(account_id, role_id) VALUES (2, 2); -- user1 -> USER
```

### Cách B - Seed dữ liệu bằng `CommandLineRunner` (dễ hơn)

Tạo file `DataInitializer.java`:

```java
package com.example.demo.config;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);

                Role userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("USER").orElseThrow();

            if (accountRepository.findByLoginName("admin").isEmpty()) {
                Account admin = new Account();
                admin.setLoginName("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                accountRepository.save(admin);
            }

            if (accountRepository.findByLoginName("user1").isEmpty()) {
                Account user = new Account();
                user.setLoginName("user1");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRoles(Set.of(userRole));
                accountRepository.save(user);
            }
        };
    }
}
```

Nếu dùng cách này, bạn cần thêm `RoleRepository`.

### 11.1. `RoleRepository.java`

```java
package com.example.demo.repository;

import com.example.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
```

---

## 12. Bước 10 - Kiểm tra kết quả

### 12.1. Kiểm tra đăng nhập

Truy cập:

```text
http://localhost:8080/products
```

Kết quả mong đợi:

1. Nếu chưa đăng nhập -> hệ thống chuyển sang trang login
2. Đăng nhập bằng tài khoản trong bảng `account`
3. Nếu đúng username/password -> đăng nhập thành công

### 12.2. Kiểm tra phân quyền

#### Đăng nhập bằng `admin`
- Truy cập được `/products`
- Truy cập được `/products/add`
- Truy cập được `/products/edit/...`
- Truy cập được `/products/delete/...`

#### Đăng nhập bằng `user1`
- Truy cập được `/products`
- Không truy cập được `/products/add`
- Không truy cập được `/products/edit/...`
- Không truy cập được `/products/delete/...`

> Đây chính là kết quả mà slide mô tả ở phần cuối: truy cập `/products` thì phải đăng nhập; nếu là tài khoản có quyền phù hợp thì thao tác được, ngược lại sẽ bị chặn.

---

## 13. Cấu trúc package gợi ý

```text
src/main/java/com/example/demo
├── config
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller
│   └── HomeController.java
├── model
│   ├── Account.java
│   ├── Role.java
│   ├── Product.java
│   └── Category.java
├── repository
│   ├── AccountRepository.java
│   ├── RoleRepository.java
│   ├── ProductRepository.java
│   └── CategoryRepository.java
├── service
│   ├── AccountService.java
│   ├── ProductService.java
│   └── CategoryService.java
└── DemoApplication.java
```

---

## 14. Checklist để chạy demo thành công

- [ ] Đã thêm `spring-boot-starter-security`
- [ ] Đã cấu hình MySQL đúng trong `application.properties`
- [ ] Đã có bảng `account`, `role`, `account_role`
- [ ] Password trong DB là **BCrypt hash**
- [ ] `AccountService` implements `UserDetailsService`
- [ ] `SecurityConfig` dùng `DaoAuthenticationProvider`
- [ ] Đã cấu hình quyền cho `/products`, `/products/add`, `/products/edit/**`, `/products/delete/**`, `/order`

---

## 15. `application.properties` mẫu

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/demoproduct
spring.datasource.username=root
spring.datasource.password=123456

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 16. Tổng kết

Trong bài này, bạn đi theo đúng 3 lớp kiến thức chính:

1. **Giới thiệu Spring Security**
   - Hiểu Spring Security là gì
   - Biết vai trò của filter chain
   - Biết các thành phần quan trọng

2. **Xác thực người dùng**
   - Demo đăng nhập mặc định
   - Tạo user in-memory
   - Tạo endpoint `/home` để kiểm tra đăng nhập

3. **Phân quyền ứng dụng**
   - Phân quyền theo endpoint với `USER` và `ADMIN`
   - Tích hợp xác thực/phân quyền với CSDL thông qua `Account`, `Role`, `AccountService`

---

## 17. Luồng làm bài gọn nhất nếu bạn muốn thực hành nhanh

1. Lấy project bài 5 (quản lý sản phẩm)
2. Thêm dependency Spring Security
3. Chạy thử để thấy form login mặc định
4. Tạo `SecurityConfig` với 2 user in-memory
5. Phân quyền endpoint `/products`, `/products/add`, `/products/edit/**`, `/products/delete/**`
6. Tạo bảng `account`, `role`, `account_role`
7. Tạo entity `Account`, `Role`
8. Tạo `AccountRepository`, `RoleRepository`
9. Tạo `AccountService implements UserDetailsService`
10. Cập nhật `SecurityConfig` để dùng DB
11. Insert tài khoản mẫu và test lại

> Sau khi hoàn tất 11 bước trên, bạn sẽ có demo Spring Security gần như đầy đủ theo đúng nội dung của file bài 6.
