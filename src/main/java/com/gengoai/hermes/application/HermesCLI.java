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

import com.gengoai.application.CommandLineApplication;
import com.gengoai.hermes.Hermes;

import java.util.Collections;
import java.util.Set;

/**
 * The type Hermes cli.
 *
 * @author David B. Bracewell
 */
public abstract class HermesCLI extends CommandLineApplication {

   /**
    * Instantiates a new Hermes cli.
    */
   public HermesCLI() {
      super();
   }

   /**
    * Instantiates a new Hermes cli.
    *
    * @param applicationName the application name
    */
   public HermesCLI(String applicationName) {
      super(applicationName);
   }

   @Override
   public Set<String> getDependentPackages() {
      return Collections.singleton(Hermes.HERMES_PACKAGE);
   }


}//END OF HermesApplication
