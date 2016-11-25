/* A tool to extract transition information from JIRA for analyzis (jan).
 *
 * Copyright (C) 2016 Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.jira.jan.service.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import static java.util.stream.Collectors.joining;

/**
 * Defines the field name path for complex fields. Complex types are not core field
 * like <tt>key</tt> or <tt>createdDate</tt> or <tt>reporter</tt>.
 * Such complex fields can have a deeper hierachies of sub fields. E.g. the 
 * <tt>User</tt> field (e.g for <tt>creator</tt> contains avatar URLs which path
 * look like <tt>creator::avatarUrls::48x48</tt>).
 * <p>A {@code FieldNamePath} is build by {@code FieldName}s.</p>
 * <p>The implementation is immutable.</p>
 * @see FieldName
 * @see FieldNameService#createFieldNamePath(de.speexx.jira.jan.service.issue.FieldName...) FieldNameService.createFieldNamePath(FieldName...) to create FieldNamePath instances
 */
public final class FieldNamePath implements Iterable<FieldName> {
    
    /**
     * Delimiter between two path elements: <tt>{@value}</tt>.
     * @see #asString() for a string representation of the path
     */
    public final static String DELIMITER = "::";

    private static final FieldNamePath EMPTY = new FieldNamePath();
    
    private final List<FieldName> pathElements;
    
    private FieldNamePath() {
        this.pathElements = Collections.emptyList();
    }
    
    FieldNamePath(final List<FieldName> pathElements) {
        this.pathElements = checkNullValue(Objects.requireNonNull(pathElements, "pathElements is null"));
        if (this.pathElements.isEmpty()) {
            throw new IllegalArgumentException("No root element given");
        }
    }
    
    FieldNamePath(final FieldName... pathElements) {
        Objects.requireNonNull(pathElements, "pathElements is null");
        if (pathElements.length == 0) {
            throw new IllegalArgumentException("No root element given");
        }
        this.pathElements = checkNullValue(Arrays.asList(pathElements));
    }

    List<FieldName> checkNullValue(final List<FieldName> fieldNames) {
        fieldNames.forEach(name -> {
            if (name == null) {
                throw new IllegalArgumentException("FieldName list contains null value: " + fieldNames);
            }
        });
        return fieldNames;
    }

    /**
     * Iterator over the path elements.
     * @return never {@code null}
     */
    @Override
    public Iterator<FieldName> iterator() {
        return Collections.unmodifiableList(this.pathElements).iterator();
    }

    @Override
    public String toString() {
        return "FieldNamePath{" + "pathElements=" + Arrays.deepToString(this.pathElements.toArray()) + '}';
    }

    /**
     * Returns the root element of the path. The root element is the first path element.
     */
    public FieldName getRootElement() {
        return this.pathElements.get(0);
    }

    /**
     * Returns the count of path elements.
     * @return the count of path elements.
     */
    public int length() {
        return this.pathElements.size();
    }
    
    /**
     * Returns a {@link String} representation of the path. The implementation
     * use {@link #DELIMITER} to distiguish between two path elements.
     * @return never {@code null}
     */
    public String asString() {
        return this.pathElements.stream().map(FieldName::asString).collect(joining("::"));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.pathElements);
        return hash;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldNamePath other = (FieldNamePath) obj;
        if (!Objects.equals(this.pathElements, other.pathElements)) {
            return false;
        }
        return true;
    }

    /**
     * Creats a new path without the root element (first element).
     */
    FieldNamePath getPathWithoutRoot() {
        assert this.pathElements != null;
        final int size = this.pathElements.size();
        if (size == 1) {
            return EMPTY;
        }
        final List<FieldName> elements = new ArrayList<>(size - 1);
        for (int idx = 1; idx < size; idx++) {
            final FieldName element = this.pathElements.get(idx);
            elements.add(element);
        }
        return new FieldNamePath(elements);
    }
}
