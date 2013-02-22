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
package com.visural.domo.generator;

import com.visural.domo.Generator;

/**
 *
 * @author Richard Nichols
 */
public class ClientClusterSequenceGenerator implements Generator<Number> {
    
    private final Number startAt;
    private final int nodeBits;
    private final int thisNodeNum;

    /**
     * A sequence generator that generates numbers from a segmented number 
     * space based on a number of potential nodes generating numbers in the same 
     * sequence.
     * 
     * @param startAt number to start counting from - generally the last committed value of this sequence at startup
     * @param nodeBits number of bits to allocate for node numbers, e.g. for 0 for 1 node, 1 for 2 nodes, 2 for 4 nodes, 3 for 8 nodes, etc.
     * @param thisNodeNum the number of the current node if nodeBits = 0, this number is irrelevant
     */
    public ClientClusterSequenceGenerator(Number startAt, int nodeBits, int thisNodeNum) {
        this.startAt = startAt;
        this.nodeBits = nodeBits;
        this.thisNodeNum = thisNodeNum;
    }
    
    @Override
    public synchronized Number get() {
        // TODO: implement
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}
