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
package com.visural.domo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a method over which a transaction should run.
 * 
 * Note that this annotation only operates when used in the Spring or Guice
 * projects downstream. However this annotation is shared by these projects to
 * allow switching between providers more easily.
 * 
 * @author Richard Nichols
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface Transactional {
    // TODO: add support for:
    
    /*
     * 
REQUIRED	 Runs in a Transaction. Will use an existing transaction if it exists, otherwise will create a new Transaction.
REQUIRES_NEW	 Runs in a new Transaction. If a current transaction exists it will be suspended.
MANDATORY	 Runs in the existing Transaction. If there is no current existing transaction an exception is thrown.
SUPPORTS	 Use a transaction if it already exists. If it does not then the method runs without a transaction.
NOT_SUPPORTS	 Always runs without a transaction. If one already exists then it is suspended.
NEVER	 Always runs without a transaction. If one already exists then it throws an exception.

     */
    String connectionSource() default ConnectionProvider.Default; 
}
