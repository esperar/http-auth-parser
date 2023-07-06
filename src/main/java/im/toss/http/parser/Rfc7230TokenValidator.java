package im.toss.http.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates a token
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">Section 3.2.6 of RFC 7230</a>
 */
class Rfc7230TokenValidator extends AbstractTokenValidator {

    private static final Pattern p = Pattern.compile("[^-A-Za-z0-9!#$%&'*+.^_`|~ \"\\\\]");

    /**
     * Validates a token
     *
     * @param  token  a token. Must not be null.
     * @throws TokenValidationException if the token is not valid
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">Section 3.2.6 of RFC 7230</a>
     */
    @Override
    public void validate(String token) {
        Matcher matcher = p.matcher(token);
        if (matcher.find()) {
            throw new TokenValidationException(unexpectedCharacterError(matcher.group(),
                                                                        matcher.start()));
        }
    }
}
