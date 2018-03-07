package im.toss.http.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static im.toss.http.parser.TokenValidatorFactory.TokenType.RFC7230_QUOTED_STRING;
import static im.toss.http.parser.TokenValidatorFactory.TokenType.RFC7230_TOKEN;
import static im.toss.http.parser.TokenValidatorFactory.TokenType.RFC7235_TOKEN68;
import static java.util.Collections.singletonList;

/**
 * An HTTP authentication credentials.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7235">Hypertext Transfer Protocol (HTTP/1.1):
 * Authentication</a>
 */
public class HttpAuthCredentials {

    private static final Logger log = LoggerFactory.getLogger(HttpAuthCredentials.class);

    private final String scheme;
    private final String token;

    // value must be neither null nor empty.
    private final Map<String, List<String>> params;

    public HttpAuthCredentials(String scheme, String token) {
        this(scheme, token, Collections.emptyMap());
    }

    public HttpAuthCredentials(String scheme, String token, Map<String, List<String>> params) {
        this.scheme = Objects.requireNonNull(scheme, "scheme must not be null");
        this.token = Objects.requireNonNull(token,  "token must not be null");
        this.params = Objects.requireNonNull(params,  "params must not be null");
    }

    /**
     * Obtains an instance of {@code HttpAuthCredentials} from scheme, token and single value
     * params.
     *
     * @param scheme  the auth scheme, not null
     * @param token  the token68, not null
     * @param params  the auth params, not null
     * @return HttpAuthCredentials, not null
     * @throws NullPointerException if any argument is null
     */
    public static HttpAuthCredentials fromSingleValueParams(
            String scheme, String token, Map<String, String> params) {

        Map<String, List<String>> multiValuesParams =
                params.entrySet()
                      .stream()
                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                e -> singletonList(e.getValue())));

        return new HttpAuthCredentials(scheme, token, multiValuesParams);
    }

    public static HttpAuthCredentials parse(String credentials) {
        return parse(credentials, true);
    }

    /**
     * Obtains an instance of {@code HttpAuthCredentials} from credentials such as
     * {@code Custom k1=v1, k2=v2}.
     *
     * @param credentials  the credentials to parse such as "Custom k1=v1, k2=v2", not null
     * @param strict  For every parsing error, an exception is thrown if true, a warning
     *                message is logged if false
     * @return HttpAuthCredentials, not null
     * @throws HttpAuthCredentialParseException for every parsing error if {@code strict} is true
     */
    public static HttpAuthCredentials parse(String credentials, boolean strict) {
        // credentials = auth-scheme [ 1*SP ( token68 / #auth-param ) ]

        if (credentials == null) {
            return HttpAuthCredentials.none();
        }

        List<String> parts = Arrays.asList(credentials.split("\\s+", 2));
        String authScheme = parts.get(0);
        String remains = parts.size() >= 2 ? parts.get(1) : "";

        TokenValidatorFactory tokenValidatorFactory = new TokenValidatorFactory();
        TokenValidator tokenValidator = tokenValidatorFactory.create(RFC7230_TOKEN);
        TokenValidator quotedStringValidator = tokenValidatorFactory.create(RFC7230_QUOTED_STRING);
        TokenValidator token68Validator = tokenValidatorFactory.create(RFC7235_TOKEN68);

        Map<String, List<String>> parameterValueByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String token = "";

        Rfc7230ListParser parser = Rfc7230ListParser.parse(remains);
        for(String value : parser.values()) {
            KvPair pair = KvPair.parse(value);
            if (pair.getKey().isEmpty() || pair.getValue().isEmpty()) {
                if (token.isEmpty()) {
                    token = value;
                } else {
                    handleError(strict, "Multiple token68 is not allowed");
                }
            } else {
                try {
                    tokenValidator.validate(pair.getKey());
                    if (pair.getValue().startsWith("\"")) {
                        quotedStringValidator.validate(pair.getValue());
                    } else {
                        tokenValidator.validate(pair.getValue());
                    }
                } catch (Exception e) {
                    handleError(strict, "Bad parameter: " + value, e);
                }
                addParameter(parameterValueByName, pair);
            }
        }

        try {
            token68Validator.validate(token);
        } catch (Exception e) {
            handleError(strict, "Bad token: " + token, e);
        }

        return new HttpAuthCredentials(authScheme, token, parameterValueByName);
    }

    private static void addParameter(Map<String, List<String>> parameterValueByName, KvPair pair) {
        List<String> values = parameterValueByName.get(pair.getKey());
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(pair.getValue());
        parameterValueByName.put(pair.getKey(), values);
    }

    private static void handleError(boolean strict, String message) {
        if (strict) {
            throw new HttpAuthCredentialParseException(message);
        }
        log.warn(message);
    }

    private static void handleError(boolean strict, String message, Exception e) {
        if (strict) {
            throw new HttpAuthCredentialParseException(message, e);
        }
        log.warn(message);
    }

    /**
     * Returns empty HttpAuthCredentials.
     *
     * @return empty HttpAuthCredentials which scheme is "", token is "" and values are empty map.
     */
    public static HttpAuthCredentials none() {
        return new HttpAuthCredentials("", "");
    }

    public String getScheme() {
        return scheme;
    }

    public String getToken() {
        return token;
    }

    /**
     * Returns the parameters.
     *
     * @return the parameters as an unmodifiable map that contains the mapping from keys to the
     * values.
     */
    public Map<String, List<String>> getParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * Returns the parameters in single value representation.
     *
     * @return the parameters as an unmodifiable map that contains the mapping from keys to the
     * first values.
     */
    public Map<String, String> getSingleValueParams() {
        return Collections.unmodifiableMap(
                getParams().entrySet()
                           .stream()
                           .collect(Collectors.toMap(Map.Entry::getKey,
                                                     e -> e.getValue().get(0))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpAuthCredentials that = (HttpAuthCredentials) o;
        return Objects.equals(scheme, that.scheme) && Objects.equals(token, that.token)
               && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, token, params);
    }

    @Override
    public String toString() {
        return "HttpAuthCredentials{" + "scheme='" + scheme + '\'' + ", token='" + token + '\''
               + ", params=" + params + '}';
    }
}
