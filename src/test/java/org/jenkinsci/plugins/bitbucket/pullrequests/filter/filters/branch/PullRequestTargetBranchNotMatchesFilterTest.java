/*
 * The MIT License
 *
 * Copyright (c) 2023, Gabriel Einsdorf.
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
package org.jenkinsci.plugins.bitbucket.pullrequests.filter.filters.branch;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.StringFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequestDestination;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequestSource;

import hudson.model.TaskListener;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestTargetBranchNotMatchesFilterTest {


    @Mock
    BitbucketSCMSourceRequest scmSourceRequest;

    @Mock
    BitbucketPullRequest pullRequest;

    @Mock
    BitbucketPullRequestDestination pullRequestDestination;

    @Mock
    BitbucketPullRequestSource pullRequestSource;

    @Mock
    BitbucketBranch sourceBranch;

    @Mock
    BitbucketBranch targetBranch;

    @Mock
    PullRequestSCMHead pullRequestSCMHead;

    @Mock
    TaskListener taskListener;

    @Mock
    PrintStream logger;

    @Before
    public void setUp() throws Throwable {
        when(taskListener.getLogger()).thenReturn(logger);
        when(pullRequest.getId()).thenReturn("1");
        when(pullRequest.getDestination()).thenReturn(pullRequestDestination);
        when(pullRequest.getSource()).thenReturn(pullRequestSource);
        when(pullRequestDestination.getBranch()).thenReturn(targetBranch);
        when(pullRequestSource.getBranch()).thenReturn(sourceBranch);
        when(sourceBranch.getName()).thenReturn("dummy");
        when(pullRequestSCMHead.getBranchName()).thenReturn("dummy");
        when(scmSourceRequest.listener()).thenReturn(taskListener);
        when(scmSourceRequest.getPullRequests()).thenReturn(Arrays.asList(pullRequest));
        when(scmSourceRequest.getPullRequestById(1)).thenReturn(pullRequest);
    }
    
    private void setupTargetBranchNameMock(String branchName) {
        when(targetBranch.getName()).thenReturn(branchName);
    }

    @Test
    public void testCaseSensitivePhrase() throws IOException, InterruptedException {
        // given
    	setupTargetBranchNameMock("Test-branch-name");
        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Test", false)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testNegativeCaseSensitivePhrase() throws IOException, InterruptedException {
        // given
    	setupTargetBranchNameMock("Test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("test", false)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testCaseInsensitivePhrase() throws IOException, InterruptedException {
        // given
    	setupTargetBranchNameMock("Test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("test", true)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testPhraseAsPartOfWord() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("Test-title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("t")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNotExistsPhrase() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("not-the-same")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testEmptyPhrase() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNullPhrase() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter((String) null)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testExistsPattern() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("Test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile("^Te??.*"))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testNotExistsPattern() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("Test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile("^te??$"))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testEmptyPattern() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile(""))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNullPattern() throws IOException, InterruptedException {
        // given
        setupTargetBranchNameMock("test-branch-name");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter((Pattern) null)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    private SCMHeadFilter givenSCMHeadFilter(StringFilter filter) {
        return new PullRequestTargetBranchNotMatchesFilter(filter);
    }

    private StringFilter givenFilter(Pattern pattern) {
        return new StringFilter(pattern);
    }

    private StringFilter givenFilter(String phrase, boolean ignoreCase) {
        return new StringFilter(phrase, ignoreCase);
    }

    private StringFilter givenFilter(String phrase) {
        return givenFilter(phrase, true);
    }
}