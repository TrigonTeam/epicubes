package cz.trigon.ecubes.res;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        if (relative) {
            if (!directory.startsWith("/"))
                directory = "/" + directory;
            if (!directory.endsWith("/"))
                directory += "/";
        }

        if (relative) {
            if (this.jarFile.isFile()) {
                JarFile jar = new JarFile(this.jarFile);
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();

                    if (!name.endsWith(".class") && name.startsWith(directory)) {
                        Resource r = new Resource((name.contains(".") ?
                                name.substring(0, name.lastIndexOf('.')) : name).replace(directory, ""), name, true);
                        this.resources.put(name, r);
                    }
                }
            } else {
                InputStream str = (InputStream) this.getClass().getResource(directory).getContent();
                InputStreamReader r = new InputStreamReader(str);
                StringBuffer b = new StringBuffer();
                while (r.ready()) {
                    char c = (char) r.read();
                    if(c == '\n') {
                        String entry = b.toString();
                        if(!entry.endsWith(".class")) {
                            String path = directory + entry;
                            if (this.getClass().getResourceAsStream(path) instanceof ByteArrayInputStream) {
                                this.probe(path + "/", true);
                                System.out.println(path + " IZ DIR");
                            } else {
                                Resource res = new Resource(entry.contains(".") ?
                                        entry.substring(0, entry.lastIndexOf('.')) : entry, path, true);
                                this.resources.put(path, res);
                                System.out.println(path + " IZ FYLE");
                            }

                            b.setLength(0);
                        }
                    } else {
                        b.append(c);
                    }
                }
            }
        }
    }

    public Resource getResource(String name) {
        return this.resources.get(name);
    }

    public List<Resource> getResourcesInDirectory(String dirName) {
        if (!dirName.startsWith("/"))
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
