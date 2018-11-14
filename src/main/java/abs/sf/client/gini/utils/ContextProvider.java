package abs.sf.client.gini.utils;

import android.content.Context;

/**
 * This is an injection interface to enable Android {@link Context}
 * injection into SDK. {@link SDKLoader} must be supplied with
 * {@link ContextProvider} instance which will stored inside
 * {@link abs.ixi.client.core.Platform} for use.
 */
public interface ContextProvider {
    public static final String KEY_CONTEXT = "android-context";

    public Context context();
}
