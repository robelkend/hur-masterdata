package com.rsoft.hurmanagement.hurmasterdata.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormulaExpressionEvaluatorTest {

    private final FormulaExpressionEvaluator evaluator = new FormulaExpressionEvaluator();

    @Test
    void evaluatesArithmeticWithPrecedence() {
        BigDecimal result = evaluator.evaluate("2 + 3 * 4");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("14")));
    }

    @Test
    void evaluatesParentheses() {
        BigDecimal result = evaluator.evaluate("(2 + 3) * 4");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("20")));
    }

    @Test
    void evaluatesTernaryWithStringCondition() {
        BigDecimal result = evaluator.evaluate("'Y' == 'Y' ? 5 : 2");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("5")));
    }

    @Test
    void evaluatesTernaryWithComparisonAndLogicalAnd() {
        BigDecimal result = evaluator.evaluate("10 > 3 && 2 > 1 ? 7 : 9");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("7")));
    }

    @Test
    void evaluatesUnaryMinus() {
        BigDecimal result = evaluator.evaluate("-2 + 3");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("1")));
    }

    @Test
    void handlesDivisionByZero() {
        BigDecimal result = evaluator.evaluate("10 / 0");
        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void evaluatesRelationalOperators() {
        BigDecimal result = evaluator.evaluate("5 >= 5 ? 1 : 0");
        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.ONE));
    }

    @Test
    void evaluatesNotEqualsString() {
        BigDecimal result = evaluator.evaluate("'N' != 'Y' ? 3 : 4");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("3")));
    }

    @Test
    void evaluatesFunctions() {
        BigDecimal result = evaluator.evaluate("max(2, 5, 3) + min(4, 1, 7)");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("6")));
    }

    @Test
    void evaluatesRoundAndPow() {
        BigDecimal result = evaluator.evaluate("round(10 / 3, 2) + pow(2, 3)");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("11.33")));
    }

    @Test
    void resolvesIdentifiersFromVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", new BigDecimal("4"));
        vars.put("y", new BigDecimal("2"));
        BigDecimal result = evaluator.evaluate("x * y + 1", vars, true);
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("9")));
    }

    @Test
    void throwsOnMissingIdentifierWhenStrict() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("missing + 1", Map.of(), true));
    }

    @Test
    void allowsMissingIdentifierWhenPermissive() {
        BigDecimal result = evaluator.evaluate("missing + 2", Map.of(), false);
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("2")));
    }

    @Test
    void evaluatesIfAndCoalesce() {
        BigDecimal result = evaluator.evaluate("if('Y' == 'Y', 5, 1) + coalesce(null, 3)");
        assertNotNull(result);
        assertEquals(0, result.compareTo(new BigDecimal("8")));
    }

    @Test
    void evaluatesDateFunctions() {
        BigDecimal diff = evaluator.evaluate("dateDiffDays('2026-01-01', '2026-01-10')");
        assertNotNull(diff);
        assertEquals(0, diff.compareTo(new BigDecimal("9")));

        BigDecimal day = evaluator.evaluate("dayOfWeek('2026-01-12')");
        assertNotNull(day);
        assertEquals(0, day.compareTo(new BigDecimal("1")));
    }

    @Test
    void evaluatesTimeDiffHours() {
        BigDecimal diff = evaluator.evaluate("timeDiffHours('22:00', '01:00')");
        assertNotNull(diff);
        assertEquals(0, diff.compareTo(new BigDecimal("3.00")));
    }
}
