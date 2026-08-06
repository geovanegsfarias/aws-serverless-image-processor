resource "aws_lambda_function" "lambda_function" {
  function_name = "image-processor"
  role          = aws_iam_role.lambda_role.arn
  handler       = "com.github.geovanegsfarias.Handler::handleRequest"
  runtime       = "java21"
  filename      = "../target/aws-serverless-image-processor.jar"
  timeout       = 30
  memory_size   = 512

  environment {
    variables = {
      ENDPOINT_URL      = "http://host.docker.internal:4566"
      ACCESS_KEY_ID     = "test"
      SECRET_ACCESS_KEY = "test"
    }
  }
}