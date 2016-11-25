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

import com.atlassian.jira.rest.client.api.domain.Issue;
import de.speexx.jira.jan.JiraAnalyzeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Produces;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import de.speexx.jira.jan.service.issue.IssueCoreFieldConfig.IssueCoreFieldDescriptionEntry;
import java.util.Collections;

class IssueCoreFieldConfigLoader {
    
    private static final String CONFIGURATION_RESOURCE_PATH = "META-INF/jan.issue.config.csv";

    private final static String FIELDNAME_HEADER = "fieldname";
    private final static String FIELDNAME_ALIAS_HEADER = "fieldname-alias";
    private final static String ISSUE_METHOD_HEADER = "issue-method";
    private final static String IGNORE_HEADER = "ignore";
    private final static String VALUE_FETCHER_TYPE_HEADER = "value-fetcher-type";
    
    private final static String KEY_ALIAS_DELIMITER = ",";

    private final FieldNameService fieldNameService = new FieldNameService();
    
    @Produces @FieldConfig
    IssueCoreFieldConfig loadConfig() {
        
        final IssueCoreFieldConfig issueConfig = new IssueCoreFieldConfig();
        final ClassLoader cl = getClass().getClassLoader();

        try (final InputStream configStream = cl.getResourceAsStream(getConfigurationResourcePath());
             final InputStreamReader configReader = new InputStreamReader(configStream);
             final CSVParser configParser = RFC4180.withHeader().parse(configReader)) {

            configParser.forEach(record -> {
                final IssueCoreFieldConfig.IssueCoreFieldDescriptionEntry entry = createIssueFieldConfigEntry(record, cl);
                issueConfig.addIssueFieldDescriptionEntry(entry);
            });
            
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
        
        return issueConfig;
    }

    IssueCoreFieldDescriptionEntry createIssueFieldConfigEntry(final CSVRecord record, 
                                                               final ClassLoader cl) {
        assert record != null;
        assert cl != null;

        try {
            final String fieldName = record.get(FIELDNAME_HEADER).trim();
            final Class<?> valueFetcherType = fetchValueFetcherType(record, cl);
            final Method fieldGetter = createFieldGetterMethod(record);
            final boolean ignore = fetchIgnore(record);
            final Set<FieldName> aliases = fetchAliasses(record);

            return new IssueCoreFieldDescriptionEntry(this.fieldNameService.createFieldName(fieldName), 
                                                      aliases,
                                                      fieldGetter,
                                                      ignore,
                                                      valueFetcherType);

        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            throw new JiraAnalyzeException(e);
        }
    }
    
    Set<FieldName> fetchAliasses(final CSVRecord record) {
        final String alias = record.get(FIELDNAME_ALIAS_HEADER);
        if (alias == null || alias.length() == 0) {
            return Collections.emptySet();
        }
        final String[] aliases = alias.split(KEY_ALIAS_DELIMITER);
        if (aliases.length == 0) {
            return Collections.emptySet();
        }
        final FieldName[] fieldNameAliases = new FieldName[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            fieldNameAliases[i] = this.fieldNameService.createFieldName(aliases[i].trim());
        }
        return new HashSet<>(Arrays.asList(fieldNameAliases));
    }
    
    boolean fetchIgnore(final CSVRecord record) {
        assert record != null;

        final String ignore = record.get(IGNORE_HEADER);
        return Boolean.parseBoolean(ignore);
    }

    Method createFieldGetterMethod(final CSVRecord record) throws NoSuchMethodException {
        assert record != null;

        final String getterName = record.get(ISSUE_METHOD_HEADER);
        final Method getterMethod = Issue.class.getMethod(getterName);
        return getterMethod;
    }
    
    Class<?> fetchValueFetcherType(final CSVRecord record, final ClassLoader cl) throws ClassNotFoundException {
        assert record != null;
        assert cl != null;

        final String valueFetcherTypeName = record.get(VALUE_FETCHER_TYPE_HEADER);
        return Class.forName(valueFetcherTypeName, false, cl);
    }
    
    String getConfigurationResourcePath() {
        return CONFIGURATION_RESOURCE_PATH;
    }
}
