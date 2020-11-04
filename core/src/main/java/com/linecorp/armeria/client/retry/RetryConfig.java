/*
 * Copyright 2020 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.client.retry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.linecorp.armeria.common.Response;

/**
 * Holds retry config used by a {@link RetryingClient}.
 * A {@link RetryConfig} instance encapsulates the used {@link RetryRule}, maxTotalAttempts,
 *  * and responseTimeoutMillisForEachAttempt.
 */
public final class RetryConfig<T extends Response> {
    private final int maxTotalAttempts;
    private final long responseTimeoutMillisForEachAttempt;
    @Nullable private final RetryRule retryRule;
    @Nullable private final RetryRuleWithContent<T> retryRuleWithContent;
    @Nullable private final RetryRule fromRetryRuleWithContent;
    private final int maxContentLength;
    private final boolean needsContentInRule;

    /**
     * Returns a new {@link RetryConfigBuilder} with the default values from Flags.
     * Uses a {@link RetryRule}.
     */
    public static <T extends Response> RetryConfigBuilder<T> builder(RetryRule retryRule) {
        return new RetryConfigBuilder<>(retryRule);
    }

    /**
     * Returns a new {@link RetryConfigBuilder} with the default values from Flags.
     * Uses a {@link RetryRuleWithContent} with unlimited content length.
     */
    public static <T extends Response> RetryConfigBuilder<T> builder(
            RetryRuleWithContent<T> retryRuleWithContent) {
        return new RetryConfigBuilder<>(retryRuleWithContent);
    }

    /**
     * Returns a new {@link RetryConfigBuilder} with the default values from Flags.
     * Uses {@link RetryRuleWithContent} with specified maxContentLength.
     */
    public static <T extends Response> RetryConfigBuilder<T> builder(
            RetryRuleWithContent<T> retryRuleWithContent, int maxContentLength) {
        return new RetryConfigBuilder<>(retryRuleWithContent, maxContentLength);
    }

    /**
     * Returns config's maxTotalAttempt.
     */
    public int maxTotalAttempts() {
        return maxTotalAttempts;
    }

    /**
     * Returns config's responseTimeoutMillisForEachAttempt.
     */
    public long responseTimeoutMillisForEachAttempt() {
        return responseTimeoutMillisForEachAttempt;
    }

    /**
     * Returns config's retryRule, could be null.
     */
    @Nullable public RetryRule retryRule() {
        return retryRule;
    }

    /**
     * Returns config's retryRuleWithContent, could be null.
     */
    @Nullable public RetryRuleWithContent<T> retryRuleWithContent() {
        return retryRuleWithContent;
    }

    /**
     * Returns config's retry rule that is converted from retryRuleWithContent, could be null.
     */
    @Nullable public RetryRule fromRetryRuleWithContent() {
        return fromRetryRuleWithContent;
    }

    /**
     * Returns config's maxContentLength, which is non-zero only if a {@link RetryRuleWithContent} is used.
     */
    public int maxContentLength() {
        return maxContentLength;
    }

    /**
     * Returns whether a {@link RetryRuleWithContent} is being used.
     */
    public boolean needsContentInRule() {
        return needsContentInRule;
    }

    /**
     * Returns whether the associated requires response trailers.
     */
    public boolean requiresResponseTrailers() {
        return needsContentInRule() ?
               retryRuleWithContent().requiresResponseTrailers() : retryRule().requiresResponseTrailers();
    }

    RetryConfig(RetryRule retryRule, int maxTotalAttempts, long responseTimeoutMillisForEachAttempt) {
        checkArguments(maxTotalAttempts, responseTimeoutMillisForEachAttempt);
        this.retryRule = checkNotNull(retryRule);
        this.maxTotalAttempts = maxTotalAttempts;
        this.responseTimeoutMillisForEachAttempt = responseTimeoutMillisForEachAttempt;
        needsContentInRule = false;
        maxContentLength = 0;
        retryRuleWithContent = null;
        fromRetryRuleWithContent = null;
    }

    RetryConfig(
            RetryRuleWithContent<T> retryRuleWithContent,
            int maxContentLength,
            int maxTotalAttempts,
            long responseTimeoutMillisForEachAttempt) {
        checkArguments(maxTotalAttempts, responseTimeoutMillisForEachAttempt);
        this.maxContentLength = maxContentLength;
        this.retryRuleWithContent = checkNotNull(retryRuleWithContent);
        fromRetryRuleWithContent = RetryRuleUtil.fromRetryRuleWithContent(retryRuleWithContent);
        this.maxTotalAttempts = maxTotalAttempts;
        this.responseTimeoutMillisForEachAttempt = responseTimeoutMillisForEachAttempt;
        needsContentInRule = true;
        retryRule = null;
    }

    private RetryConfig() {
        throw new IllegalStateException("RetryConfig must have a rule.");
    }

    private static void checkArguments(int maxTotalAttempts, long responseTimeoutMillisForEachAttempt) {
        checkArgument(
                maxTotalAttempts > 0,
                "maxTotalAttempts: %s (expected: > 0)",
                maxTotalAttempts);
        checkArgument(
                responseTimeoutMillisForEachAttempt >= 0,
                "responseTimeoutMillisForEachAttempt: %s (expected: >= 0)",
                responseTimeoutMillisForEachAttempt);
    }
}
