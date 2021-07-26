package tudbut.mod.polyfire.utils;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.RuntimeNameMapper;
import tudbut.mod.polyfire.PolyFire;

import java.util.ArrayList;

public class JSFieldMapper implements RuntimeNameMapper {

    public static Context createMapperContext() {
        return Context.newBuilder()
                .allowExperimentalOptions(true)
                .allowHostAccess(HostAccess.ALL)
                .allowAllAccess(true)
                .allowCreateThread(true)
                .allowIO(true)
                .option("engine.EnableMultithreading", "true")
                .runtimeNameMapper(new JSFieldMapper())
                .build();
    }



    private boolean isObfClass(Class<?> clazz) {
        return clazz.getName().startsWith("net.minecraft") && PolyFire.isObfEnv();
    }

    private boolean isObfClass(String clazz) {
        return clazz.startsWith("net.minecraft") && PolyFire.isObfEnv();
    }

    @SuppressWarnings({"unused"}) // Will use this later probably
    private String[] splitClassName(Class<?> clazz) {
        ArrayList<String> classNames = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass.getDeclaringClass() != null) {
            classNames.add(0, currentClass.getSimpleName());
            currentClass = currentClass.getDeclaringClass();
        }
        classNames.add(clazz.getName());
        return classNames.toArray(new String[0]);
    }

    private String getDeobfClassName(Class<?> obfClass) {
        String name = obfClass.getName();
        String rename = name.replaceAll("[.$]", "/");
        rename = PolyFire.obfMap.get(rename);
        if(rename == null)
            return name;
        String ord = name.replaceAll("[^$.]", "");
        String[] spl = rename.split("/");
        rename = "";
        for (int i = 0; i < spl.length; i++) {
            rename += spl[i];
            if(i < spl.length-1)
                rename += ord.charAt(i);
        }
        return rename;
    }

    private String getObfClassName(String clazz) {
        String rename = clazz.replaceAll("[.$]", "/");
        rename = PolyFire.deobfMap.get(rename);
        if(rename == null)
            return clazz;
        String ord = clazz.replaceAll("[^$.]", "");
        String[] spl = rename.split("/");
        rename = "";
        for (int i = 0; i < spl.length; i++) {
            rename += spl[i];
            if(i < spl.length-1)
                rename += ord.charAt(i);
        }
        return rename;
    }

    private void allInClassTree(Class<?> clazz, ArrayList<Class<?>> list) {
        list.add(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            allInClassTree(interfaces[i], list);
        }
        if(clazz.getSuperclass() != null)
            allInClassTree(clazz.getSuperclass(), list);
    }

    @Override
    public String getClass(String name) {
        if(name.startsWith("pf."))
            name = "tudbut.mod.polyfire." + name.substring("pf.".length());
        System.out.println("JS loaded class " + name);
        if(isObfClass(name)) {
            return getObfClassName(name);
        }
        return name;
    }

    @Override
    public String getClass(Class<?> clazz, String name) {
        System.out.println("JS loaded class " + clazz.getName() + "$" + name);
        if(isObfClass(clazz)) {
            String fullName = getObfClassName(getDeobfClassName(clazz) + "$" + name);
            return fullName.substring(fullName.lastIndexOf("$")+1);
        }
        return name;
    }

    @Override
    public String getField(Class<?> clazz, String name) {
        System.out.println("JS loaded field " + clazz.getName() + "#" + name);
        if(isObfClass(clazz)) {
            ArrayList<Class<?>> list = new ArrayList<>();
            allInClassTree(clazz, list);
            String fullName = null;
            for (int i = 0; i < list.size() && fullName == null; i++) {
                fullName = PolyFire.deobfMap.get(getDeobfClassName(list.get(i)).replaceAll("[.$]", "/") + "/" + name);
            }
            if(fullName == null)
                return name;
            return fullName.substring(fullName.lastIndexOf("/")+1);
        }
        return name;
    }

    @Override
    public String getMethod(Class<?> clazz, String name) {
        System.out.println("JS loaded method " + clazz.getName() + "#" + name);
        if(isObfClass(clazz)) {
            ArrayList<Class<?>> list = new ArrayList<>();
            allInClassTree(clazz, list);
            String fullName = null;
            for (int i = 0; i < list.size() && fullName == null; i++) {
                fullName = PolyFire.deobfMap.get(getDeobfClassName(list.get(i)).replaceAll("[.$]", "/") + "/" + name);
            }
            if(fullName == null)
                return name;
            return fullName.substring(fullName.lastIndexOf("/")+1);
        }
        return name;
    }
}
