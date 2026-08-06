resource "aws_dynamodb_table" "dynamodb_table" {
  name     = "images_table"
  billing_mode = "PAY_PER_REQUEST"
  hash_key = "id"

  attribute {
    name = "id"
    type = "S"
  }
}