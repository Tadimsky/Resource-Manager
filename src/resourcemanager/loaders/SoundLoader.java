package resourcemanager.loaders;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;
import resourcemanager.ResourceLoader;


public class SoundLoader extends ResourceLoader {

    public SoundLoader () {
        super();
        this.registerExtension("wav");
        this.registerExtension("aiff");
        this.registerExtension("au");
        this.registerExtension("mid");
        this.registerExtension("rmf");
    }

    @Override
    public Class<?> getFileType () {
        return AudioClip.class;
    }

    @Override
    public Object loadFile (URL path) {
        return Applet.newAudioClip(path);
    }

}
