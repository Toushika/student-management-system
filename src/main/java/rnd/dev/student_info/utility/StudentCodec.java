package rnd.dev.student_info.utility;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import rnd.dev.student_info.record.Student;

public class StudentCodec implements MessageCodec<Student, Student> {
  @Override
  public void encodeToWire(Buffer buffer, Student student) {
    // Encode the User object as a JSON string and put it into the buffer
    String json = student.toJson().encode();
    buffer.appendInt(json.length());  // Append the length of the string (for deserialization purposes)
    buffer.appendString(json);        // Append the string itself
  }

  @Override
  public Student decodeFromWire(int position, Buffer buffer) {
    // Decode the buffer to a JsonObject and then convert it to a User object
    int length = buffer.getInt(position);
    String json = buffer.getString(position + 4, position + 4 + length);
    return Student.fromJsonObject(new JsonObject(json));
  }

  @Override
  public Student transform(Student student) {
    return student;  // No transformation, just return the user as is
  }

  @Override
  public String name() {
    return "studentCodec";  // Name for the codec
  }

  @Override
  public byte systemCodecID() {
    return -1;  // Return -1 for custom codecs
  }
}
