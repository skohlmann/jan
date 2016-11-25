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

import java.util.Locale;
import java.util.Objects;

/**
 * Encapsultes a field name of a JIRA issue. A field name can be a name of
 * the core fields like <tt>createdDate</tt>, <tt>reporter</tt> or the issue
 * <tt>key</tt>. A field name can also be a name of a custom field created
 * for a dedicated project.
 * <p>The implementation is immutable.</p>
 * @see FieldNamePath FieldNamePath for complex field names
 * @see FieldNameService#createFieldName(java.lang.String) FieldNameService.createFieldName(String) to create FieldName instances
 * @see IssueCoreFieldConfig IssueCoreFieldConfig for an API to handle the core fields of an issue
 */
public final class FieldName {

    final String nameElement;

    FieldName(final String name) {
        this.nameElement = adjustFieldName(Objects.requireNonNull(name, "Name element is null"));
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name contains no visibile character");
        }
    }
    
    /**
     * Returns the given field name as {@code String}.
     * @return the field name element
     */
    public String asString() {
        return this.nameElement;
    }
    
    @Override
    public String toString() {
        return "FieldName{" + "nameElement=" + nameElement + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.nameElement);
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
        final FieldName other = (FieldName) obj;
        if (!Objects.equals(this.nameElement, other.nameElement)) {
            return false;
        }
        return true;
    }
    
    static String adjustFieldName(final String name) {
        assert !Objects.isNull(name);
        return name.toLowerCase(Locale.ENGLISH).intern();
    }
}
