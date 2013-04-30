package resourcemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import resourcemanager.exceptions.FileNotSupportedException;


/**
 * This class manages the loading of all resources in the game.
 * It supports any kind of file and uses the correct ResourceLoader
 * in order to load the files of a certain type. The type of the file
 * is determined by the extension.
 * 
 * @author Jonathan Schmidt
 * 
 */
public class ResourceManager {

    private static final String EXTENSION_NOT_SUPPORTED =
            "The file %s has extension %s which is not supported by the Resource Manager.";

    private static ResourceManager myInstance = new ResourceManager();

    /**
     * Maps the extension of a file to the appropriate Resource Loader.
     */
    private Map<String, ResourceLoader> myLoaderMap;

    /**
     * Stores all of the information that the Resource Manager has loaded.
     */
    private Map<URL, Object> myResourceStorage;

    /**
     * The queue of files to process.
     */
    private Queue<URL> myLoadQueue;

    /**
     * Thread used to load all the files.
     */
    private Thread myLoadThread;

    private String myResourceBase;

    private ResourceManager () {
        myLoaderMap = new HashMap<String, ResourceLoader>();
        myResourceStorage = new HashMap<URL, Object>();
        myLoadQueue = new LinkedList<URL>();
        myLoadThread = new Thread();
        myResourceBase = "";
    }

    /**
     * Adds a Resource Loader to the Resource Manager and registers
     * the types of files that the loader can handle.
     * 
     * @param loader The loader to register with the Resource Manager
     */
    public void registerResourceLoader (ResourceLoader loader) {
        for (String ext : loader.getSupportedExtensions()) {
            myLoaderMap.put(ext, loader);
        }
    }

    /**
     * Sets the base resource folder if you want to specify one.
     * This is where it will look for relative files first.
     * 
     * @param base The base resource folder.
     */
    public void setResourceBase (String base) {
        myResourceBase = base;
    }

    /**
     * Returns whether the Manager is still loading files.
     * 
     * @return whether it is still loading.
     */
    public boolean isLoading () {
        synchronized (myLoadThread) {
            return myLoadThread.isAlive();
        }
    }

    /**
     * Queues a file for loading. Adds it to the list but only
     * loads the file from the disk when load() is called.
     * 
     * @param filename The file name of the file to load.
     * @return Whether it was able to queue the file or not.
     * @throws FileNotSupportedException If the file is not supported by the Manager.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public boolean queueFile (String filename) throws FileNotSupportedException,
                                              FileNotFoundException {
        URL file = getFileName(filename);
        return queueFile(file);
    }

    /**
     * Queues a file for loading. Adds it to the list but only
     * loads the file from the disk when load() is called.
     * 
     * @param filename The file name of the file to load.
     * @return Whether it was able to queue the file or not.
     * @throws FileNotSupportedException If the file is not supported by the Manager
     */
    public boolean queueFile (URL filename) throws FileNotSupportedException {
        if (filename == null) {
            return false;
        }

        String ext = getExtension(filename.getPath());
        if (!myLoaderMap.containsKey(ext)) {
            throw new FileNotSupportedException(String.format(EXTENSION_NOT_SUPPORTED,
                                                              filename.getPath(), ext));
        }

        synchronized (myResourceStorage) {
            if (myResourceStorage.containsKey(filename)) {
                return false;
            }
        }
        synchronized (myLoadQueue) {
            myLoadQueue.add(filename);
            return true;
        }
    }

    /**
     * Starts loading the resources that have been queued.
     */
    public void load () {
        if (!isLoading()) {
            myLoadThread = new Thread(new Runnable() {
                @Override
                public void run () {
                    loadFiles();
                }
            });
            myLoadThread.start();
        }
    }

    /**
     * Loads all of the files in the queue.
     */
    private void loadFiles () {
        synchronized (myLoadQueue) {
            while (!myLoadQueue.isEmpty()) {
                URL nextFile = myLoadQueue.poll();
                loadFile(nextFile);
            }
        }
    }

    /**
     * Loads a file with a specified filename.
     * Passes it off to the correct resource loader.
     * 
     * @param filename
     */
    private void loadFile (URL filename) {
        String ext = getExtension(filename.getPath());
        ResourceLoader rl = myLoaderMap.get(ext);
        Object loaded = rl.loadFile(filename);
        synchronized (myResourceStorage) {
            myResourceStorage.put(filename, loaded);
        }
    }

    /**
     * Returns a file with the specified file name.
     * If it's already loaded, then it returns the file.
     * If not, it then loads the files from the disk using
     * the correct loader and stores it before returning it.
     * 
     * @param filename The filename to load.
     * @param resourceType The class type of resource that the file is of.
     * @return The object that was loaded.
     */
    public <T> T getFile (String filename, Class<T> resourceType) {
        URL file;
        try {
            file = getFileName(filename);
        }
        catch (FileNotFoundException e1) {
            return null;
        }

        try {
            if (queueFile(file)) {
                load();
                try {
                    // Wait for loading to complete
                    myLoadThread.join();
                }
                catch (InterruptedException e) {
                    System.out.println("Error waiting for threads.");
                }
            }
        }
        catch (FileNotSupportedException e) {
            return null;
        }

        Object loadedFile = myResourceStorage.get(file);
        if (resourceType.isAssignableFrom(loadedFile.getClass())) {
            return resourceType.cast(loadedFile);
        }
        return null;
    }

    /**
     * Converts the string filename to a URL.
     * If the file is not relative to the resources folder
     * then it needs to be an absolute path.
     * To set the absolute path, call setResourceBase() with
     * the resource folder location relative to the project.
     * e.g. "/vooga/rts/resources/"
     * 
     * @param filename The filename of the file to convert
     * @return The converted URL
     * @throws FileNotFoundException
     */
    private URL getFileName (String filename) throws FileNotFoundException {
        URL f = getClass().getResource(myResourceBase + filename);
        if (f == null) {

            f = getClass().getResource(filename);
            if (f != null) {
                return f;
            }
            else {
                File file = new File(filename);
                try {
                    return new URL(file.getPath());
                }
                catch (MalformedURLException e1) {
                    throw new FileNotFoundException(filename);
                }
            }
        }
        return f;
    }

    /**
     * Returns the instance of the Resource Manager
     * 
     * @return Reference to the Resource Manager
     */
    public static ResourceManager getInstance () {
        return myInstance;
    }

    /**
     * Returns the extension of a particular filename.
     * 
     * @param filename The file name including the extension
     * @return The extension
     */
    public static String getExtension (String filename) {
        int index = filename.lastIndexOf(".");
        if (index > 0) {
            return filename.substring(index + 1);
        }
        return "";
    }

}
