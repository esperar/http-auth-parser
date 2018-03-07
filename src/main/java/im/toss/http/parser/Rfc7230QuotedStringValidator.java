package im.toss.http.parser;

import static im.toss.http.parser.Constants.DQUOTE;
import static im.toss.http.parser.Constants.HTAB;
import static im.toss.http.parser.Constants.SP;

/**
 * Validates a quoted string.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">Section 3.2.6 of RFC 7230</a>
 */
class Rfc7230QuotedStringValidator extends AbstractTokenValidator {

    enum State {
        BEGIN, QUOTED_STRING, QUOTED_PAIR, END
    }

    /**
     * Validates a quoted string.
     *
     * @param  quotedString  a quoted string. Must not be null.
     * @throws Rfc7230ListParserException if the quoted string is not valid
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">Section 3.2.6 of RFC 7230</a>
     */
    @Override
    public void validate(String quotedString) {
        // quoted-string  = DQUOTE *( qdtext / quoted-pair ) DQUOTE
        // qdtext         = HTAB / SP /%x21 / %x23-5B / %x5D-7E / obs-text
        // obs-text       = %x80-FF
        // quoted-pair    = "\" ( HTAB / SP / VCHAR / obs-text )
        // VCHAR          = %x21-7E
        // HTAB           = %x09
        // SP             = %x20

        State state = State.BEGIN;

        int pos = 0;
        for(char ch : quotedString.toCharArray()) {
            switch(state) {
                case BEGIN:
                    if (ch == DQUOTE) {
                        state = State.QUOTED_STRING;
                    } else {
                        throw new TokenValidationException(unexpectedCharacterError(ch, pos));
                    }
                    break;
                case QUOTED_STRING:
                    switch(ch) {
                        case '\\':
                            state = State.QUOTED_PAIR;
                            break;
                        case DQUOTE:
                            state = State.END;
                            break;
                        default:
                            if (!isQdText(ch)) {
                                throw new TokenValidationException(
                                        unexpectedCharacterError(ch, pos));
                            }
                    }
                    break;
                case QUOTED_PAIR:
                    // quoted-pair    =  "\" ( HTAB / SP / VCHAR / obs-text )
                    // obs-text       =  %x80-FF
                    // VCHAR          =  %x21-7E
                    // HTAB           =  %x09
                    // SP             =  %x20
                    if (ch == SP || ch == HTAB || isObsText(ch) || isVchar(ch)) {
                        state = State.QUOTED_STRING;
                        continue;
                    } else {
                        throw new TokenValidationException(unexpectedCharacterError(ch, pos));
                    }
                case END:
                    throw new TokenValidationException(unexpectedCharacterError(ch, pos));
            }
            pos++;
        }

        switch(state) {
            case QUOTED_STRING:
                throw new TokenValidationException("Unclosed quoted string");
            case QUOTED_PAIR:
                throw new TokenValidationException("Unclosed quoted pair");
            default:
                break;
        }
    }

    private boolean isQdText(char ch) {
        // qdtext = HTAB / SP / %x21 / %x23-5B / %x5D-7E / obs-text
        return ch == HTAB || ch == SP || (isVchar(ch) && ch != DQUOTE && ch != '\\')
               || isObsText(ch);
    }

    private boolean isVchar(char ch) {
        return ch >= 0x21 && ch <= 0x7E;
    }

    private boolean isObsText(char ch) {
        return ch >= 0x80 && ch <= 0xFF;
    }
}
