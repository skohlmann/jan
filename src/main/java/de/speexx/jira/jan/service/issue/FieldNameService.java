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

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Simple service to create {@link FieldName} and {@link FieldNamePath} instances.
 */
public final class FieldNameService {

    private static final Map<String, FieldName> FIELDNAME_CACHE = new WeakHashMap<>();
    private static final Map<FieldNamePath, FieldNamePath> FIELDNAMEPATH_CACHE = new WeakHashMap<>();
    
    /**
     * Returns an instance of a {@code FieldNamePath} based on the given parameter.
     * @param pathElements the field name path elements in the correct order
     * @return an instance
     * @throws NullPointerException if and only if <em>pathElements</em> is {@code null}
     * @throws IllegalArgumentException if and only if <em>pathElements</em> contains a
     *                                   {@code null} element or the array is empty.
     */
    public FieldNamePath createFieldNamePath(final FieldName... pathElements) {
        final FieldNamePath path = new FieldNamePath(pathElements);
        final FieldNamePath old = FIELDNAMEPATH_CACHE.get(path);
        if (old != null) {
            return old;
        }
        FIELDNAMEPATH_CACHE.put(path, path);
        return path;
    }

    /**
     * Returns an instance of a {@code FieldName} based on the given parameter.
     * @param name the name of the field. Must not be {@code null}
     * @return an instance
     * @throws NullPointerException if and only if <em>name</em> is {@code null}
     */
    public FieldName createFieldName(final String name) {
        final String adjusted = FieldName.adjustFieldName(Objects.requireNonNull(name, "fieldname is null"));
        final FieldName fieldName = FIELDNAME_CACHE.get(adjusted);
        if (fieldName == null) {
            final FieldName newFieldName = new FieldName(name);
            FIELDNAME_CACHE.putIfAbsent(adjusted, newFieldName);
            return newFieldName;
        }
        return fieldName;
    }
}
