# Basic Exception Handling and Validation

## I. Các exceptions có thể xảy ra:


### Trùng `username` trong khi khởi tạo mới một `user`.

```java
if (userRepository.existsByUsername(request.getUsername())) {
        throw new RuntimeException("Username existed.");
    }
```

### Trùng `email` trong khi khởi tạo mới `user`.

```java
if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email existed.");
        }
```

### Không tìm thấy `User`:

`userId` nhập vào để tìm kiếm trong database không tồn tại -> không tìm thấy `user`:

```java
public User getUserById(String userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
}
```

## II. Các validation trong quá trình khởi tạo hoặc thay đổi `user`:

### `username` quá ngắn, dưới 3 kí tự.

```java
public class UserCreationRequest {
    @Size(min = 3, message = "username must be at least 3 characters.")
    private String username;

    // Các field khác
}
```

### `password` quá ngắn, dưới 8 kí tự.

```java
public class UserCreationRequest {

    @Size(min = 8, message = "password must be at least 8 character")
    private String password;

    // Các field khác
}
```

```java
public class UserUpdateRequest {

    @Size(min = 8, message = "password must be at least 8 character")
    private String password;

    // Các field khác
}
```

## III. Vấn đề:

Mặc dù đã ném ra `RuntimeException` nếu trong trường hợp không tìm thấy `User` với `id` truyền vào sẽ hiển thị message là `"User not found."`.

Tuy nhiên khi thử gửi một `id` không đúng:
`localhost:8082/identity/users/2ascasc`

Thì response trả về vẫn chỉ là mặc định, không rõ ràng:
```json
{
    "timestamp": "2024-05-12T15:28:41.055+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/identity/users/2ascasc"
}
```

Response trả về lỗi cần tường minh, rõ ràng để có thể biết được lỗi nằm ở đâu.

## IV. Giải quyết:

### Exception:
Cần phải có một class chuyên xử lý Exception của toàn bộ project với annotation `@ControllerAdvice`.

Ta sẽ tạo ra một package `exception`, trong đó chứa class `GlobalExceptionHandler`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    //Xử lý những RuntimeException
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<String> handlingRuntimeException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

}
```

Khi này kết quả trả về sẽ là message mà ta mong muốn:

```bash
User not found.
```

### Validation:

Khi khởi tạo hoặc thay đổi thông tin `User`, ta cần phải validate thông tin từ các request để đảm bảo đúng với yêu cầu của hệ thống.
Ví dụ: `password` thì cần phải trên 8 kí tự, `username` thì cần phải tối thiểu 3 kí tự, v.v...

Trước hết cần phải có dependency của `validation` trong file `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Giờ ta thêm annotation `@Size` cùng với các tham số (trong trường hợp yêu cầu về số lượng kí tự):


#### `username` quá ngắn, dưới 3 kí tự.

```java
public class UserCreationRequest {
    @Size(min = 3, message = "username must be at least 3 characters.")
    private String username;

    // Các field khác
}
```

#### `password` quá ngắn, dưới 8 kí tự.

```java
public class UserCreationRequest {

    @Size(min = 8, message = "password must be at least 8 character")
    private String password;

    // Các field khác
}
```

```java
public class UserUpdateRequest {

    @Size(min = 8, message = "password must be at least 8 character")
    private String password;

    // Các field khác
}
```

Thêm annotation `@Valid` vào tham số `request` cần được validate trong class `UserController`:

```java
    //Create
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        User createUser = userService.createUser(userCreationRequest);
        return new ResponseEntity<User>(createUser, HttpStatus.CREATED);
    }

    //Update
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        User updateUser = userService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(updateUser);
    }
```

Giờ ta thử gửi một request khởi tạo, với mật khẩu ít hơn 8 kí tự:
```json
{
    "username": "user999",
    "password": "1234",
    "firstName": "Jane",
    "lastName": "Smith",
    "dob": "1985-05-15"
}
```

Thì sẽ nhận được respone:
```json
{
    "timestamp": "2024-05-12T16:10:25.724+00:00",
    "status": 400,
    "error": "Bad Request",
    "path": "/identity/users"
}
```

Giờ thì ta vào terminal để xem lỗi là gì:
```bash
2024-05-12T23:10:25.703+07:00  WARN 15928 --- [nio-8082-exec-3] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public org.springframework.http.ResponseEntity<com.example.basichandlingexceptionvalidation.entity.User> com.example.basichandlingexceptionvalidation.controller.UserController.createUser(com.example.basichandlingexceptionvalidation.dto.request.UserCreationRequest): [Field error in object 'userCreationRequest' on field 'password': rejected value [1234]; codes [Size.userCreationRequest.password,Size.password,Size.java.lang.String,Size]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [userCreationRequest.password,password]; arguments []; default message [password],2147483647,8]; default message [password must be at least 8 character]] ]
```

Ta có thể thấy message ta viết ở trên có xuất hiện ở `default message [password must be at least 8 character]]`, tuy nhiên ta cần một respone trả về lỗi và message này, vậy thì ta sẽ thêm một phương thức xử lý các lỗi liên quan đến `MethodArgumentNotValidException`:

```java
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<String> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(exception.getFieldError().getDefaultMessage());
    }
```

Và kết quả:

```json
password must be at least 8 character
```