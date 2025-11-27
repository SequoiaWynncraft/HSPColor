// Provided by Sequoia Mod
package ooo.sequoia.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

public final class HttpUtils {
    public static String USER_AGENT = "SequoiaModHTTPClient/1.0 (minecraft:_WayLessSad_; discord:@.tragedia.; github:Erwqs; mailto:3waylesssad@protonmail.com; restrictions:no-reply-not-allowed)";
    public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(30);

    private HttpUtils() {}

    public static HttpRequest newGetRequest(String url) {
        return newGetRequest(url, TIMEOUT_DURATION);
    }

    public static HttpRequest newGetRequest(String url, Duration timeoutDuration) {
        return HttpRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .uri(URI.create(url))
                .timeout(timeoutDuration)
                .GET()
                .build();
    }

    public static HttpRequest newPostRequest(String url, String body) {
        return newPostRequest(url, body, TIMEOUT_DURATION);
    }

    public static HttpRequest newPostRequest(String url, String body, Duration timeoutDuration) {
        return HttpRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json; charset=utf-8")
                .uri(URI.create(url))
                .timeout(timeoutDuration)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
