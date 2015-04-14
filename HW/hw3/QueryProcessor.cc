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

#include <iostream>
#include <algorithm>

#include "./QueryProcessor.h"

extern "C" {
  #include "./libhw1/CSE333.h"
}

namespace hw3 {

// Returns a list of docid_element_header where both lists have an
// docid_element_header with the same docID and sums the positions
static list<docid_element_header> CompareDocIDHeaderList(
       const list<docid_element_header> &l1,
       const list<docid_element_header> &l2);

QueryProcessor::QueryProcessor(list<string> indexlist, bool validate) {
  // Stash away a copy of the index list.
  indexlist_ = indexlist;
  arraylen_ = indexlist_.size();
  Verify333(arraylen_ > 0);

  // Create the arrays of DocTableReader*'s. and IndexTableReader*'s.
  dtr_array_ = new DocTableReader *[arraylen_];
  itr_array_ = new IndexTableReader *[arraylen_];

  // Populate the arrays with heap-allocated DocTableReader and
  // IndexTableReader object instances.
  list<string>::iterator idx_iterator = indexlist_.begin();
  for (HWSize_t i = 0; i < arraylen_; i++) {
    FileIndexReader fir(*idx_iterator, validate);
    dtr_array_[i] = new DocTableReader(fir.GetDocTableReader());
    itr_array_[i] = new IndexTableReader(fir.GetIndexTableReader());
    idx_iterator++;
  }
}

QueryProcessor::~QueryProcessor() {
  // Delete the heap-allocated DocTableReader and IndexTableReader
  // object instances.
  Verify333(dtr_array_ != nullptr);
  Verify333(itr_array_ != nullptr);
  for (HWSize_t i = 0; i < arraylen_; i++) {
    delete dtr_array_[i];
    delete itr_array_[i];
  }

  // Delete the arrays of DocTableReader*'s and IndexTableReader*'s.
  delete[] dtr_array_;
  delete[] itr_array_;
  dtr_array_ = nullptr;
  itr_array_ = nullptr;
}

vector<QueryProcessor::QueryResult>
QueryProcessor::ProcessQuery(const vector<string> &query) {
  Verify333(query.size() > 0);
  vector<QueryProcessor::QueryResult> finalresult;

  // MISSING:

  // Look up the first query word (query[0]) in the inverted
  // index.  For each document that matches, allocate a QueryResult
  // structure.  Initialize that QueryResult structure with the
  // filename, and the initial computed rank for the document.  (The
  // initial computed rank is the number of times the word appears
  // in that document.)
  //
  // Then, append the QueryResult structure onto finalresult
  // If there are no matching documents, continue to the next index file

  for (HWSize_t i; i < arraylen_; i++) {
    DocTableReader *dtr = dtr_array_[i];
    IndexTableReader *itr = itr_array_[i];
    list<docid_element_header> res;
    DocIDTableReader *IDtr = itr->LookupWord(query[0]);
    if (IDtr == NULL) {
      continue;
    }
    res = IDtr->GetDocIDList();
    delete IDtr;
    // Great; we have our search results for the first query
    // word.  If there is only one query word, go to the next
    // index file
    if (query.size() == 1) {
      list<docid_element_header>::iterator iter;
      string document_name;
      for (iter = res.begin(); iter != res.end(); iter++) {
        QueryResult qres;
        Verify333(dtr->LookupDocID(iter->docid, &document_name));
        qres.document_name = document_name;
        qres.rank = iter->num_positions;
        finalresult.push_back(qres);
      }
      continue;
    }
    // Handling the additional queries one at a time
    for (HWSize_t i = 1; i < query.size(); i++) {
      // Look up the next query word (query[i]) in the inverted index.
      // If there are no matches, it means the overall query
      // should return no documents, so clear the result and
      // break out of the loop
      IDtr = itr->LookupWord(query[i]);
      if (IDtr == NULL) {
        res.clear();
        break;
      }
      std::list<docid_element_header> reslist = IDtr->GetDocIDList();
      delete IDtr;
      // There are matches.  We're going to iterate through
      // the filenames in our current search result list, testing each
      // to see whether it is also in the set of matches for
      // the previous queries
      // We do this by using CompareDocIDHeaderList to find docIDs in both
      // lists and adding their num_positions together
      // If there are no matches then the docid_element_header won't be added
      // to the list of them.
      res = CompareDocIDHeaderList(res, reslist);
      if (res.size() == 0)
        break;
    }

    // If there are results after looking at all the queries iterate
    // through the list of docid_element_header and create a QueryResult
    // for each filename by looking up their docid to get the filename
    // and the rank that is given by the num_positions
    if (res.size() != 0) {
      list<docid_element_header>::iterator iter;
      string document_name;
      for (iter = res.begin(); iter != res.end(); iter++) {
        QueryResult qres;
        Verify333(dtr->LookupDocID(iter->docid, &document_name));
        qres.document_name = document_name;
        qres.rank = iter->num_positions;
        finalresult.push_back(qres);
      }
    }
  }

  // Sort the final results.
  std::sort(finalresult.begin(), finalresult.end());
  return finalresult;
}
static list<docid_element_header> CompareDocIDHeaderList(
       const list<docid_element_header> &doclist1,
       const list<docid_element_header> &doclist2) {
  list<docid_element_header> retlist;
  list<docid_element_header>::const_iterator iter1;
  list<docid_element_header>::const_iterator iter2;
  for (iter1 = doclist1.begin(); iter1 != doclist1.end(); iter1++) {
    for (iter2 = doclist2.begin(); iter2 != doclist2.end(); iter2++) {
      if (iter1->docid == iter2->docid) {
        docid_element_header current;
        current.docid = iter1->docid;
        current.num_positions = iter1->num_positions + iter2->num_positions;
        retlist.push_back(current);
        break;
      }
    }
  }
  return retlist;
}
}  // namespace hw3
