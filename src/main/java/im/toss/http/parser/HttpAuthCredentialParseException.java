package im.toss.http.parser;

public class HttpAuthCredentialParseException extends RuntimeException {

    HttpAuthCredentialParseException(String s, Exception e) {
        super(s, e);
    }

    HttpAuthCredentialParseException(String s) {
        super(s);
    }
}
