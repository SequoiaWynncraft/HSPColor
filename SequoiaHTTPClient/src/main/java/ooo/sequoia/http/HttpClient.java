// Provided by Sequoia Mod
package ooo.sequoia.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient {
    private static final int[] OK_STATUS_CODES = {200, 201, 202, 203, 204,
            205, 206, 207, 208, 226};
    private static final Gson gson = new GsonBuilder().create();
    private static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newHttpClient();
    private static final Logger LOGGER = Logger.getLogger(HttpClient.class.getName());

    protected HttpClient() {}

    public static HttpClient newHttpClient() {
        return new HttpClient();
    }

    private boolean isOkStatusCode(int statusCode) {
        for (int okStatusCode : OK_STATUS_CODES) {
            if (statusCode == okStatusCode) {
                return true;
            }
        }
        return false;
    }

    private HttpResponse<String> sendSyncRequest(java.net.http.HttpRequest request, String context) {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Thread interrupted while " + context, exception);
            return null;
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed " + context, exception);
            return null;
        }
    }

    private CompletableFuture<HttpResponse<String>> sendAsyncRequest(java.net.http.HttpRequest request, String context) {
        try {
            return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed " + context, exception);
            return null;
        }
    }

    private HttpResponse<byte[]> sendSyncRequestBytes(java.net.http.HttpRequest request, String context) {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Thread interrupted while " + context, exception);
            return null;
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed " + context, exception);
            return null;
        }
    }

    private CompletableFuture<HttpResponse<byte[]>> sendAsyncRequestBytes(java.net.http.HttpRequest request, String context) {
        try {
            return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed " + context, exception);
            return null;
        }
    }

    public HttpResponse<String> get(String url) {
        return sendSyncRequest(HttpUtils.newGetRequest(url), "fetching response");
    }

    public CompletableFuture<HttpResponse<String>> getAsync(String url) {
        return sendAsyncRequest(HttpUtils.newGetRequest(url), "fetching async response");
    }

    public HttpResponse<byte[]> getBytes(String url) {
        return sendSyncRequestBytes(HttpUtils.newGetRequest(url), "fetching binary response");
    }

    public CompletableFuture<HttpResponse<byte[]>> getBytesAsync(String url) {
        return sendAsyncRequestBytes(HttpUtils.newGetRequest(url), "fetching async binary response");
    }

    public HttpResponse<String> post(String url, String body) {
        return sendSyncRequest(HttpUtils.newPostRequest(url, body), "posting response");
    }

    public CompletableFuture<HttpResponse<String>> postAsync(String url, String body) {
        return sendAsyncRequest(HttpUtils.newPostRequest(url, body), "posting async response");
    }

    public <T> T getJson(String url, Class<T> responseType) {
        return getJson(url, responseType, gson);
    }

    public byte[] getBinary(String url) {
        HttpResponse<byte[]> response = getBytes(url);
        if (response != null) {
            int length = response.body() == null ? 0 : response.body().length;
            LOGGER.fine(response.statusCode() + " " + url + " bytes=" + length);
            if (response.body() != null && isOkStatusCode(response.statusCode())) {
                return response.body();
            }
        }
        return null;
    }

    public CompletableFuture<byte[]> getBinaryAsync(String url) {
        return getBytesAsync(url).thenApply(response -> {
            if (response != null) {
                int length = response.body() == null ? 0 : response.body().length;
                LOGGER.fine("ASYNC " + response.statusCode() + " " + url + " bytes=" + length);
                if (response.body() != null && isOkStatusCode(response.statusCode())) {
                    return response.body();
                }
            }
            return null;
        });
    }

    public <T> T getJson(String url, Class<T> responseType, Gson gson) {
        HttpResponse<String> response = get(url);
        if (response != null) {
            LOGGER.fine(response.statusCode() + " " + url + " " + response.body());
            if (response.body() != null && isOkStatusCode(response.statusCode())) {
                return gson.fromJson(response.body(), responseType);
            }
        }
        return null;
    }

    public <T> CompletableFuture<T> getJsonAsync(String url, Class<T> responseType) {
        return getJsonAsync(url, responseType, gson);
    }

    public <T> CompletableFuture<T> getJsonAsync(String url, Class<T> responseType, Gson gson) {
        return getAsync(url).thenApply(response -> {
            if (response != null) {
                LOGGER.fine("ASYNC " + response.statusCode() + " " + url + " " + response.body());
                if (response.body() != null && isOkStatusCode(response.statusCode())) {
                    return gson.fromJson(response.body(), responseType);
                }
            }
            return null;
        });
    }
}
