package javatools.jaxb;

import com.sun.tools.xjc.Driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * For JAX-B 2.0
 */
public class DynamicCompilationV2
{
  public static final int CREATE_JAR_FILE    = 1;
  public static final int CREATE_NO_JAR_FILE = 2;

  protected String jarFileName = "JAXBJar.jar";

  private boolean verbose = false;
  private URL schemaURL = null;
  private String destinationDirectory = null;
  private String destinationPackage = null;
  private String jaxbClassesDirectory = null;
  private int jarOption = -1;

  public DynamicCompilationV2(URL schemaURL,
                              String destinationDirectory,
                              String destinationPackage,
                              String jaxbClassesDirectory)
  {
    this(schemaURL,
         destinationDirectory,
         destinationPackage,
         jaxbClassesDirectory,
         false,
         CREATE_NO_JAR_FILE);
  }

  public DynamicCompilationV2(URL schemaURL,
                              String destinationDirectory,
                              String destinationPackage,
                              String jaxbClassesDirectory,
                              boolean verbose)
  {
    this(schemaURL,
         destinationDirectory,
         destinationPackage,
         jaxbClassesDirectory,
         verbose,
         CREATE_NO_JAR_FILE);
  }

  public DynamicCompilationV2(URL schemaURL,
                              String destinationDirectory,
                              String destinationPackage,
                              String jaxbClassesDirectory,
                              int jarOption)
  {
    this(schemaURL,
         destinationDirectory,
         destinationPackage,
         jaxbClassesDirectory,
         false,
         jarOption);
  }

  public DynamicCompilationV2(URL schemaURL,
                              String destinationDirectory,
                              String destinationPackage,
                              String jaxbClassesDirectory,
                              boolean v,
                              int jarOption)
  {
    this.verbose = v;
    this.schemaURL = schemaURL;
    this.destinationDirectory = destinationDirectory;
    this.destinationPackage = destinationPackage;
    this.jaxbClassesDirectory = jaxbClassesDirectory;
    this.jarOption = jarOption;
  }

  private boolean alreadyCompiled = true;

  public ArrayList<String> generate()
  {
    ArrayList<String> atr = new ArrayList<String>(1);
    long schemaModified = Long.MAX_VALUE;

    String jarFileName = schemaURL.toExternalForm().substring(schemaURL.toExternalForm().lastIndexOf("/") + 1);
    jarFileName = jarFileName.substring(0, jarFileName.indexOf(".xsd")) + ".jar";
    if (verbose)
      System.out.println("Jar Name:" + jarFileName);

    try
    {
      URLConnection con = schemaURL.openConnection();
      String lastModified = con.getHeaderField("Last-modified");
      if (verbose)
        System.out.println(schemaURL.toExternalForm() + ", Last Modified:[" + lastModified + "]");
      if (lastModified != null)
      {
        // like Tue, "21 Sep 2004 13:37:32 GMT"
        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
        Date age = df.parse(lastModified);
        schemaModified = age.getTime();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    File srcDirectory = new File(destinationDirectory);
    if (!srcDirectory.exists())
    {
      alreadyCompiled = false;
    }
    else
    {
      // Check directory content
      if (srcDirectory.isDirectory()) // very probable
      {
        FilenameFilter filter = new FilenameFilter()
          {
            public boolean accept(File dir, String name)
            {
              String str = dir.getAbsolutePath() + File.separator + name;
              File f = new File(str);
              return f.isDirectory() ||
                (f.isFile() && name.endsWith(".java"));
            }
          };
        long oldest = getOldestElement(srcDirectory, filter);
        if (oldest ==  Long.MAX_VALUE || oldest < schemaModified) // MAX_VALUE: empty directory
          alreadyCompiled = false;
        else
        {
          if (verbose)
          {
            System.out.println("Schema Modified:" + new Date(schemaModified));
            System.out.println("Oldest java file in " + srcDirectory + ":" + new Date(oldest));
          }
        }
      }
    }

    if (alreadyCompiled)
    {
      if (verbose)
        System.out.println("No compilation required");
    }
    else if (verbose)
      System.out.println("Compilation required");

    if (!alreadyCompiled)
    {
      // Recreate output directories
      try { srcDirectory.delete(); } catch (Exception ignore){ System.err.println(ignore.getLocalizedMessage()); }
      try { srcDirectory.mkdirs(); } catch (Exception ignore){ System.err.println(ignore.getLocalizedMessage()); }
      File compiledClasses = new File(jaxbClassesDirectory);
      try { compiledClasses.delete(); } catch (Exception ignore){ System.err.println(ignore.getLocalizedMessage()); }
      try { compiledClasses.mkdirs(); } catch (Exception ignore){ System.err.println(ignore.getLocalizedMessage()); }

      try
      {
        // JAX-B Compilation
        String[] compargs = null;
        if (destinationPackage == null || destinationPackage.trim().length() == 0)
          compargs = new String[] { "-extension",
                                    "-d", destinationDirectory,
                                    schemaURL.toString() };
        else
          compargs = new String[] { "-extension",
                                    "-d", destinationDirectory,
                                    "-p", destinationPackage,
                                    schemaURL.toString() };
        System.out.println("xjc Driver:" + Driver.getBuildID());
        Driver.run(compargs, System.err, System.out );

        if (verbose)
          System.out.println("JAX-B Compilation completed");

        // Generation Completed, now let's compile the generated java files
        Vector fList = drillDownDestinationDirectory(new File(destinationDirectory), null);

        String[] args = new String[6 + fList.size()];
        args[0] = "-d";
        args[1] = jaxbClassesDirectory;
        args[2] = "-sourcepath";
        args[3] = destinationDirectory;
        args[4] = "-classpath";
//      args[5] = jaxbClassesDirectory + File.pathSeparator + jaxbCompilerJars;
        args[5] = jaxbClassesDirectory + File.pathSeparator + System.getProperty("java.class.path");

        File destDir = new File(jaxbClassesDirectory);
        if (!destDir.exists())
          destDir.mkdirs();

        int argIdx = 6;
        if (verbose) System.out.println("Adding to compilation list:");
        Enumeration enumeration = fList.elements();
        while (enumeration.hasMoreElements())
        {
          String f = (String) enumeration.nextElement();
          if (f.endsWith(".java"))
          {
            args[argIdx++] = f;
            if (verbose)
              System.out.println("- " + f);
          }
        }

        if (verbose)
        {
          System.out.print("Running javac ");
          for (int i = 0; i < args.length; i++)
          {
            if (verbose)
              System.out.print(args[i] + " ");
          }
          System.out.println();
        }

        int status = com.sun.tools.javac.Main.compile(args);
        
        if (verbose)
          System.out.println("Java Compilation returned " + status);
        // Copy the properties file (JAXB 1.0) 
        if (srcDirectory.isDirectory() && false) // By-passed this block, it was for JAXB 1.0
        {
          FilenameFilter filter = new FilenameFilter()
            {
              public boolean accept(File dir, String name)
              {
                String str = dir.getAbsolutePath() + File.separator + name;
                File f = new File(str);
                return f.isDirectory() || (f.isFile() && name.endsWith(".properties"));
              }
            };
          Vector<File> vector = new Vector<File>();
          vector = getFileList(vector, srcDirectory, filter);
          enumeration = vector.elements();
          while (enumeration.hasMoreElements())
          {
            File origin = (File) enumeration.nextElement();
            String fileName = origin.getName();
            String destFileName = jaxbClassesDirectory + File.separator +
                                  destinationPackage.replace('.', File.separatorChar) +
                                  File.separator + fileName;
            File copied = new File(destFileName);
            FileInputStream fis  = new FileInputStream(origin);
            FileOutputStream fos = new FileOutputStream(copied);
            try
            {
              copy(fis, fos);
            }
            catch (IOException ioe)
            {
              ioe.printStackTrace();
            }
          }
        }
        if (jarOption == CREATE_JAR_FILE)
        {
          // List the files in the class directory
          Vector<File> toArchive = new Vector<File>();
          File classDir = new File(jaxbClassesDirectory);
          if (classDir.isDirectory()) // very probable
          {
            FilenameFilter filter = new FilenameFilter()
              {
                public boolean accept(File dir, String name)
                {
                  String str =
                    dir.getAbsolutePath() + File.separator + name;
                  File f = new File(str);
                  return (f.isFile() && !str.endsWith(".jar")) || f.isDirectory();
                }
              };
            toArchive = getFileList(toArchive, classDir, filter);
          }
          // Generate jar-file
          String jarFilePath = jaxbClassesDirectory + File.separator + jarFileName;
          createJarFile(toArchive.toArray(), jarFilePath, jaxbClassesDirectory);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    } // Conditional generation
    atr = getPackageList(jaxbClassesDirectory);

    if (jarOption == CREATE_NO_JAR_FILE)
    {
      try
      {
        File classDir = new File(jaxbClassesDirectory);
        System.out.println("Adding directory URL " + classDir.toURI().toURL().toString() + " to class loader");
        addURLToClassPath(classDir.toURI().toURL());
      }
      catch (MalformedURLException e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      try
      {
        File jar = new File(jaxbClassesDirectory + File.separator + jarFileName);
        addURLToClassPath(jar.toURI().toURL());
        System.out.println("Adding jar URL [" + jar.toURI().toURL().toString() + "] to class loader");
//      if (verbose) System.out.println("Classapth becomes[" + System.getProperty("java.class.path") + "]");
      }
      catch (MalformedURLException e)
      {
        e.printStackTrace();
      }
    }
    return atr;
  }

  private Vector drillDownDestinationDirectory(File f, Vector current) throws Exception
  {
    Vector v = current;
    if (v == null)
      v = new Vector();
    if (f.isDirectory())
    {
      File[] files = f.listFiles();
      for (int i=0; i<files.length; i++)
        v = drillDownDestinationDirectory(files[i], v);
    }
    else if (f.getAbsolutePath().endsWith(".java"))
    {
      v.add(f.getAbsolutePath());
    }
    return v;
  }

  private synchronized void addURLToClassPath(URL url)
  {
    if (verbose) System.out.println("Adding " + url.toString() + " to classpath");
    try
    {
//    URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Class sysClass = URLClassLoader.class;
      Class[] parameterTypes = new Class[] { URL.class };
      Method m = sysClass.getDeclaredMethod("addURL", parameterTypes);
      m.setAccessible(true);
      Object[] args = new Object[1];
      args[0] = url;
      m.invoke(urlClassLoader, args);

      if (verbose)
      {
        URL[] urls = urlClassLoader.getURLs();
        for (int i=0; i<urls.length; i++)
          System.out.println("-> " + urls[i].toString());
      }
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

  private void createJarFile(Object[] fList,
                             String jarFileName,
                             String classesDirectory)
  {
    JarEntry je = null;

    try
    {
      File jarFile = new File(jarFileName);
      if (jarFile.exists())
        jarFile.delete();
      FileOutputStream fos = new FileOutputStream(jarFile);
      JarOutputStream jos = new JarOutputStream(fos);
      jos.setLevel(1);

      for (int i = 0; i < fList.length; i++)
      {
        String realFileName = ((File) fList[i]).getAbsolutePath();
        String clsDirectory = new File(classesDirectory).getAbsolutePath();
        if (!realFileName.startsWith(clsDirectory))
          System.out.println("Class in the wrong directory:[" + realFileName + "], not in [" + classesDirectory + "]");
        else
        {
          String entryName = realFileName.substring(clsDirectory.length() + 1);
          if (verbose) System.out.println("Adding to jar: [" + entryName + "]");
          je = new JarEntry(entryName.replace(File.separatorChar, '/'));
          jos.putNextEntry(je);
          FileInputStream fis = new FileInputStream(new File(realFileName));
          try
          {
            copy(fis, jos);
          }
          catch (IOException ioe)
          {
            ioe.printStackTrace();
          }
          jos.closeEntry();
          fis.close();
        }
      }
      jos.close();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void copy(InputStream fis, OutputStream fos)
    throws IOException
  {
    synchronized (fis)
    {
      synchronized (fos)
      {
        byte[] buffer = new byte[256];
        while (true)
        {
          int byteRead = fis.read(buffer);
          if (byteRead == -1)
            break;
          fos.write(buffer, 0, byteRead);
        }
        fos.flush();
      }
    }
  }

  private long getOldestElement(File root, FilenameFilter filter)
  {
    long age = Long.MAX_VALUE;
    if (root.exists())
    {
      if (root.isDirectory())
      {
        File[] children = root.listFiles(filter);
        for (int i = 0; i < children.length; i++)
        {
          long dAge = getOldestElement(children[i], filter);
          if (dAge < age)
            age = dAge;
        }
      }
      else
      {
        long fAge = root.lastModified();
        if (fAge < age)
          age = fAge;
      }
    }
    else
      System.err.println("Problem:" + root.getName() + " does not exist");
    return age;
  }

  private Vector<File> getFileList(Vector<File> vector,
                                   File root,
                                   FilenameFilter filter)
  {
    if (root.exists())
    {
      if (root.isDirectory())
      {
        File[] children = root.listFiles(filter);
        for (int i = 0; i < children.length; i++)
        {
          vector = getFileList(vector, children[i], filter);
        }
      }
      else
        vector.add(root);
    }
    else
      System.err.println("Problem:" + root.getName() + " does not exist");
    return vector;
  }

  private ArrayList<String> getPackageList(String classesDir)
  {
    ArrayList<String> als = new ArrayList<String>(1);
    Vector<File> classList = new Vector<File>();
    FilenameFilter filter = new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          String str =
            dir.getAbsolutePath() + File.separator + name;
          File f = new File(str);
          return (f.isFile() && !str.endsWith(".jar")) || f.isDirectory();
        }
      };

    classList = getFileList(classList, new File(classesDir), filter);
    for (File f : classList)
    {
      String fullPath = f.getAbsolutePath();
      if (fullPath.endsWith(".class"))
      {
        File clsDir = new File(classesDir);
        if (!fullPath.startsWith(clsDir.getAbsolutePath()))
          System.out.println("Weird...: [" + fullPath + "] does not start with [" + clsDir.getAbsolutePath() + "]");
        else
        {
          String lastPart = fullPath.substring(clsDir.getAbsolutePath().length() + 1);
          try { lastPart = lastPart.substring(0, lastPart.lastIndexOf(File.separator)); } catch (Exception ignore) { ignore.printStackTrace(); }
      //  System.out.println("lastPart:[" + lastPart + "], classesDir=[" + classesDir + "] for clsDir=[" + clsDir.getAbsolutePath() + "]");
          String packName = lastPart.replace(File.separatorChar, '.');
          if (!als.contains(packName))
          {
            als.add(packName);
            System.out.println("Adding [" + packName + "] to package list");
          }
        }
      }
    }
    return als;
  }

  public boolean isAlreadyCompiled()
  {
    return alreadyCompiled;
  }
}
