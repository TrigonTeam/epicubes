package cz.trigon.ecubes.res;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Resource {
    public enum ResourceType {
        IMAGE, SOUND, MUSIC, OTHER
    }

    private String name;
    private String fileName;
    private ResourceType type;
    private boolean isLoaded;
    private boolean relative;

    Resource(String name, String fileName, boolean relative) {
        this.name = name;
        this.fileName = fileName;
        this.discoverType();
        this.relative = relative;
    }

    void setLoaded(boolean loaded) {
        this.isLoaded = loaded;
    }

    private void discoverType() {
        //TODO: create better way to determining resource type

        if(!this.fileName.contains(".")) {
            this.type = ResourceType.OTHER;
            return;
        }

        switch (this.fileName.substring(this.fileName.lastIndexOf("."))) {
            case "png":
                this.type = ResourceType.IMAGE;
                break;
            case "ogg":
                this.type = ResourceType.MUSIC;
                break;
            case "wav":
                this.type = ResourceType.SOUND;
                break;
            default:
                this.type = ResourceType.OTHER;
                break;
        }
    }

    public InputStream openStream() throws FileNotFoundException {
        if(this.relative) {
            InputStream s = this.getClass().getResourceAsStream("/" + fileName);
            if(s == null)
                throw new FileNotFoundException();

            return s;
        } else {
            return new FileInputStream(this.fileName);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ResourceType getType() {
        return this.type;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }
}
