/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.gengoai.hermes.application;

import com.gengoai.application.SwingApplication;
import lombok.NonNull;

import static com.gengoai.hermes.Hermes.HERMES_PACKAGE;

/**
 * The type Hermes cli.
 *
 * @author David B. Bracewell
 */
public abstract class HermesGUI extends SwingApplication {

   /**
    * Instantiates a new Hermes cli.
    */
   public HermesGUI() {
      addDependency(HERMES_PACKAGE);
   }

   /**
    * Instantiates a new Hermes cli.
    *
    * @param applicationName the application name
    */
   public HermesGUI(String applicationName) {
      super(applicationName);
      addDependency(HERMES_PACKAGE);
   }

   /**
    * Instantiates a new Hermes cli.
    *
    * @param applicationName  the application name
    * @param requiredPackages the required packages
    */
   public HermesGUI(String applicationName, @NonNull String[] requiredPackages) {
      super(applicationName, requiredPackages);
      addDependency(HERMES_PACKAGE);
   }


}//END OF HermesApplication
