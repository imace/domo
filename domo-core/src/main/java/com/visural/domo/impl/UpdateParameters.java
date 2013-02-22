/**
 * Copyright (C) 2012 Richard Nichols <rn@visural.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.visural.domo.impl;

/**
 *
 * @author Richard Nichols
 */
public class UpdateParameters {

    /**
     * Number of records after which to flush an insert/update batch.
     *
     * This is done as while batches improve performance initially, for very
     * large batches performance will actually decrease. A batch size as below
     * should get 90% of the performance improvement from batching, while not
     * risking "over-batching".
     */
    public static final int DEFAULT_BATCH_FLUSH_THRESHOLD = 1000;
    public static final UpdateParameters defaults = new UpdateParameters(DEFAULT_BATCH_FLUSH_THRESHOLD, true, true, true, false);
    private final int flushThreshold;
    private final boolean requireSuppliedOrder;
    private final boolean requirePopulateGeneratedIds;
    private final boolean processUnmodifiedRows;
    private final boolean refreshAfterPersist;

    public UpdateParameters(int flushThreshold, boolean requireSuppliedOrder, boolean requirePopulateGeneratedIds, boolean processUnmodifiedRows, boolean refreshAfterPersist) {
        this.flushThreshold = flushThreshold;
        this.requireSuppliedOrder = requireSuppliedOrder;
        this.requirePopulateGeneratedIds = requirePopulateGeneratedIds;
        this.processUnmodifiedRows = processUnmodifiedRows;
        this.refreshAfterPersist = refreshAfterPersist;
    }    

    public int getFlushThreshold() {
        return flushThreshold;
    }

    public boolean isProcessUnmodifiedRows() {
        return processUnmodifiedRows;
    }

    public boolean isRefreshAfterPersist() {
        return refreshAfterPersist;
    }

    public boolean isRequirePopulateGeneratedIds() {
        return requirePopulateGeneratedIds;
    }

    public boolean isRequireSuppliedOrder() {
        return requireSuppliedOrder;
    }
}
