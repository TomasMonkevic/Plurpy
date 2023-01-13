package api

import org.tomasmo.plurpy.domain.AuthContext
import org.tomasmo.plurpy.service.Authorizer
import scalapb.zio_grpc.RequestContext
import zio.{UIO, ZIO}

object AuthContextTransformer {
  private val AuthorizationTokenKey = io.grpc.Metadata.Key.of("authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER)

  def apply(authorizer: Authorizer)(rc: RequestContext): ZIO[Any, Nothing, AuthContext] = {
    rc.metadata
      .get(AuthorizationTokenKey)
      .flatMap(authorization =>
        authorization.map(a => getAuthContext(authorizer, a.split(" ")(1))).getOrElse(ZIO.succeed(AuthContext.empty))
      ) //TODO refactor the split string hack
  }

  private def getAuthContext(authorizer: Authorizer, bearerToken: String): UIO[AuthContext] = {
    authorizer.getAuthContext(bearerToken)
  }
}
