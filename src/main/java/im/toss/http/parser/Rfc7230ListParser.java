package im.toss.http.parser;

import java.util.ArrayList;
import java.util.List;

import static im.toss.http.parser.Constants.DQUOTE;

/**
 * Parses comma-delimited lists of elements.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-7">Section 7 of RFC 7230</a>
 */
public class Rfc7230ListParser {

    private final String input;
    private final List<String> values;
    private StringBuilder valueBuilder;

    Rfc7230ListParser(String input) {
        this.input = input;
        this.values = new ArrayList<>();
        this.valueBuilder = new StringBuilder();
    }

    /**
     * Creates a parser for the given {@link String}.
     *
     * @param input  a CSV string. Must not be null.
     * @return a new parser
     * @throws Rfc7230ListParserException if the input cannot be parsed
     */
    public static Rfc7230ListParser parse(String input) {
        Rfc7230ListParser parser = new Rfc7230ListParser(input);
        parser.parse();
        return parser;
    }

    private void parse() {

        boolean inQuotedPair = false;
        boolean inQuotedString = false;

        for(char ch : input.toCharArray()) {
            switch (ch) {
                case DQUOTE:
                    valueBuilder.append(ch);

                    if (inQuotedPair) {
                        break;
                    }

                    inQuotedString = !inQuotedString;

                    if (!inQuotedString) {
                        closeValue();
                    }
                    break;
                case ',':
                    if (inQuotedString || inQuotedPair) {
                        valueBuilder.append(ch);
                    } else {
                        closeValue();
                    }
                    break;
                default:
                    valueBuilder.append(ch);
            }

            inQuotedPair = !inQuotedPair && ch == '\\';
        }

        if (inQuotedPair) {
            throw new Rfc7230ListParserException("Unclosed quoted pair");
        }

        if (inQuotedString) {
            throw new Rfc7230ListParserException("Unclosed quoted string");
        }

        closeValue();
    }

    private void closeValue() {
        String value = valueBuilder.toString().trim();
        if (!value.isEmpty()) {
            values.add(value);
        }
        valueBuilder = new StringBuilder();
    }

    /**
     * Returns the list of parsed values.
     *
     * @return the list of parsed values
     */
    public List<String> values() {
        return values;
    }
}
