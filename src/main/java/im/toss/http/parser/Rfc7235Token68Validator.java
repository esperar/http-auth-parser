package im.toss.http.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates a token68.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7235#section-2.1">Section 2.1 of RFC 7235</a>
 */
class Rfc7235Token68Validator extends AbstractTokenValidator {

    /**
     * Validates a token68.
     *
     * @param  token68  a token68. Must not be null.
     * @throws TokenValidationException if the token68 is not valid
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-2.1">Section 2.1 of RFC 7235</a>
     */
    @Override
    public void validate(String token68) {
        Pattern p = Pattern.compile("[^-A-Za-z0-9!#$%&'*+.^_`|~]");
        Matcher matcher = p.matcher(token68);
        if (matcher.find()) {
            if (matcher.start() == token68.length() - 1 && matcher.group().equals("=")) {
                return;
            }
            throw new TokenValidationException(unexpectedCharacterError(matcher.group(),
                                                                        matcher.start()));
        }
    }
}
