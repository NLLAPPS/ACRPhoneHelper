package com.nll.helper.update;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 *
 * https://stackoverflow.com/questions/67718409/packageinstaller-does-not-work-on-miui-optimization-mode
 *
 */
public class MiuiUtils {

    public static boolean isMiui() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static String getActualMiuiVersion() {
        return Build.VERSION.INCREMENTAL;
    }

    private static int[] parseVersionIntoParts(String version) {
        try {
            String[] versionParts = version.split("\\.");
            int[] intVersionParts = new int[versionParts.length];

            for (int i = 0; i < versionParts.length; i++)
                intVersionParts[i] = Integer.parseInt(versionParts[i]);

            return intVersionParts;
        } catch (Exception e) {
            return new int[]{-1};
        }
    }

    /**
     * @return 0 if versions are equal, values less than 0 if ver1 is lower than ver2, value more than 0 if ver1 is higher than ver2
     */
    private static int compareVersions(String version1, String version2) {
        if (version1.equals(version2))
            return 0;

        int[] version1Parts = parseVersionIntoParts(version1);
        int[] version2Parts = parseVersionIntoParts(version2);

        for (int i = 0; i < version2Parts.length; i++) {
            if (i >= version1Parts.length)
                return -1;

            if (version1Parts[i] < version2Parts[i])
                return -1;

            if (version1Parts[i] > version2Parts[i])
                return 1;
        }

        return 1;
    }

    public static boolean isActualMiuiVersionAtLeast(String targetVer) {
        return compareVersions(getActualMiuiVersion(), targetVer) >= 0;
    }

    @SuppressLint("PrivateApi")
    public static boolean isMiuiOptimizationDisabled() {
        if ("0".equals(getSystemProperty("persist.sys.miui_optimization")))
            return true;

        try {
            return (boolean) Class.forName("android.miui.AppOpsUtils")
                    .getDeclaredMethod("isXOptMode")
                    .invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isFixedMiui() {
        return isActualMiuiVersionAtLeast("20.2.20") || isMiuiOptimizationDisabled();
    }

    @SuppressLint("PrivateApi")
    @Nullable
    public static String getSystemProperty(String key) {
        try {
            return (String) Class.forName("android.os.SystemProperties")
                    .getDeclaredMethod("get", String.class)
                    .invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}