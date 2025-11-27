// Provided by Sequoia Mod
package ooo.sequoia.http;

import ooo.sequoia.http.clients.MojangApiHttpClient;
import ooo.sequoia.http.clients.UpdateApiHttpClient;
import ooo.sequoia.http.clients.WynncraftApiHttpClient;

public final class HttpClients {
    public static final WynncraftApiHttpClient WYNNCRAFT_API = WynncraftApiHttpClient.newHttpClient();
    public static final MojangApiHttpClient MOJANG_API = MojangApiHttpClient.newHttpClient();
    public static final UpdateApiHttpClient UPDATE_API = UpdateApiHttpClient.newHttpClient();

    private HttpClients() {}
}
