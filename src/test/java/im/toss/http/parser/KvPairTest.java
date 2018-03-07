package im.toss.http.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class KvPairTest {

    @Test
    public void test() {
        assertThat(KvPair.parse("a=b")).isEqualTo(new KvPair("a", "b"));
        assertThat(KvPair.parse("a= b")).isEqualTo(new KvPair("a", "b"));
        assertThat(KvPair.parse("a=\tb")).isEqualTo(new KvPair("a", "b"));
        assertThat(KvPair.parse("a =b")).isEqualTo(new KvPair("a", "b"));
        assertThat(KvPair.parse("a = b")).isEqualTo(new KvPair("a", "b"));
        assertThat(KvPair.parse("a = \"b=c\"")).isEqualTo(new KvPair("a", "\"b=c\""));
        assertThat(KvPair.parse("a = \"b=\\\"c=d\\\"\"")).isEqualTo(new KvPair("a", "\"b=\\\"c=d\\\"\""));
        assertThat(KvPair.parse("a")).isEqualTo(new KvPair("", "a"));
    }

    @Test
    public void equalsShouldWork() {
        KvPair a = new KvPair("a", "b");
        KvPair b = new KvPair("a", "b");
        KvPair c = new KvPair("a", "c");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
    }

    @Test
    public void hashcodeShouldWork() {
        KvPair a = new KvPair("a", "b");
        KvPair b = new KvPair("a", "b");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void toStringShouldContainsImportantThings() {
        KvPair a = new KvPair("aaa", "bbb");

        assertThat(a.toString()).contains("aaa");
        assertThat(a.toString()).contains("bbb");
    }

    @Test
    public void getters() {
        KvPair parsed = KvPair.parse("a=b");
        assertThat(parsed.getKey()).isEqualTo("a");
        assertThat(parsed.getValue()).isEqualTo("b");
    }

}
