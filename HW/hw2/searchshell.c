/*
 * Copyright 2011 Steven Gribble
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

// Feature test macro for strtok_r (c.f., Linux Programming Interface p. 63)
#define _XOPEN_SOURCE 600

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>

#include "libhw1/CSE333.h"
#include "memindex.h"
#include "filecrawler.h"

static void Usage(void);

int main(int argc, char **argv) {
  if (argc != 2)
    Usage();

  // Implement searchshell!  We're giving you very few hints
  // on how to do it, so you'll need to figure out an appropriate
  // decomposition into functions as well as implementing the
  // functions.  There are several major tasks you need to build:
  //
  //  - crawl from a directory provided by argv[1] to produce and index
  //  - prompt the user for a query and read the query from stdin, in a loop
  //  - split a query into words (check out strtok_r)
  //  - process a query against the index and print out the results
  //
  // When searchshell detects end-of-file on stdin (cntrl-D from the
  // keyboard), searchshell should free all dynamically allocated
  // memory and any other allocated resources and then exit.
  char reader[1000];
  char delimiter[2]=" ";
  char *token;
  char *ptr;
  int res;
  int i;
  DocTable dt;
  MemIndex idx;
  LinkedList list;
  SearchResult *searchres;
  LLIter iter;
  printf("Indexing \'%s\'\n", argv[1]);

  res = CrawlFileTree(argv[1], &dt, &idx);
  // If CrawlFileTree fails
  if (res == 0) Usage();
  Verify333(dt != NULL);
  Verify333(idx != NULL);

  while (1) {
    printf("enter query:\n");
    if (fgets(reader, 1000, stdin) != NULL) {
      char **query = (char **) malloc(500 * sizeof(char *));
      Verify333(query != NULL);
      char *input = reader;
      int q_length = 0;
      while (1) {
        token = strtok_r(input, delimiter, &ptr);
        if (token == NULL) break;
        query[q_length] = token;
        q_length++;
        input = NULL;
      }
      char *strip = strchr(query[q_length - 1], '\n');
        if (strip) *strip = '\0';
      // Process query
      list = MIProcessQuery(idx, query, q_length);
      if (list != NULL) {
        iter = LLMakeIterator(list, 0);
        Verify333(iter != NULL);
        for (i = 0; i < NumElementsInLinkedList(list); i++) {
          LLIteratorGetPayload(iter, (LLPayload_t*) &searchres);
          printf("  %s (%u)\n", \
           DTLookupDocID(dt, searchres->docid), searchres->rank);
          LLIteratorNext(iter);
        }
        LLIteratorFree(iter);
        FreeLinkedList(list, &free);
      }
      free(query);
    } else {
      break;
    }
  }

  // Free MemIndex and DocTable after usage
  FreeMemIndex(idx);
  FreeDocTable(dt);
  return EXIT_SUCCESS;
}

static void Usage(void) {
  fprintf(stderr, "Usage: ./searchshell <docroot>\n");
  fprintf(stderr,
          "where <docroot> is an absolute or relative " \
          "path to a directory to build an index under.\n");
  exit(-1);
}

