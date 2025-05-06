package rnd.dev.student_info.config;

import io.vertx.core.json.JsonObject;
import rnd.dev.student_info.constant.ApplicationConstant;

public class MongoDBConnection {

  public static JsonObject dbConnection() {
    return new JsonObject()
      .put(ApplicationConstant.ENV_DB_HOST, ApplicationConstant.DB_HOST)
      .put(ApplicationConstant.ENV_DB_PORT, ApplicationConstant.DB_PORT)
      .put(ApplicationConstant.ENV_DB_NAME, ApplicationConstant.DB_NAME);
  }
}
