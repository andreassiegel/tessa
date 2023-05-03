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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * The data model for the document index.
 *
 * @see DocumentIndexItem
 */
public class DocumentIndex {

  /** The document title. */
  @Getter private String title;

  /** The name of the index file and the directory containing the files of the index items. */
  @Getter private String directory;

  /** The index items. */
  @Getter private List<DocumentIndexItem> items;

  /**
   * Instantiates the index model.
   *
   * @param title the index document title.
   * @param directory the index directory.
   */
  public DocumentIndex(String title, String directory) {
    this.title = title;
    this.directory = directory;
    this.items = new ArrayList<>();
  }

  /**
   * Checks whether any of the index items uses the {@code status} property.
   *
   * <p>The result is used to determine whether the table in the generated index page needs to
   * include the "Status" column.
   *
   * @return {@code true} if any of the items has a status set, {@code false} otherwise
   */
  public Boolean getIncludesStatus() {
    return items.stream()
        .map(DocumentIndexItem::getStatus)
        .filter(Objects::nonNull)
        .anyMatch(status -> !status.isEmpty() && !status.isBlank());
  }

  /**
   * Adds an index item to the index.
   *
   * @param item the index item
   */
  public void addToIndex(DocumentIndexItem item) {
    items.add(item);
  }
}
