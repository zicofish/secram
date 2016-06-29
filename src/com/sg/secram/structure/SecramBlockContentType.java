/**
 * ****************************************************************************
 * Copyright 2013 EMBL-EBI
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.sg.secram.structure;

/**
 * The supported block content types are:
 * <ul>
 * <li>FILE_HEADER, for header of a SECRAM file</li>
 * <li>CONTAINER_HEADER, for header of a container</li>
 * <li>COMPRESSION_HEADER, for compression information of a container</li>
 * <li>EXTERNAL, for an external block that stores the same type of information in SECRAM records</li>
 * <li>CORE, for a block that stores any non-external information of SECRAM records. Each container has only one CORE block.</li>
 * </ul>
 * @author zhihuang
 *
 */
public enum SecramBlockContentType {
	FILE_HEADER, CONTAINER_HEADER, COMPRESSION_HEADER, EXTERNAL, CORE
}
