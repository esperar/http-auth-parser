package im.toss.http.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class Rfc7230QuotedStringValidatorTest {

    @Test
    public void validate() {
        Rfc7230QuotedStringValidator validator = new Rfc7230QuotedStringValidator();

        validator.validate("\"abc\"");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("abc"))
                .withMessage("Unexpected character 'a' at position 0");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"abc"))
                .withMessage("Unclosed quoted string");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"가나다\""))
                .withMessage("Unexpected character '가' at position 1");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"\\가나다\""))
                .withMessage("Unexpected character '가' at position 2");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"\\"))
                .withMessage("Unclosed quoted pair");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"\\\""))
                .withMessage("Unclosed quoted string");
        validator.validate("\"\\\"\"");
        assertThatExceptionOfType(TokenValidationException.class)
                .isThrownBy(() -> validator.validate("\"a\"b"))
                .withMessage("Unexpected character 'b' at position 3");
    }
}