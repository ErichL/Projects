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

#include <stdint.h>
#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <map>
#include <string>
#include <vector>

#include "./HttpRequest.h"
#include "./HttpUtils.h"
#include "./HttpConnection.h"

using std::map;
using std::string;

namespace hw4 {

bool HttpConnection::GetNextRequest(HttpRequest *request) {
  // Use "WrappedRead" to read data into the buffer_
  // instance variable.  Keep reading data until either the
  // connection drops or you see a "\r\n\r\n" that demarcates
  // the end of the request header.
  //
  // Once you've seen the request header, use ParseRequest()
  // to parse the header into the *request argument.
  //
  // Very tricky part:  clients can send back-to-back requests
  // on the same socket.  So, you need to preserve everything
  // after the "\r\n\r\n" in buffer_ for the next time the
  // caller invokes GetNextRequest()!

  // MISSING:
  size_t pos = buffer_.find("\r\n\r\n");
  if (pos == std::string::npos) {
    int res;
    unsigned char buf[1024];
    while (1) {
      res = WrappedRead(fd_, buf, 1024);
      // Fatal Error
      if (res == -1) {
        return false;
      } else if (res == 0) {
        // EOF or Connection dropped
        break;
      } else {
        buffer_.append(reinterpret_cast<char*>(buf), res);
        // If we see "\r\n\r\n" break the connection
        pos = buffer_.find("\r\n\r\n");
        if (pos != std::string::npos)
          break;
      }
    }
  }

  *request = ParseRequest(pos + 4);
  // If the request is bad return false
  if (request->URI == "BAD_") {
    request = NULL;
    return false;
  }
  // remove \r\n\r\n from the buffer
  buffer_ = buffer_.substr(pos + 4);
  return true;
}

bool HttpConnection::WriteResponse(const HttpResponse &response) {
  std::string str = response.GenerateResponseString();
  int res = WrappedWrite(fd_,
                         (unsigned char *) str.c_str(),
                         str.length());
  if (res != static_cast<int>(str.length()))
    return false;
  return true;
}

HttpRequest HttpConnection::ParseRequest(size_t end) {
  HttpRequest req;
  req.URI = "/";  // by default, get "/".

  // Get the header.
  std::string str = buffer_.substr(0, end);

  // Split the header into lines.  Extract the URI from the first line
  // and store it in req.URI.  For each additional line beyond the
  // first, extract out the header name and value and store them in
  // req.headers (i.e., req.headers[headername] = headervalue).
  // You should look at HttpResponse.h for details about the HTTP header
  // format that you need to parse.
  //
  // You'll probably want to look up boost functions for (a) splitting
  // a string into lines on a "\r\n" delimiter, (b) trimming
  // whitespace from the end of a string, and (c) converting a string
  // to lowercase.

  // MISSING:

  // Split string into lines
  std::vector<std::string> head_split;
  boost::split(head_split, str, boost::is_any_of("\r\n"),
               boost::token_compress_on);

  if (head_split.size() <= 1) {
    req.URI = "BAD_";
    return req;
  }

  // Trim whitespace
  for (size_t i = 0; i < head_split.size(); i++) {
    boost::trim(head_split[i]);
  }

  // Extract the URI from the first line and store it in req.URI
  // Split first line using " " delimiter
  std::vector<std::string> line_split;
  boost::split(line_split, head_split[0], boost::is_any_of(" "),
    boost::token_compress_on);
  int sizel = line_split.size();
  if (line_split[0] != "GET") {
    req.URI = "BAD_";
    return req;
  }
  if (sizel == 2) {
    if (line_split[1][0] != '/' &&
  line_split[1].find("HTTP/") == std::string::npos) {
      req.URI = "BAD_";
      return req;
    }
  } else if (sizel == 3) {
    if (line_split[1][0] != '/' ||
        line_split[2].find("HTTP/") == std::string::npos) {
      req.URI = "BAD_";
      return req;
    }
    req.URI = line_split[1];
  } else if (sizel > 3) {
    req.URI = "BAD_";
    return req;
  }

  std::vector<std::string> header;
  for (size_t i = 1; i < head_split.size() - 1; i++) {
    size_t col_pos = head_split[i].find(": ");

    // Make sure the header name and value are
    // in the right format
    if (col_pos == std::string::npos) {
      req.URI = "BAD_";
      req.headers.clear();
      return req;
    }

    // Make headername lowercase and store
    // headername and headervalue in req.headers
    std::string headername = head_split[i].substr(0, col_pos);
    boost::to_lower(headername);
    std::string headervalue = head_split[i].substr(col_pos + 2);
    req.headers[headername] = headervalue;
  }

  return req;
}

}  // namespace hw4
