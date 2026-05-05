package progsmod.plugin;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import progsmod.data.campaign.EngagementListener;
import progsmod.data.campaign.LunaSModButton;
import progsmod.util.SModUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;

@SuppressWarnings("unused")
public class ProgSMod extends com.fs.starfarer.api.BaseModPlugin {

    @Override
    public void onApplicationLoad() {
        SModUtils.loadConstants("progsmod_settings.json");
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSModButton.addButton();
        }
    }

    /** Disable only when necessary, i.e. in the refit screen, so that fleet inflaters that rely on s-mods being able
     *  to be built in "normally" can work properly. */
    public static void disableStoryPointBuildIn() {
        for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
            if (spec.hasTag("no_build_in")) {
                spec.addTag("progsmod_no_build_in");
            }
            spec.addTag("no_build_in");
        }
    }

    public static void enableStoryPointBuildIn() {
        for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
            if (!spec.hasTag("progsmod_no_build_in")) {
                spec.getTags().remove("no_build_in");
            }
            spec.getTags().remove("progsmod_no_build_in");
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (!SModUtils.Constants.DISABLE_MOD) {
            SModUtils.loadData();
            Global.getSettings().getHullModSpec("progsmod_xptracker").setHiddenEverywhere(false);
            Global.getSector().getMemory().set("$progsmodEnabled", true);
            Global.getSector().addTransientListener(new EngagementListener(false));

            try {
                Class<?> cls = getClassLoader().loadClass("progsmod.data.campaign.RefitTabListenerAndScript");
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle mh = lookup.findConstructor(cls, MethodType.methodType(void.class));
                EveryFrameScript refitScript = (EveryFrameScript) mh.invoke();
                ListenerManagerAPI listeners = Global.getSector().getListenerManager();
                listeners.addListener(refitScript, true);
                Global.getSector().addTransientScript(refitScript);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to add refit tab listener", e);
            }
        }
        else {
            Global.getSettings().getHullModSpec("progsmod_xptracker").setHiddenEverywhere(true);
            Global.getSector().getMemory().set("$progsmodEnabled", false);
        }
    }

    private static final String[] reflectionWhitelist = new String[] {
            "progsmod.data.campaign.RefitTabListenerAndScript",
            "progsmod.util.ReflectionUtils"
    };

    public static ReflectionEnabledClassLoader getClassLoader() {
        URL url = ProgSMod.class.getProtectionDomain().getCodeSource().getLocation();
        return new ReflectionEnabledClassLoader(url, ProgSMod.class.getClassLoader());
    }

    public static class ReflectionEnabledClassLoader extends URLClassLoader {

        public ReflectionEnabledClassLoader(URL url, ClassLoader parent) {
            super(new URL[] {url}, parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("java.lang.reflect")) {
                return ClassLoader.getSystemClassLoader().loadClass(name);
            }
            return super.loadClass(name);
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }
            // Be the defining classloader for all classes in the reflection whitelist
            // For classes defined by this loader, classes in java.lang.reflect will be loaded directly
            // by the system classloader, without the intermediate delegations.
            for (String str : reflectionWhitelist) {
                if (name.startsWith(str)) {
                    return findClass(name);
                }
            }
            return super.loadClass(name, resolve);
        }
    }
}
