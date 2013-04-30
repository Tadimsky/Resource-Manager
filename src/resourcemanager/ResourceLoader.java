package resourcemanager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public abstract class ResourceLoader {

    private List<String> mySupportedExtensions;

    public ResourceLoader () {
        mySupportedExtensions = new ArrayList<String>();
    }

    /**
     * Returns the type of object that this Resource Loader can handle.
     * 
     * @return The object type
     */
    public abstract Class<?> getFileType ();

    /**
     * Returns the extensions of files that this Resource Loader
     * can load and process.
     * 
     * @return List of Extensions
     */
    public List<String> getSupportedExtensions () {
        return mySupportedExtensions;
    }

    /**
     * Loads file with the specified file name.
     * @param path The file path to load
     * @return The loaded object
     */
    public abstract Object loadFile (URL path);

    /**
     * Registers an extension that this Resource Loader can handle.
     * 
     * @param ext The extension of the file.
     */
    public void registerExtension (String ext) {
        mySupportedExtensions.add(ext);
    }
}
