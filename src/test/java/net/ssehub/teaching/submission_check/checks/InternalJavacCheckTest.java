/*
 * Copyright 2020 Software Systems Engineering, University of Hildesheim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.teaching.submission_check.checks;

import static org.junit.Assume.assumeTrue;

import org.junit.BeforeClass;

import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class InternalJavacCheckTest extends JavacCheckTest {

    @BeforeClass
    public static void checkIfSupported() {
        assumeTrue(InternalJavacCheck.isSupported());
    }
    
    @Override
    protected JavacCheck creatInstance() {
        return new InternalJavacCheck();
    }
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }

}
