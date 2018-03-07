package im.toss.http.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

import static ch.qos.logback.classic.Level.WARN;
import static im.toss.http.parser.HttpAuthCredentials.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class HttpAuthCredentialsTest {

    @Mock private Appender mockAppender;

    @Captor private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    public void tearDown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    public void testParseToken() {
        assertCredential("Basic abc", new HttpAuthCredentials("Basic", "abc"));

        assertCredential("Custom abc, k=v",
                         HttpAuthCredentials.fromSingleValueParams("Custom",
                                                                   "abc",
                                                                   singletonMap("k", "v")));

        assertCredential("Custom abc, k=v1, k=v2",
                         new HttpAuthCredentials("Custom",
                                                 "abc",
                                                 singletonMap("k", asList("v1", "v2"))));

        assertCredential("Custom a=",
                         new HttpAuthCredentials("Custom", "a="));

        assertCredential("Custom a=, k=v",
                         HttpAuthCredentials.fromSingleValueParams("Custom",
                                                                   "a=",
                                                                   singletonMap("k", "v")));

        assertCredentialNonStrict("Custom a=, k=\t",
                                  new HttpAuthCredentials("Custom", "a="));
    }

    private void assertCredentialNonStrict(String credentials, HttpAuthCredentials expected) {
        HttpAuthCredentials actual = parse(credentials, false);

        assertCredential(actual, expected);
    }

    private void assertCredential(String credentials, HttpAuthCredentials expected) {
        HttpAuthCredentials actual = parse(credentials);

        assertCredential(actual, expected);
    }

    private void assertCredential(HttpAuthCredentials actual, HttpAuthCredentials expected) {
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getParams()).isEqualTo(expected.getParams());
        assertThat(actual.getScheme()).isEqualTo(expected.getScheme());
    }

    @Test
    public void testCaseInsensitiveParameterName() {
        assertThat(parse("Custom k=v").getParams().get("K")).isEqualTo(singletonList("v"));
        assertThat(parse("Custom K=v").getParams().get("k")).isEqualTo(singletonList("v"));
        assertThat(parse("Custom K=v").getParams().get("K")).isEqualTo(singletonList("v"));
        assertThat(parse("Custom K=v1, k=v2").getParams().get("k")).isEqualTo(asList("v1", "v2"));
    }

    @Test
    public void shouldThrowExceptionForBadTokenInStrictMode() {
        assertThatExceptionOfType(HttpAuthCredentialParseException.class).isThrownBy(() -> parse(
                "Custom k?",
                true)).withMessage("Bad token: k?");

        assertThatExceptionOfType(HttpAuthCredentialParseException.class).isThrownBy(() -> parse(
                "Custom =v",
                true)).withMessage("Bad token: =v");
    }

    @Test
    public void justWarnIfInNonStrictMode() {
        assertThat(parse("Custom k?", false).getToken()).isEqualTo("k?");
        assertThat(parse("Custom =v", false).getToken()).isEqualTo("=v");

        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getAllValues()).filteredOn(e -> e.getLevel().equals(WARN))
                                                     .anyMatch(expectLog("Bad token: k?"))
                                                     .anyMatch(expectLog("Bad token: =v"));
    }

    private Predicate<LoggingEvent> expectLog(String message) {
        return e -> e.getFormattedMessage().equals(message);
    }

    @Test
    public void strictModeIsDefault() {
        assertThatExceptionOfType(HttpAuthCredentialParseException.class).isThrownBy(() -> parse(
                "Custom k?")).withMessage("Bad token: k?");
    }

    @Test
    public void shouldThrowExceptionForBadParameterInStrictMode() {
        assertThatExceptionOfType(HttpAuthCredentialParseException.class)
                .isThrownBy(() -> parse("Custom k, a?=b", true))
                .withMessage("Bad parameter: a?=b");
    }

    @Test
    public void shouldIgnoreWhitespace() {
        // auth-param     = token BWS "=" BWS ( token / quoted-string )

        HttpAuthCredentials expected =
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "abc",
                                                          singletonMap("k", "v"));

        assertThat(parse("Custom abc, k=v")).isEqualTo(expected);
        assertThat(parse("Custom abc,  k=v")).isEqualTo(expected);
        assertThat(parse("Custom abc, k= v")).isEqualTo(expected);
        assertThat(parse("Custom abc, k=v ")).isEqualTo(expected);
        assertThat(parse("Custom abc, k =v")).isEqualTo(expected);
        assertThat(parse("Custom abc, k = v")).isEqualTo(expected);
    }

    @Test
    public void schemeShouldNotBeNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new HttpAuthCredentials(null, "abc"));
    }

    @Test
    public void tokenShouldNotBeNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new HttpAuthCredentials("Custom", null));
    }

    @Test
    public void paramsShouldNotBeNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new HttpAuthCredentials("Custom", "abc", null));
    }

    @Test
    public void shouldAllowEqualSignOnlyAtTheEndOfTheToken() {
        assertThat(parse("Custom ab=")).isEqualTo(
                new HttpAuthCredentials("Custom", "ab="));
        assertThat(parse("Custom a=b")).isEqualTo(
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "",
                                                          singletonMap("a", "b")));
    }

    @Test
    public void allowNoToken() {
        HttpAuthCredentials expected = new HttpAuthCredentials("Custom", "");
        assertThat(parse("Custom")).isEqualTo(expected);
        assertThat(parse("Custom ")).isEqualTo(expected);
        assertThat(parse("Custom  ")).isEqualTo(expected);
    }

    @Test
    public void allowTabs() {
        HttpAuthCredentials expected =
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "abc",
                                                          singletonMap("k", "v"));
        assertThat(parse("Custom abc,\tk=v")).isEqualTo(expected);
    }

    @Test
    public void shouldSupportQuotedString() {
        assertThat(parse("Custom abc, k=\"my \\\"name\\\"\"")).isEqualTo(
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "abc",
                                                          singletonMap("k", "\"my \\\"name\\\"\"")));

        assertThat(parse("Custom abc, k=\"a, b\"")).isEqualTo(
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "abc",
                                                          singletonMap("k", "\"a, b\"")));
    }

    @Test
    public void equalsShouldWork() {
        HttpAuthCredentials a = new HttpAuthCredentials("Test", "foo");
        HttpAuthCredentials b = new HttpAuthCredentials("Test", "foo");
        HttpAuthCredentials c = new HttpAuthCredentials("Test", "bar");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
    }

    @Test
    public void hashcodeShouldWork() {
        HttpAuthCredentials a = new HttpAuthCredentials("Test", "foo");
        HttpAuthCredentials b = new HttpAuthCredentials("Test", "foo");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void shouldBeNullSafe() {
        HttpAuthCredentials none = HttpAuthCredentials.parse(null);
        assertThat(none.getScheme()).isNotNull();
        assertThat(none.getParams()).isNotNull();
    }

    @Test
    public void multipleToken68IsNotAllowed() {
        assertThatExceptionOfType(HttpAuthCredentialParseException.class).isThrownBy(() -> parse(
                "Custom a, b",
                true)).withMessage("Multiple token68 is not allowed");
    }

    @Test
    public void toStringShouldContainsImportantThings() {
        HttpAuthCredentials credential =
                HttpAuthCredentials.fromSingleValueParams("Custom",
                                                          "aaa",
                                                          singletonMap("xxx", "yyy"));

        assertThat(credential.toString()).contains("Custom");
        assertThat(credential.toString()).contains("aaa");
        assertThat(credential.toString()).contains("xxx");
        assertThat(credential.toString()).contains("yyy");
    }

    @Test
    public void singleValueParams() {
        HttpAuthCredentials credentials =
                new HttpAuthCredentials("Custom",
                                        "abc",
                                        singletonMap("k", asList("v1", "v2")));

        assertThat(credentials.getSingleValueParams()).isEqualTo(singletonMap("k", "v1"));
    }
}
