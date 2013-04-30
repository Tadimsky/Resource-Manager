Resource Manager
================
Jonathan Schmidt

This bit of code is my multipurpose resource manager used for loading items into Java. It caches objects in memory in order to save generally inefficient loading.

The Resource Manager is meant to make it very easy to load things from disk into Java. The are two main components to the Resource Mananger: The ResourceManager class and the abstract ResourceLoader class.
The ResourceManager class is the proxy through which a developer deals. There are methods to load a file immediately (getFile) as well as methods to queue a file for loading which will only load at a later point (when the loadFiles method is called).
The ResourceManager is completely closed to the developer and they do not every need to touch it. The actual loading of files is done by the Resource Loaders which provides two abstract methods that the concrete class must implement, namely getFileType and load.

To enable the loading of different resources, the developer extends the ResourceLoader class and implements those two methods. The first one returns the class type that the loader loads files into. For example this could be BufferedImage, Sound, etc. The second method is the method that is called to actually load a file with the loader. This takes in a URL and provides an Object as a result. The code for loading the item goes in here.

The ResourceManager uses the singleton pattern, so there is only ever one of them in existence. This means that the resources are always cached and will be faster on subsequent loads. In addition to this, it means that the developer can use the resource manager anywhere in their code without providing a reference to it.
The ResourceManager supports file names that are relative to the entire Java application (or relative to the resource manager package), files with an absolute path ad files that are relative to a specified base location. This base location can be set by the developer. This base location would generally be a resources package or something like that.

# Usage
The way that a developer would use the ResourceManager is to first register the loaders that the ResourceManager can deal with and possible set a default base location.

	ResourceManager.getInstance().registerResourceLoader(new ImageLoader());
    ResourceManager.getInstance().registerResourceLoader(new SoundLoader());
    ResourceManager.getInstance().setResourceBase(DEFAULT_RESOURCE_LOCATION);

This lets the Resource Manager load Images and Sounds and specifies the default location to be the provided constant value.

Then, the developer could either queue a file for loading later as such:
	ResourceManager.getInstance().queueFile(filename);

And then call 
	ResourceManager.getInstance().load();	

This would then load the queued files into the ResourceManager so that access will be faster.

Whether you queue a file or not, you will still call:
	ResourceManager.getInstance().<BufferedImage> getFile(fileName, BufferedImage.class);

This method uses generics in order to return a value of the correct type. Unfortunately you have to specify the class type that you are returning as well as Java is not intelligent enough to do checks with just a generic.

I believe this code is a good example of what I have learnt because it uses advanced principles and topics such singleton pattern, generics and threads. It also is completely closed and modular in that a developer will never need to open this file again. All they do is write more advanced Resource Loaders if they so desire.

We used this loader in our CS 308 Vooga RTS Game and it provided much needed performance boosts on loading.