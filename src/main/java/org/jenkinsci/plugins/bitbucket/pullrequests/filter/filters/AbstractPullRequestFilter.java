/*
 * The MIT License
 *
 * Copyright (c) 2021, Krzysztof Kacprzak.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.bitbucket.pullrequests.filter.filters;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.eclipse.jgit.annotations.NonNull;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.TypeFilter;

import java.io.IOException;

public abstract class AbstractPullRequestFilter<T> extends SCMHeadFilter {

    private final TypeFilter<T> filter;

    protected AbstractPullRequestFilter(TypeFilter filter) {
        this.filter = filter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExcluded(@NonNull SCMSourceRequest scmSourceRequest, @NonNull SCMHead scmHead) throws IOException, InterruptedException {
        if (scmSourceRequest instanceof BitbucketSCMSourceRequest && scmHead instanceof PullRequestSCMHead) {
            BitbucketSCMSourceRequest request = (BitbucketSCMSourceRequest) scmSourceRequest;
            for (BitbucketPullRequest pullRequest : request.getPullRequests()) {
                if (pullRequest.getSource().getBranch().getName().equals(((PullRequestSCMHead) scmHead).getBranchName())) {
                    BitbucketPullRequest fullPullRequest = request.getPullRequestById(Integer.parseInt(pullRequest.getId()));
                    return !isAccepted(fullPullRequest);
                }
            }
        }

        return false;
    }

    /**
     * @param pullRequest
     * @return
     */
    protected boolean isAccepted(BitbucketPullRequest pullRequest) {
        TypeFilter filter = getFilter();
        if (filter == null) {
            return true;
        }
        return filter.accepted(getData(pullRequest));
    }

    /**
     * @return
     */
    protected TypeFilter getFilter() {
        return filter;
    }

    /**
     * @param pullRequest
     * @return
     */
    protected abstract T getData(BitbucketPullRequest pullRequest);

}
