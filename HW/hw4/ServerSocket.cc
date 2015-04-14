/*
 * Copyright 2012 Steven Gribble
 *
 *  This file is part of the UW CSE 333 course project sequence
 *  (333proj).
 *
 *  333proj is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  333proj is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 333proj.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdio.h>       // for snprintf()
#include <unistd.h>      // for close(), fcntl()
#include <sys/types.h>   // for socket(), getaddrinfo(), etc.
#include <sys/socket.h>  // for socket(), getaddrinfo(), etc.
#include <arpa/inet.h>   // for inet_ntop()
#include <netdb.h>       // for getaddrinfo()
#include <errno.h>       // for errno, used by strerror()
#include <string.h>      // for memset, strerror()
#include <iostream>      // for std::cerr, etc.

#include "./ServerSocket.h"

extern "C" {
  #include "libhw1/CSE333.h"
}

namespace hw4 {

ServerSocket::ServerSocket(uint16_t port) {
  port_ = port;
  listen_sock_fd_ = -1;
}

ServerSocket::~ServerSocket() {
  // Close the listening socket if it's not zero.  The rest of this
  // class will make sure to zero out the socket if it is closed
  // elsewhere.
  if (listen_sock_fd_ != -1)
    close(listen_sock_fd_);
  listen_sock_fd_ = -1;
}

bool ServerSocket::BindAndListen(int ai_family, int *listen_fd) {
  // Use "getaddrinfo," "socket," "bind," and "listen" to
  // create a listening socket on port port_.  Return the
  // listening socket through the output parameter "listen_fd".

  // MISSING:
  struct addrinfo hints;
  struct addrinfo *result;
  std::string port = std::to_string(port_);
  int s;

  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = ai_family;    /* Allow IPv4 or IPv6 */
  hints.ai_socktype = SOCK_STREAM; /* Stream socket */
  hints.ai_flags = AI_PASSIVE;    /* For wildcard IP address */
  hints.ai_protocol = IPPROTO_TCP;          /* TCP */
  hints.ai_canonname = NULL;
  hints.ai_addr = NULL;
  hints.ai_next = NULL;

  s = getaddrinfo(NULL, port.c_str(), &hints, &result);
  if (s != 0) {
    std::cerr << "getaddrinfo() failed: ";
    std::cerr << gai_strerror(s) << std::endl;
    return -1;
  }


  // Loop through the returned address structures until we are able
  // to create a socket and bind to one.  The address structures are
  // linked in a list through the "ai_next" field of result.
  int listening_fd = -1;
  for (struct addrinfo *rp = result; rp != NULL; rp = rp->ai_next) {
    listening_fd = socket(rp->ai_family,
                       rp->ai_socktype,
                       rp->ai_protocol);
    if (listening_fd == -1) {
      // Creating this socket failed.  So, loop to the next returned
      // result and try again.
      std::cerr << "socket() failed: " << strerror(errno) << std::endl;
      listening_fd = -1;
      continue;
    }

    // Configure the socket; we're setting a socket "option."  In
    // particular, we set "SO_REUSEADDR", which tells the TCP stack
    // so make the port we bind to available again as soon as we
    // exit, rather than waiting for a few tens of seconds to recycle it.
    int optval = 1;
    setsockopt(listening_fd, SOL_SOCKET, SO_REUSEADDR,
               &optval, sizeof(optval));

    // Try binding the socket to the address and port number returned
    // by getaddrinfo().
    if (bind(listening_fd, rp->ai_addr, rp->ai_addrlen) == 0) {
      // Bind worked!  Return to the caller the address family.
      sock_family_ = rp->ai_family;
      break;
    }

    // The bind failed.  Close the socket, then loop back around and
    // try the next address/port returned by getaddrinfo().
    close(listening_fd);
    listening_fd = -1;
  }

  // Free the structure returned by getaddrinfo().
  freeaddrinfo(result);

  // If we failed to bind, return failure.
  if (listening_fd <= 0)
    return listening_fd;

  // Success. Tell the OS that we want this to be a listening socket.
  if (listen(listening_fd, SOMAXCONN) != 0) {
    std::cerr << "Failed to mark socket as listening: ";
    std::cerr << strerror(errno) << std::endl;
    close(listening_fd);
    return -1;
  }

  // store the listening socket file descriptor in field and output parameter
  listen_sock_fd_ = listening_fd;
  *listen_fd = listening_fd;
  return true;
}

bool ServerSocket::Accept(int *accepted_fd,
                          std::string *client_addr,
                          uint16_t *client_port,
                          std::string *client_dnsname,
                          std::string *server_addr,
                          std::string *server_dnsname) {
  // Accept a new connection on the listening socket listen_sock_fd_.
  // (Block until a new connection arrives.)  Return the newly accepted
  // socket, as well as information about both ends of the new connection,
  // through the various output parameters.

  // MISSING:

  struct sockaddr_storage client;
  socklen_t addrlen = sizeof(client);
  struct sockaddr *addr = reinterpret_cast<struct sockaddr *>(&client);
  int client_fd = -1;
  while (1) {
    client_fd = accept(listen_sock_fd_,
                       addr,
                       &addrlen);

    if (client_fd < 0) {
      if ((errno == EAGAIN) || (errno == EINTR))
        continue;
      return false;
    }
    break;
  }

  if (client_fd < 0)
    return false;

  // Output client_fs in accepted_fs
  *accepted_fd = client_fd;

  // Output client_addr and client_port
  // If ipv4
  if (addr->sa_family == AF_INET) {
    char c_addr[INET_ADDRSTRLEN];
    struct sockaddr_in *ipv4_addr = \
    reinterpret_cast<struct sockaddr_in *>(addr);
    inet_ntop(AF_INET, &(ipv4_addr->sin_addr), c_addr, INET_ADDRSTRLEN);

    *client_addr = std::string(c_addr);
    *client_port = htons(ipv4_addr->sin_port);
  } else {  // If ipv6
    char c_addr[INET6_ADDRSTRLEN];
    struct sockaddr_in6 *ipv6_addr = \
    reinterpret_cast<struct sockaddr_in6 *>(addr);
    inet_ntop(AF_INET6, &(ipv6_addr->sin6_addr), c_addr, INET6_ADDRSTRLEN);

    *client_addr = std::string(c_addr);
    *client_port = htons(ipv6_addr->sin6_port);
  }

  // Output dnsname to client_dnsname
  char c_dnsname[1024];
  Verify333(getnameinfo(addr, addrlen, c_dnsname, 1024, NULL, 0, 0) == 0);
  *client_dnsname = std::string(c_dnsname);

  // Output server_addr and server_dnsname
  char s_dnsname[1024];
  s_dnsname[0] = '\0';
  // If ipv4
  if (sock_family_ == AF_INET) {
    struct sockaddr_in server_ipv4;
    socklen_t serverlen = sizeof(server_ipv4);
    char addrbuf[INET_ADDRSTRLEN];
    getsockname(client_fd, (struct sockaddr *) &server_ipv4, &serverlen);
    inet_ntop(AF_INET, &server_ipv4.sin_addr, addrbuf, INET_ADDRSTRLEN);
    getnameinfo((const struct sockaddr *) &server_ipv4,
                serverlen, s_dnsname, 1024, NULL, 0, 0);

    *server_addr = std::string(addrbuf);
    *server_dnsname = std::string(s_dnsname);
  } else {  // If ipv6
    struct sockaddr_in6 server_ipv6;
    socklen_t serverlen = sizeof(server_ipv6);
    char addrbuf[INET6_ADDRSTRLEN];
    getsockname(client_fd, (struct sockaddr *) &server_ipv6, &serverlen);
    inet_ntop(AF_INET6, &server_ipv6.sin6_addr, addrbuf, INET6_ADDRSTRLEN);
    getnameinfo((const struct sockaddr *) &server_ipv6,
                serverlen, s_dnsname, 1024, NULL, 0, 0);

    *server_addr = std::string(addrbuf);
    *server_dnsname = std::string(s_dnsname);
  }

  return true;
}

}  // namespace hw4
