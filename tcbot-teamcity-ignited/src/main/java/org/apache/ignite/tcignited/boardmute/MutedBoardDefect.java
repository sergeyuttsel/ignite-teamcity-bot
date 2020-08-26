/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.tcignited.boardmute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MutedBoardDefect {
    private int id;

    private String trackedBranchCid;

    private HashMap<String, MutedBoardIssueInfo> mutedBoardIssues = new HashMap<>();

    public MutedBoardDefect(int id, String trackedBranchCid) {
        this.id = id;
        this.trackedBranchCid = trackedBranchCid;

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrackedBranchCid() {
        return trackedBranchCid;
    }

    public void setTrackedBranchCid(String trackedBranchCid) {
        this.trackedBranchCid = trackedBranchCid;
    }

    public HashMap<String, MutedBoardIssueInfo> getMutedIssues() {
        return mutedBoardIssues;
    }

    public void setMutedIssues(HashMap<String, MutedBoardIssueInfo> mutedBoardIssue) {
        this.mutedBoardIssues = mutedBoardIssue;
    }

    public boolean isTestMuted(String issueName) {
        return mutedBoardIssues.get(issueName) != null;
    }

    public String userName(String issueName) {
        MutedBoardIssueInfo issueInfo = mutedBoardIssues.get(issueName);
        if (issueInfo != null)
            return issueInfo.getUserName();
        else
            return null;
    }
}