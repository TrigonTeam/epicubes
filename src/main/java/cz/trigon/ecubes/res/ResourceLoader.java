package cz.trigon.ecubes.res;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceLoader {

    private final File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    private Map<String, Resource> resources = new HashMap<>();

    public void probe() throws IOException {
        this.probe("/", true);
    }

    public void probe(String directory, boolean relative) throws IOException {
        if(relative) {
            if (!directory.startsWith("/"))
                directory = "/" + directory;
            if (!directory.endsWith("/"))
                directory += "/";
        }

        if (relative && jarFile.isFile()) {
            JarFile jar = new JarFile(this.jarFile);
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();

                if (!name.endsWith(".class") && name.startsWith(directory)) {
                    Resource r = new Resource(name, name.contains(".") ?
                            name.substring(0, name.lastIndexOf('.')) : name, true);
                    this.resources.put(name, r);
                }
            }
        } else {

        }
    }

    public Resource getResource(String name) {
        return this.resources.get(name);
    }

    public List<Resource> getResourcesInDirectory(String dirName) {
        if(!dirName.startsWith("/"))
            dirName = "/" + dirName;

        final String d = dirName;

        List<Resource> ret = new ArrayList<>();

        this.resources.keySet().forEach(s -> {
            if (s.startsWith(d)) {
                ret.add(this.getResource(s));
            }
        });

        return ret;
    }
}
