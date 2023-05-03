package de.andreassiegel.tessa.plugin.model.index;

/*-
 * Copyright © 2023 Andreas Siegel (mail@andreassiegel.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Builder;
import lombok.Getter;

/** The data model for document index items. */
@Getter
@Builder
public class DocumentIndexItem {

  /** The item title. */
  private String title;

  /** The name of the item file. */
  private String filename;

  /** The description from the index item. */
  private String description;

  /** The status from the index item. */
  private String status;
}
