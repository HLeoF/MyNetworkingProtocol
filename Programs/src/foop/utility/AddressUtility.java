package foop.utility;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class AddressUtility {
  /**
   * Find a "best" local address
   * 
   * @return "best" local address or null if no candidate addresses
   * @throws SocketException if problem getting addresses
   */
  public static InetAddress getAddress() throws SocketException {
    // Get list of interfaces
    Enumeration<NetworkInterface> interfaceList = NetworkInterface
        .getNetworkInterfaces();
    // No interfaces?
    if (interfaceList == null) {
      throw new SocketException("No interfaces");
    }
    // Iterate over interfaces, searching for best address
    InetAddress bestAddress = null;  // Best address
    while (interfaceList.hasMoreElements()) {
      // Get next interface and see if it meets minimal standard
      NetworkInterface iface = interfaceList.nextElement();
      if (!interfaceOk(iface)) {
        continue;
      }
      // Iterate over addresses of interface searching for best address
      Enumeration<InetAddress> addrList = iface.getInetAddresses();
      while (addrList.hasMoreElements()) {
        InetAddress address = addrList.nextElement();
        if (bestAddress == null || addressRank(bestAddress) < addressRank(address)) {
          bestAddress = address;
        }
      }
    }
    
    return bestAddress;
  }
  
  /**
   * Determines if network interface is minimally acceptable
   * 
   * @param i interface to evaluate
   * 
   * @return true if interface is acceptable
   * @throws SocketException if problem evaluating interface
   */
  public static boolean interfaceOk(NetworkInterface i) throws SocketException {
    return i.isUp();
  }
  
  /**
   * Returns ranking (higher is better) of given address
   * 
   * @param a address to evaluate
   * @return rank of address - 0 (lowest) to 3 (highest)
   */
  public static int addressRank(InetAddress a) {
    if (!(a instanceof Inet4Address) || a.isMulticastAddress()) {
      return 0;  // if not IPv4 or if multicast then bad
    } else if (a.isLoopbackAddress()) {
      return 1;  // if loopback
    } else if (a.isAnyLocalAddress() || a.isLinkLocalAddress() || a.isSiteLocalAddress()) {
      return 2; // if private/local address
    } else {
      return 3;  // if globally-routable address
    }
  }
}
