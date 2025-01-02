package io.github.edadma.apion_demo

import io.github.edadma.apion._
import zio.json._
import scala.concurrent.Future
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global

// Data models
case class User(
    id: String,
    email: String,
    name: String,
    role: String,
    createdAt: Long = System.currentTimeMillis(),
) derives JsonEncoder, JsonDecoder

case class LoginRequest(email: String, password: String) derives JsonDecoder
case class CreateUserRequest(email: String, name: String, password: String, role: String) derives JsonDecoder
case class UpdateUserRequest(name: Option[String], role: Option[String]) derives JsonDecoder
case class LoginResponse(token: String, user: User) derives JsonEncoder

// In-memory user store (replace with database in production)
object UserStore {
  private var users = Map[String, (User, String)]( // (User, PasswordHash)
    "1" -> (User("1", "admin@example.com", "Admin", "admin"), "admin123"),
    "2" -> (User("2", "user@example.com", "User", "user"), "user123"),
  )
  private var nextId = 3

  def getUsers: List[User] = users.values.map(_._1).toList

  def getUser(id: String): Option[User] = users.get(id).map(_._1)

  def getUserByEmail(email: String): Option[(User, String)] =
    users.values.find(_._1.email == email)

  def createUser(email: String, name: String, password: String, role: String): User = {
    val id = nextId.toString
    nextId += 1
    val user = User(id, email, name, role)
    users += (id -> (user, password))
    user
  }

  def updateUser(id: String, name: Option[String], role: Option[String]): Option[User] = {
    users.get(id).map { case (user, pwd) =>
      val updated = user.copy(
        name = name.getOrElse(user.name),
        role = role.getOrElse(user.role),
      )
      users += (id -> (updated, pwd))
      updated
    }
  }
}

@main
def run(): Unit = {
  // Auth configuration
  val authConfig = AuthMiddleware.AuthConfig(
    secretKey = "your-secret-key-change-this",
    requireAuth = true,
    excludePaths = Set("/auth/login"),
  )

  // Auth middleware
  val auth = AuthMiddleware(authConfig)

  // Role-based authorization middleware
  def requireRole(role: String): Handler = request => {
    request.context.get("auth") match {
      case Some(auth: AuthMiddleware.Auth) if auth.roles.contains(role) =>
        request.skip
      case _ =>
        "Insufficient permissions".asText(403)
    }
  }

  val server = Server()
    .use(LoggingMiddleware())
    .use(CorsMiddleware(CorsMiddleware.Options(
      origin = CorsMiddleware.Origin.Any,
      credentials = true,
      exposedHeaders = Set("*"),
    )))
    .use(BodyParser.json[LoginRequest]())

    // Auth endpoints
    .post(
      "/auth/login",
      request => {
        request.context.get("body") match {
          case Some(LoginRequest(email, password)) =>
            UserStore.getUserByEmail(email) match {
              case Some((user, pwd)) if pwd == password => // Use proper hashing in production
                val token = AuthMiddleware.createAccessToken(
                  user.id,
                  Set(user.role),
                  authConfig,
                )
                LoginResponse(token, user).asJson
              case _ =>
                "Invalid credentials".asText(401)
            }
          case _ => "Invalid request".asText(400)
        }
      },
    )

    // Protected routes
    .use(auth)

    // User management endpoints
    .get(
      "/users",
      request => {
        request.context.get("auth") match {
          case Some(auth: AuthMiddleware.Auth) if auth.roles.contains("admin") =>
            UserStore.getUsers.asJson
          case Some(auth: AuthMiddleware.Auth) =>
            // Regular users can only see themselves
            UserStore.getUser(auth.user).toList.asJson
          case _ => "Unauthorized".asText(401)
        }
      },
    )
    .get(
      "/users/:id",
      request => {
        val userId = request.params("id")
        request.context.get("auth") match {
          case Some(auth: AuthMiddleware.Auth)
              if auth.roles.contains("admin") || auth.user == userId =>
            UserStore.getUser(userId) match {
              case Some(user) => user.asJson
              case None       => "User not found".asText(404)
            }
          case _ => "Unauthorized".asText(401)
        }
      },
    )
    .use(BodyParser.json[CreateUserRequest]())
    .post(
      "/users",
      requireRole("admin"),
      request => {
        request.context.get("body") match {
          case Some(CreateUserRequest(email, name, password, role)) =>
            if (UserStore.getUserByEmail(email).isDefined) {
              "Email already exists".asText(400)
            } else {
              val user = UserStore.createUser(email, name, password, role)
              user.asJson(201)
            }
          case _ => "Invalid request".asText(400)
        }
      },
    )
    .use(BodyParser.json[UpdateUserRequest]())
    .patch(
      "/users/:id",
      request => {
        val userId = request.params("id")
        request.context.get("auth") match {
          case Some(auth: AuthMiddleware.Auth)
              if auth.roles.contains("admin") || auth.user == userId =>
            request.context.get("body") match {
              case Some(UpdateUserRequest(name, role)) =>
                // Only admins can change roles
                val newRole = if (auth.roles.contains("admin")) role else None
                UserStore.updateUser(userId, name, newRole) match {
                  case Some(user) => user.asJson
                  case None       => "User not found".asText(404)
                }
              case _ => "Invalid request".asText(400)
            }
          case _ => "Unauthorized".asText(401)
        }
      },
    )

  server.listen(3000) {
    println("Server running at http://localhost:3000")
  }
}
