package im.toss.http.parser

class TokenValidatorFactory {
    enum class TokenType {
        RFC7230_TOKEN,
        RFC7235_TOKEN68,
        RFC7230_QUOTED_STRING
    }

    fun create(type: TokenType): TokenValidator {
        return when (type) {
            TokenType.RFC7230_TOKEN -> Rfc7230TokenValidator()
            TokenType.RFC7235_TOKEN68 -> Rfc7235Token68Validator()
            TokenType.RFC7230_QUOTED_STRING -> Rfc7230QuotedStringValidator()
        }
    }
}
