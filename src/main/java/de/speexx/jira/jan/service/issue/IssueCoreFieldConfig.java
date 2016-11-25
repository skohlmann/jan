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

import de.speexx.jira.jan.JiraAnalyzeException;
import java.lang.reflect.Method;
import java.util.Collections;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class IssueCoreFieldConfig {
    
    private final Map<FieldName, IssueCoreFieldDescriptionEntry> config = new HashMap<>();
    
    void addIssueFieldDescriptionEntry(final IssueCoreFieldDescriptionEntry entry) {
        if (entry == null) {
            return;
        }
        
        final Set<FieldName> names = entry.getAllFieldNames();
        assert names != null;
        names.forEach(name -> {
            assert name != null;
            this.config.put(name, entry);
        });
    }

    public Iterator<FieldName> fieldNames() {
        return Collections.unmodifiableSet(this.config.keySet()).iterator();
    }
    
    public boolean contains(final FieldName fieldName) {
        assert this.config != null;
        return this.config.containsKey(fieldName);
    }

    public boolean contains(final String fieldName) {
        if (fieldName == null) {
            return false;
        }
        assert this.config != null;
        return this.config.containsKey(new FieldName(fieldName));
    }

    
    public Optional<Boolean> isIgnorable(final FieldName fieldName) {
        final IssueCoreFieldDescriptionEntry entry = fetchIssueFieldDescriptionEntry(fieldName);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.isIgnore());
    }
    
    public Optional<Method> getIssueGetter(final FieldName fieldName) {
        final IssueCoreFieldDescriptionEntry entry = fetchIssueFieldDescriptionEntry(fieldName);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getIssueFieldGetterMethod());
    }
    
    Optional<ValueFetcher> getValueFetcher(final FieldName fieldName) {
        final IssueCoreFieldDescriptionEntry entry = fetchIssueFieldDescriptionEntry(fieldName);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getValueFetcher());
    }
    
    IssueCoreFieldDescriptionEntry fetchIssueFieldDescriptionEntry(final FieldName fieldName) {
        return this.config.get(fieldName);
    }
    
    @Override
    public String toString() {
        return "IssueFieldConfig{" + "config=" + config + '}';
    }
    
    static final class IssueCoreFieldDescriptionEntry {
        
        final FieldName fieldName;
        final Set<FieldName> fieldNameAliases;
        final Method fieldNameMethod;
        final boolean ignore;
        final ValueFetcher valueFetcher;

        public IssueCoreFieldDescriptionEntry(final FieldName fieldName,
                                              final Set<FieldName> fieldNameAliases,
                                              final Method fieldNameMethod,
                                              final boolean ignore,
                                              final Class<?> cast) {
            if (fieldName == null) {throw new IllegalArgumentException("Fieldname is null");}
            this.fieldName = fieldName;
            if (fieldNameAliases == null) {
                this.fieldNameAliases = emptySet();
            } else {
                this.fieldNameAliases = unmodifiableSet(new HashSet<FieldName>(fieldNameAliases));
            }
            if (fieldNameMethod == null) {throw new IllegalArgumentException("FieldNameMethod is null");}
            this.fieldNameMethod = fieldNameMethod;
            this.ignore = ignore;
            if (cast == null) {throw new IllegalArgumentException("Cast is null");}
            try {
                this.valueFetcher = (ValueFetcher) cast.newInstance();
            } catch (final InstantiationException | IllegalAccessException ex) {
                throw new JiraAnalyzeException(ex);
            }
        }

        public FieldName getFieldName() {
            return this.fieldName;
        }

        public Set<FieldName> getFieldNameAliases() {
            return this.fieldNameAliases;
        }

        public Method getIssueFieldGetterMethod() {
            return this.fieldNameMethod;
        }

        public boolean isIgnore() {
            return this.ignore;
        }
        
        ValueFetcher getValueFetcher() {
            return this.valueFetcher;
        }
        
        public Set<FieldName> getAllFieldNames() {
            final Set<FieldName> names = new HashSet<>(getFieldNameAliases());
            names.add(getFieldName());
            return unmodifiableSet(names);
        }

        @Override
        public String toString() {
            return "IssueFieldDescriptionEntry{" + "fieldName=" + fieldName + ", fieldNameAliases=" + fieldNameAliases + ", fieldNameMethod=" + fieldNameMethod + ", ignore=" + ignore + ", valueFetcher=" + valueFetcher + '}';
        }
    }
}
