package api

import io.grpc.Status

object StatusErrors {

  // Account errors:
  val unauthenticated = Status.UNAUTHENTICATED.withDescription("Invalid or missing access token")
  val invalidName = Status.INVALID_ARGUMENT
    .withDescription("Name should contain only alphanumeric character and length should be minimum 3 chars and maximum 25 chars")
  val invalidPassword = Status.INVALID_ARGUMENT
    .withDescription("Password length should be minimum 8 chars and maximum 36 chars")

  val failedToInsertAccount = Status.INTERNAL.withDescription("Failed to insert account")
  val failedToGetAccount = Status.INTERNAL.withDescription("Failed to get account")
  val failedToCreateAccessToken = Status.INTERNAL.withDescription("Failed to create access token")
}
