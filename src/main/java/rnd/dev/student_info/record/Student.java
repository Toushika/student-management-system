package rnd.dev.student_info.record;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {
  private String name;
  private int age;
  private String email;
  private double cgpa;
  private LocalDate profileCreatedAt;

  public static Student fromJsonObject(JsonObject jsonObject) {
    return Student.builder()
      .name(jsonObject.getString("name", null))
      .age(jsonObject.getInteger("age", 0))
      .email(jsonObject.getString("email", null))
      .cgpa(jsonObject.getDouble("cgpa", 0.0))
      .build();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("name", this.name);
    jsonObject.put("age", this.age);
    jsonObject.put("email", this.email);
    jsonObject.put("cgpa", this.cgpa);
    jsonObject.put("profileCreatedAt", profileCreatedAt != null ? profileCreatedAt.toString() : LocalDate.now().toString());
    return jsonObject;
  }

}
