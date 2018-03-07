package im.toss.http.parser;

abstract class AbstractTokenValidator implements TokenValidator {

    protected String unexpectedCharacterError(String unexpectedCharacter, int position) {
        return String.format("Unexpected character '%s' at position %d",
                             unexpectedCharacter, position);
    }

    protected String unexpectedCharacterError(char unexpectedCharacter, int position) {
        return unexpectedCharacterError("" + unexpectedCharacter, position);
    }
}
