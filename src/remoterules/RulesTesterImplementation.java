package remoterules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import xmlfacts.AssertXMLFact;

public class RulesTesterImplementation 
     extends UnicastRemoteObject
  implements RulesTesterInterface
{
  @SuppressWarnings("compatibility:3950036629120367661")
  private final static long serialVersionUID = 1L;
  
  private RulesTesterImplementation instance = this;

  private transient Registry registry     = null;
  private int registryPort                = 1100; // Default value
  
  private transient Thread serverThread = null;
  
  public RulesTesterImplementation(int i, 
                                  RMIClientSocketFactory rmiClientSocketFactory,
                                  RMIServerSocketFactory rmiServerSocketFactory)
    throws RemoteException
  {
    super(i, rmiClientSocketFactory, rmiServerSocketFactory);
  }

  public RulesTesterImplementation(int i)
    throws RemoteException
  {
    super(i);
  }

  public RulesTesterImplementation()
    throws RemoteException
  {
    super();
  }

  public String runRules(String[] args) throws RemoteException, Exception
  {
    String outputFactsLocation = findPrm(args, "-facts-output");
    if (outputFactsLocation == null || outputFactsLocation.trim().length() == 0)
    {
      throw new RemoteException("Missing Parameter -facts-output");
    }
    
    String remainingFacts = "";
    AssertXMLFact.run(args, null);

    try
    {
      BufferedReader br = new BufferedReader(new FileReader(outputFactsLocation + File.separator + "allfacts.xml"));
      String line = "";
      while (line != null)
      {
        line = br.readLine();
        if (line != null)
          remainingFacts += (line + "\n");
      }
      br.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return remainingFacts;
  }
  
  private static String findPrm(String[] args, String prmName)
  {
    String prmValue = null;
    for (int i=0; i<args.length; i++)
    {
      if (prmName.equals(args[i]))
      {
        prmValue = args[i + 1];
        break;
      }
    }    
    return prmValue;
  }

  public void stopTestServer() throws RemoteException
  {
    try
    {
      System.out.println("Stoping RMI Server " + name);
      Naming.unbind(name);  
      System.out.println("Stopped.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    synchronized (instance) { instance.notify(); }
  }
  
  public void start()
  {
    try
    {
      registry = LocateRegistry.createRegistry(registryPort);
      System.out.println("Registry started");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    Thread t = new Thread()
      {
        public void run()
        {
          try
          {
            System.out.println("...TestServer Waiting");
            serverThread = this;
            synchronized (instance) { instance.wait(); }
            System.out.println("TestServer is done waiting.");
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          finally
          {
            System.out.println("Done with ServerThread.");
            System.exit(0);
          }
        }
      };
    t.start();    
  }
  private static String name = "";
  
  public static void main(String[] args)
  {
    try
    {
      RulesTesterImplementation server = new RulesTesterImplementation();
      name = System.getProperty("server.name", "rmi://localhost:" + Integer.toString(server.registryPort) + "/RulesTesterServer");  
      System.out.println("Server: server.name=" + name);
      
      server.start();
      System.out.println("Now binding.");
      
      Naming.rebind(name, server);      
      System.out.println(name + " is ready for duty.");
  //  server.start();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    System.out.println("Testing server is on its own.");
  }

  public void setRegistryPort(int registryPort)
  {
    this.registryPort = registryPort;
  }
}
