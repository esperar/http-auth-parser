package im.toss.http.parser;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class RFC7230ListParserTest {

    @Test
    public void test() {

        assertThat(Rfc7230ListParser.parse("a, b").values())
                .isEqualTo(asList("a", "b"));

        assertThat(Rfc7230ListParser.parse("a, \"b, c\"").values())
                .isEqualTo(asList("a", "\"b, c\""));

        assertThat(Rfc7230ListParser.parse("a, b=\"d, c\"").values())
                .isEqualTo(asList("a", "b=\"d, c\""));

        assertThat(Rfc7230ListParser.parse("a, b=\"d, \\\"c\\\"\"").values())
                .isEqualTo(asList("a", "b=\"d, \\\"c\\\"\""));

        assertThat(Rfc7230ListParser.parse("a, \"b\\1\", c").values())
                .isEqualTo(asList("a", "\"b\\1\"", "c"));

        assertThat(Rfc7230ListParser.parse("a, \"b\\\"\", c").values())
                .isEqualTo(asList("a", "\"b\\\"\"", "c"));
    }

    @Test
    public void unclosedQuotedString() {
        // a, "b\", c
        assertThatExceptionOfType(Rfc7230ListParserException.class)
                .isThrownBy(() -> Rfc7230ListParser.parse("a, \"b\\\", c"))
                .withMessage("Unclosed quoted string");
    }

    @Test
    public void unclosedQuotedPair() {
        // a, "b\\", c
        assertThatExceptionOfType(Rfc7230ListParserException.class)
                .isThrownBy(() -> Rfc7230ListParser.parse("a, \"b\\"))
                .withMessage("Unclosed quoted pair");
    }
}
