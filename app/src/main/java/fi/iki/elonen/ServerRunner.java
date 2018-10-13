package fi.iki.elonen;

import java.io.IOException;

import timber.log.Timber;

public class ServerRunner {
    public static void run(Class serverClass) {
        try {
            executeInstance((NanoHTTPD) serverClass.newInstance());
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        } catch (IOException ioe) {
            Timber.e(ioe, "Couldn't start server: %s", ioe.getMessage());
            System.exit(-1);
        }

        Timber.d("Server started, Hit Enter to stop.\n");

        try {
            System.in.read();
        } catch (Exception ignored) {
        }

        server.stop();
        Timber.d("Server stopped.\n");
    }
}
