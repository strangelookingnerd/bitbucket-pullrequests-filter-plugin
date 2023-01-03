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
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.TypeFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A {@link SCMHead} filter to exclusion the pull requests due to not match by user filter.
 *
 * @param <T> data type to validation
 * @since 0.1.0
 */
public abstract class AbstractPullRequestFilter<T> extends SCMHeadFilter {

    private final TypeFilter<T> filter;

    /**
     * Constructor.
     *
     * @param filter {@link TypeFilter} to validate the data
     */
    protected AbstractPullRequestFilter(TypeFilter<T> filter) {
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExcluded(@Nonnull SCMSourceRequest request, @Nonnull SCMHead head) throws IOException, InterruptedException {
        if (request instanceof BitbucketSCMSourceRequest && head instanceof PullRequestSCMHead) {
            BitbucketSCMSourceRequest req = (BitbucketSCMSourceRequest) request;
            for (BitbucketPullRequest pullRequest : req.getPullRequests()) {
                if (pullRequest.getSource().getBranch().getName().equals(((PullRequestSCMHead) head).getBranchName())) {
                    BitbucketPullRequest fullPullRequest = req.getPullRequestById(Integer.parseInt(pullRequest.getId()));
                    boolean isExluded = !isAccepted(fullPullRequest);
                    if (isExluded) {
                        String message = getMessage(fullPullRequest);
                        if (StringUtils.isNotBlank(message)) {
                            req.listener().getLogger().format("  %s%n", message);
                        }
                    }
                    return isExluded;
                }
            }
        }

        return false;
    }

    /**
     * Validates the pull requests is accepted by the filter.
     *
     * @param pullRequest the {@link BitbucketPullRequest}
     * @return {@code true} if and only if the pull requests was verified positive by the filter
     */
    protected boolean isAccepted(BitbucketPullRequest pullRequest) {
        T data = getData(pullRequest);
        TypeFilter<T> filter = getFilter();
        if (filter == null) {
            return true;
        }
        return filter.accepted(data);
    }

    /**
     * Return instance of a filter to validate the data.
     *
     * @return {@link TypeFilter} to validate the data
     */
    protected TypeFilter<T> getFilter() {
        return filter;
    }

    /**
     * Extracts data from pull requests to validate.
     *
     * @param pullRequest the {@link BitbucketPullRequest}
     * @return extracted data to validation
     */
    protected abstract T getData(BitbucketPullRequest pullRequest);

    /**
     * Prepare a message for user to logs.
     *
     * @param pullRequest the {@link BitbucketPullRequest}
     * @return a message for user
     */
    protected abstract String getMessage(BitbucketPullRequest pullRequest);

}
