package rnd.dev.student_info.vericle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rnd.dev.student_info.config.MongoDBConnection;
import rnd.dev.student_info.constant.ApplicationConstant;
import rnd.dev.student_info.record.Student;

import java.util.List;

public class StudentMessageReceiverVerticle extends AbstractVerticle {
  private MongoClient mongoClient;
  private static final Logger logger = LoggerFactory.getLogger(StudentMessageReceiverVerticle.class);

  @Override
  public void start() {
    mongoClient = MongoClient.createShared(vertx, MongoDBConnection.dbConnection());
    vertx.eventBus().consumer(ApplicationConstant.STUDENT_SAVE_MESSAGE_ADDRESS, this::saveStudent);
    vertx.eventBus().consumer(ApplicationConstant.STUDENT_VIEW_MESSAGE_ADDRESS, this::viewAllStudent);
    vertx.eventBus().consumer(ApplicationConstant.STUDENT_FIND_MESSAGE_ADDRESS, this::findStudent);
    vertx.eventBus().consumer(ApplicationConstant.STUDENT_EDIT_MESSAGE_ADDRESS, this::editStudent);
    vertx.eventBus().consumer(ApplicationConstant.STUDENT_DELETE_MESSAGE_ADDRESS, this::deleteStudent);
  }

  private void saveStudent(Message<Student> message) {
    Student messageReceived = message.body();
    logger.info("StudentMessageReceiverVerticle:: messageReceived ::{}", messageReceived);
    if (messageReceived != null) {
      mongoClient.save(ApplicationConstant.TABLE_NAME, messageReceived.toJson(), res -> {
        if (res.succeeded()) {
          message.reply(messageReceived + " has been saved successfully");
        } else {
          message.reply("Problem in saving data");
        }
      });

    } else {
      message.reply(null);
    }
  }

  private void viewAllStudent(Message<String> message) {
    mongoClient.find(ApplicationConstant.TABLE_NAME, new JsonObject(), res -> {
      if (res.succeeded()) {
        logger.info("StudentMessageReceiverVerticle:: viewAllUser ::{}", res.result());
        List<JsonObject> userList = res.result();
        message.reply(userList.toString());
      } else {
        logger.info("StudentMessageReceiverVerticle:: viewAllUser :: error", res.cause());
        message.reply(res.cause());
      }
    });
  }


  private void findStudent(Message<Student> message) {
    Student receivedStudent = message.body();

    JsonObject query = new JsonObject();
    if (receivedStudent.getName() != null) {
      query.put("name", receivedStudent.getName());
    }
    if (receivedStudent.getEmail() != null) {
      query.put("email", receivedStudent.getEmail());
    }

    mongoClient.find(ApplicationConstant.TABLE_NAME, query, res -> {
      if (res.succeeded()) {
        logger.info("StudentMessageReceiverVerticle:: findStudent :: {}", res.result());
        message.reply(res.result().toString());
      } else {
        logger.error("StudentMessageReceiverVerticle:: findStudent :: error", res.cause());
        message.reply(res.cause().getMessage());
      }
    });
  }



  private void editStudent(Message<Student> message) {
    Student updatedStudent = message.body();
    JsonObject query = new JsonObject().put("email", updatedStudent.getEmail());

    JsonObject updateFields = new JsonObject();
    if (updatedStudent.getName() != null) updateFields.put("name", updatedStudent.getName());
    if (updatedStudent.getAge() != 0) updateFields.put("age", updatedStudent.getAge());
    if (updatedStudent.getCgpa() != 0.0) updateFields.put("cgpa", updatedStudent.getCgpa());
    if (updatedStudent.getProfileCreatedAt() != null)
      updateFields.put("profileCreatedAt", updatedStudent.getProfileCreatedAt().toString());

    JsonObject update = new JsonObject().put("$set", updateFields);

    mongoClient.findOneAndUpdate(ApplicationConstant.TABLE_NAME, query, update, res -> {
      if (res.succeeded()) {
        logger.info("StudentMessageReceiverVerticle:: editStudent :: updated {}", res.result());
        message.reply("Information for " + updatedStudent.getEmail() + " updated successfully");
      } else {
        logger.error("StudentMessageReceiverVerticle:: editStudent :: error", res.cause());
        message.reply(res.cause());
      }
    });
  }



  private void deleteStudent(Message<Student> message) {
    Student messageReceived = (Student) message.body();
    JsonObject jsonObject = new JsonObject()
      .put("name", messageReceived.getName())
      .put("email", messageReceived.getEmail());


    mongoClient.findOneAndDelete(ApplicationConstant.TABLE_NAME, jsonObject, res -> {
      if (res.succeeded()) {
        logger.info("StudentMessageReceiverVerticle:: deleteStudent ::{}", res.result());
        message.reply("Data has been deleted successfully");
      } else {
        logger.info("StudentMessageReceiverVerticle:: deleteStudent ::{}", res.cause());
        message.reply(res.cause());
      }

    });
  }

}
