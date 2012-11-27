package remoterules;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RulesTesterInterface
  extends Remote
{
  public String runRules(String[] args) throws RemoteException, Exception;
  public void stopTestServer() throws RemoteException;
}
