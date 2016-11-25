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
package de.speexx.jira.jan.command.issuequery;

import com.beust.jcommander.ParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.expectThrows;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class TemporalParameterValidatorTest {

    @Test
    public void test_validate_unknown_parameter_value() {
        final Throwable exception = expectThrows(ParameterException.class, () -> {
            new TemporalParameterValidator().validate("-t", "test");
        });
        assertEquals("Illegal temporal parameter value: test", exception.getMessage());
    }

    @Test
    public void test_convert_unknown_parameter_value() {
        assertEquals(TemporalChangeOutput.DURATION, new TemporalParameterValidator().convert("something"));
    }

    @Test
    public void test_validate_null_parameter_value() {
        final Throwable exception = expectThrows(ParameterException.class, () -> {
            new TemporalParameterValidator().validate("--temporal", null);
        });
        assertEquals("Illegal temporal parameter value: null", exception.getMessage());
    }

    @TestFactory
    Stream<DynamicTest> test_validate() {
	List<InputResult> testData = createTestData();
	return testData.stream()
		.map(inOut -> DynamicTest.dynamicTest("Testing " + inOut,
                        () -> new TemporalParameterValidator().validate("-t", inOut.input)));
    }

    @TestFactory
    Stream<DynamicTest> test_convert() {
	List<InputResult> testData = createTestData();
	return testData.stream()
		.map(inOut -> DynamicTest.dynamicTest("Testing " + inOut,
                        () -> assertEquals(inOut.result, new TemporalParameterValidator().convert(inOut.input))));
    }

    final List<InputResult> createTestData() {
        return Arrays.asList(new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_NONE_SHORT, TemporalChangeOutput.NONE),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_NONE_LONG, TemporalChangeOutput.NONE),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_TIME_SHORT, TemporalChangeOutput.TIME),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_TIME_LONG, TemporalChangeOutput.TIME),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_DURATION_SHORT, TemporalChangeOutput.DURATION),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_DURATION_LONG, TemporalChangeOutput.DURATION),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_BOTH_SHORT, TemporalChangeOutput.BOTH),
                             new InputResult(ChangelogFieldFetcher.TEMPORAL_VALUE_BOTH_LONG, TemporalChangeOutput.BOTH));
    }
    
    final static class InputResult {
        public final String input;
        public final TemporalChangeOutput result;
        public InputResult(final String input, final TemporalChangeOutput result) {
            this.input = input;
            this.result = result;
        }

        @Override
        public String toString() {
            return "InputResult{" + "input=" + input + ", result=" + result + '}';
        }
    }
}
