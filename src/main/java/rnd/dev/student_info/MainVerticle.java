package rnd.dev.student_info;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rnd.dev.student_info.constant.ApplicationConstant;
import rnd.dev.student_info.record.Student;
import rnd.dev.student_info.utility.StudentCodec;
import rnd.dev.student_info.vericle.StudentMessageReceiverVerticle;

import java.util.Arrays;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {

    vertx.eventBus().registerDefaultCodec(Student.class, new StudentCodec());

    RouterBuilder.create(vertx, "webroot/openapi.yaml")
      .onSuccess(routerBuilder -> {

        routerBuilder.operation("saveStudent").handler(this::handleSaveStudent);
        routerBuilder.operation("viewStudents").handler(this::handleViewStudents);
        routerBuilder.operation("findStudent").handler(this::handleFindStudent);
        routerBuilder.operation("editStudent").handler(this::handleEditStudent);
        routerBuilder.operation("deleteStudent").handler(this::handleDeleteStudent);

        routerBuilder.rootHandler(BodyHandler.create());
        routerBuilder.rootHandler(StaticHandler.create());

        Router router = routerBuilder.createRouter();

        vertx.createHttpServer()
          .requestHandler(router)
          .listen(8888)
          .onSuccess(server -> {
            startPromise.complete();
            System.out.println("HTTP server started on port 8888");
          })
          .onFailure(startPromise::fail);

        vertx.deployVerticle(new StudentMessageReceiverVerticle(), res -> {
          if (res.succeeded()) {
            logger.info("Receiver Verticle deployed");
          } else {
            logger.error("Deployment failed", res.cause());
          }
        });

      })
      .onFailure(err -> {
        logger.error("Failed to load OpenAPI spec", err);
        startPromise.fail(err);
      });
  }


  private void handleSaveStudent(RoutingContext routingContext) {
    JsonObject jsonObject = routingContext.body().asJsonObject();
    Student student = Student.fromJsonObject(jsonObject);
    if (student != null) {
      vertx.eventBus().request(ApplicationConstant.STUDENT_SAVE_MESSAGE_ADDRESS, student, res -> {
        if (res.succeeded()) {
          System.out.println("Reply received: " + res.result().body());
          routingContext.response().setStatusCode(200).end(res.result().body().toString());
        } else {
          System.out.println("Reply failed: " + res.cause());
          routingContext.response().setStatusCode(500).end(res.cause().getMessage());
        }
      });
    } else {
      routingContext.response().setStatusCode(400).end("Bad request : no json body");
    }
  }

  private void handleViewStudents(RoutingContext routingContext) {
    vertx.eventBus().request(ApplicationConstant.STUDENT_VIEW_MESSAGE_ADDRESS, "", res -> {
      if (res.succeeded()) {
        System.out.println("Reply received: " + res.result().body());
        res.result().body();
        routingContext.response().putHeader("Content-Type", "application/json").end(res.result().body().toString());
      } else {
        System.out.println("Reply failed: " + res.cause());
        routingContext.response().setStatusCode(500).end(res.cause().getMessage());
      }
    });
  }

  private void handleFindStudent(RoutingContext routingContext) {
    JsonObject json = routingContext.body().asJsonObject();
    Student student = Student.fromJsonObject(json);
    vertx.eventBus().request(ApplicationConstant.STUDENT_FIND_MESSAGE_ADDRESS, student, res -> {
      if (res.succeeded()) {
        routingContext.response().putHeader("Content-Type", "application/json").end(res.result().body().toString());
      } else {
        routingContext.response().setStatusCode(500).end(res.cause().getMessage());
      }
    });
  }

  private void handleEditStudent(RoutingContext routingContext) {
    JsonObject json = routingContext.body().asJsonObject();
    Student student = Student.fromJsonObject(json);
    vertx.eventBus().request(ApplicationConstant.STUDENT_EDIT_MESSAGE_ADDRESS, student, res -> {
      if (res.succeeded()) {
        routingContext.response().end(res.result().body().toString());
      } else {
        System.out.println("Reply failed: " + res.cause());
        routingContext.response().setStatusCode(500).end(res.cause().getMessage());
      }
    });
  }

  private void handleDeleteStudent(RoutingContext routingContext) {
    JsonObject json = routingContext.body().asJsonObject();
    Student student = Student.fromJsonObject(json);
    vertx.eventBus().request(ApplicationConstant.STUDENT_DELETE_MESSAGE_ADDRESS, student, res -> {
      if (res.succeeded()) {
        routingContext.response().setStatusCode(200).end(res.result().body().toString());
      } else {
        routingContext.response().setStatusCode(500).end(res.cause().getMessage());
      }
    });
  }

  public static void main(String[] args) {
    String[] params = Arrays.copyOf(args, args.length + 1);
    params[params.length - 1] = MainVerticle.class.getName();
    io.vertx.core.Launcher.executeCommand("run", params);

  }
}
