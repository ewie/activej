package io.activej.http;

import io.activej.common.StringFormatUtils;
import io.activej.dns.RemoteAsyncDnsClient;
import io.activej.eventloop.Eventloop;
import io.activej.test.rules.ByteBufRule;
import io.activej.test.rules.EventloopRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

import static io.activej.promise.TestUtils.await;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

@Ignore
public class AsyncHttpClientBufsConsumerDecoderTest {
	private static final String URL_WITH_BIG_CHUNK = "https://o7planning.org/ru/10399/jsoup-java-html-parser-tutorial";
	private static final String URL_WITHOUT_REQUIRED_SSL = "http://paypal.com";
	private static final String URL_WITH_REQUIRED_SSL = "https://paypal.com";
	private static final String URL_TEST_SSL = "https://ukraine.craigslist.org/";
	private static final String URL_TEST_SSL2 = "https://www.forbes.com/";
	private static final String URL_TEST_HTTP_ParseException = "http://www.reverso.net/";
	private static final String URL_TEST_HTTP_ParseException2 = "https://www.hepsiburada.com/";

	@ClassRule
	public static EventloopRule eventloopRule = new EventloopRule();

	@ClassRule
	public static final ByteBufRule byteBufRule = new ByteBufRule();

	private static AsyncHttpClient client;

	@BeforeClass
	public static void init() throws NoSuchAlgorithmException, IOException {
		RemoteAsyncDnsClient dnsClient = RemoteAsyncDnsClient.create(Eventloop.getCurrentEventloop())
				.withDnsServerAddress(InetAddress.getByName("8.8.8.8"))
				.withTimeout(StringFormatUtils.parseDuration("5 seconds"));
		client = AsyncHttpClient.create(Eventloop.getCurrentEventloop())
				.withSslEnabled(SSLContext.getDefault(), newSingleThreadExecutor())
				.withDnsClient(dnsClient);
	}

	@Test
	public void testWithBigChunk() {
		testUrl(URL_WITH_BIG_CHUNK);
	}

	@Test
	public void testWithoutRequiredSsl() {
		testUrl(URL_WITHOUT_REQUIRED_SSL);
	}

	@Test
	public void testWithRequiredSsl() {
		testUrl(URL_WITH_REQUIRED_SSL);
	}

	@Test
	public void testSsl() {
		testUrl(URL_TEST_SSL);
	}

	@Test
	public void testSsl2() {
		testUrl(URL_TEST_SSL2);
	}

	@Test
	public void testPotentialParseException() {
		testUrl(URL_TEST_HTTP_ParseException);
	}

	@Test
	public void testPotentialParseException2() {
		testUrl(URL_TEST_HTTP_ParseException2);
	}

	public void testUrl(String url) {
		String result = await(client.request(
				HttpRequest.get(url)
						.withHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1")
						.withHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
						.withHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
						.withHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8"))
				.then(response -> response.loadBody())
				.map(body -> body.getString(UTF_8)));
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}
}
