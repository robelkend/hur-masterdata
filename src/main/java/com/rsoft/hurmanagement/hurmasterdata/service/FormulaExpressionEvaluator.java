package com.rsoft.hurmanagement.hurmasterdata.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class FormulaExpressionEvaluator {

    public BigDecimal evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        return evaluate(expression, null, false);
    }

    public BigDecimal evaluate(String expression, java.util.Map<String, Object> variables, boolean strictMissingVariables) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();
        if (tokens.isEmpty()) {
            return null;
        }
        Parser parser = new Parser(tokens, variables, strictMissingVariables);
        Object result = parser.parseExpression();
        return toBigDecimal(result);
    }

    public List<String> extractIdentifiers(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();
        if (tokens.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<String> identifiers = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type != TokenType.IDENTIFIER) {
                continue;
            }
            if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.LPAREN) {
                continue;
            }
            identifiers.add(token.value);
        }
        return identifiers;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static class Parser {
        private final List<Token> tokens;
        private int index = 0;
        private final java.util.Map<String, Object> variables;
        private final boolean strictMissingVariables;

        private Parser(List<Token> tokens, java.util.Map<String, Object> variables, boolean strictMissingVariables) {
            this.tokens = tokens;
            this.variables = variables;
            this.strictMissingVariables = strictMissingVariables;
        }

        private Object parseExpression() {
            return parseTernary();
        }

        private Object parseTernary() {
            Object condition = parseOr();
            if (match(TokenType.QUESTION)) {
                Object trueExpr = parseExpression();
                expect(TokenType.COLON);
                Object falseExpr = parseExpression();
                return toBoolean(condition) ? trueExpr : falseExpr;
            }
            return condition;
        }

        private Object parseOr() {
            Object left = parseAnd();
            while (match(TokenType.OR)) {
                Object right = parseAnd();
                left = toBoolean(left) || toBoolean(right);
            }
            return left;
        }

        private Object parseAnd() {
            Object left = parseEquality();
            while (match(TokenType.AND)) {
                Object right = parseEquality();
                left = toBoolean(left) && toBoolean(right);
            }
            return left;
        }

        private Object parseEquality() {
            Object left = parseRelational();
            while (true) {
                if (match(TokenType.EQ)) {
                    Object right = parseRelational();
                    left = compare(left, right) == 0;
                } else if (match(TokenType.NE)) {
                    Object right = parseRelational();
                    left = compare(left, right) != 0;
                } else {
                    break;
                }
            }
            return left;
        }

        private Object parseRelational() {
            Object left = parseAdditive();
            while (true) {
                if (match(TokenType.GT)) {
                    Object right = parseAdditive();
                    left = compare(left, right) > 0;
                } else if (match(TokenType.GE)) {
                    Object right = parseAdditive();
                    left = compare(left, right) >= 0;
                } else if (match(TokenType.LT)) {
                    Object right = parseAdditive();
                    left = compare(left, right) < 0;
                } else if (match(TokenType.LE)) {
                    Object right = parseAdditive();
                    left = compare(left, right) <= 0;
                } else {
                    break;
                }
            }
            return left;
        }

        private Object parseAdditive() {
            Object left = parseMultiplicative();
            while (true) {
                if (match(TokenType.PLUS)) {
                    Object right = parseMultiplicative();
                    left = toNumber(left).add(toNumber(right));
                } else if (match(TokenType.MINUS)) {
                    Object right = parseMultiplicative();
                    left = toNumber(left).subtract(toNumber(right));
                } else {
                    break;
                }
            }
            return left;
        }

        private Object parseMultiplicative() {
            Object left = parseUnary();
            while (true) {
                if (match(TokenType.MUL)) {
                    Object right = parseUnary();
                    left = toNumber(left).multiply(toNumber(right));
                } else if (match(TokenType.DIV)) {
                    Object right = parseUnary();
                    BigDecimal divisor = toNumber(right);
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        left = BigDecimal.ZERO;
                    } else {
                        left = toNumber(left).divide(divisor, 6, RoundingMode.HALF_UP);
                    }
                } else {
                    break;
                }
            }
            return left;
        }

        private Object parseUnary() {
            if (match(TokenType.NOT)) {
                return !toBoolean(parseUnary());
            }
            if (match(TokenType.MINUS)) {
                return toNumber(parseUnary()).negate();
            }
            if (match(TokenType.PLUS)) {
                return toNumber(parseUnary());
            }
            return parsePrimary();
        }

        private Object parsePrimary() {
            if (match(TokenType.LPAREN)) {
                Object value = parseExpression();
                expect(TokenType.RPAREN);
                return value;
            }
            if (match(TokenType.NUMBER)) {
                return new BigDecimal(previous().value);
            }
            if (match(TokenType.STRING)) {
                return previous().value;
            }
            if (match(TokenType.IDENTIFIER)) {
                String name = previous().value;
                if (match(TokenType.LPAREN)) {
                    List<Object> args = new ArrayList<>();
                    if (!check(TokenType.RPAREN)) {
                        do {
                            args.add(parseExpression());
                        } while (match(TokenType.COMMA));
                    }
                    expect(TokenType.RPAREN);
                    return evaluateFunction(name, args);
                }
                return resolveIdentifier(name);
            }
            return BigDecimal.ZERO;
        }

        private boolean match(TokenType type) {
            if (check(type)) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(TokenType type) {
            if (!match(type)) {
                throw new IllegalArgumentException("Expected token: " + type);
            }
        }

        private boolean check(TokenType type) {
            if (isAtEnd()) {
                return false;
            }
            return peek().type == type;
        }

        private boolean isAtEnd() {
            return index >= tokens.size();
        }

        private Token peek() {
            return tokens.get(index);
        }

        private Token previous() {
            return tokens.get(index - 1);
        }

        private BigDecimal toNumber(Object value) {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
            if (value instanceof Boolean) {
                return (Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
            }
            if (value instanceof String) {
                try {
                    return new BigDecimal((String) value);
                } catch (NumberFormatException ex) {
                    return BigDecimal.ZERO;
                }
            }
            return BigDecimal.ZERO;
        }

        private boolean toBoolean(Object value) {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).compareTo(BigDecimal.ZERO) != 0;
            }
            if (value instanceof String) {
                String text = ((String) value).trim();
                return "Y".equalsIgnoreCase(text) || "TRUE".equalsIgnoreCase(text) || "1".equals(text);
            }
            return false;
        }

        private int compare(Object left, Object right) {
            BigDecimal leftNumber = toNumber(left);
            BigDecimal rightNumber = toNumber(right);
            boolean leftIsNumber = isNumeric(left);
            boolean rightIsNumber = isNumeric(right);
            if (leftIsNumber && rightIsNumber) {
                return leftNumber.compareTo(rightNumber);
            }
            String leftText = String.valueOf(left).replace("'", "");
            String rightText = String.valueOf(right).replace("'", "");
            return leftText.compareToIgnoreCase(rightText);
        }

        private boolean isNumeric(Object value) {
            if (value instanceof BigDecimal) {
                return true;
            }
            if (value instanceof String) {
                try {
                    new BigDecimal((String) value);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return false;
        }

        private Object resolveIdentifier(String name) {
            if (variables != null && variables.containsKey(name)) {
                return variables.get(name);
            }
            if ("true".equalsIgnoreCase(name)) {
                return true;
            }
            if ("false".equalsIgnoreCase(name)) {
                return false;
            }
            if ("Y".equalsIgnoreCase(name)) {
                return "Y";
            }
            if ("N".equalsIgnoreCase(name)) {
                return "N";
            }
            if (strictMissingVariables) {
                throw new IllegalArgumentException("Unknown identifier: " + name);
            }
            return null;
        }

        private Object evaluateFunction(String name, List<Object> args) {
            String fn = name.toLowerCase();
            switch (fn) {
                case "if":
                    if (args.size() >= 3) {
                        return toBoolean(args.get(0)) ? args.get(1) : args.get(2);
                    }
                    return BigDecimal.ZERO;
                case "coalesce":
                    for (Object arg : args) {
                        if (!isNullLike(arg)) {
                            return arg;
                        }
                    }
                    return null;
                case "min":
                    return args.stream().map(this::toNumber).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                case "max":
                    return args.stream().map(this::toNumber).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                case "abs":
                    return toNumber(firstArg(args)).abs();
                case "round":
                    if (args.size() >= 2) {
                        int scale = toNumber(args.get(1)).intValue();
                        return toNumber(firstArg(args)).setScale(scale, RoundingMode.HALF_UP);
                    }
                    return toNumber(firstArg(args)).setScale(0, RoundingMode.HALF_UP);
                case "ceil":
                    return toNumber(firstArg(args)).setScale(0, RoundingMode.CEILING);
                case "floor":
                    return toNumber(firstArg(args)).setScale(0, RoundingMode.FLOOR);
                case "pow":
                    if (args.size() >= 2) {
                        BigDecimal base = toNumber(args.get(0));
                        BigDecimal exp = toNumber(args.get(1));
                        return BigDecimal.valueOf(Math.pow(base.doubleValue(), exp.doubleValue()));
                    }
                    return BigDecimal.ZERO;
                case "today":
                    return java.time.LocalDate.now().toString();
                case "now":
                    return java.time.OffsetDateTime.now().toString();
                case "datediffdays":
                    if (args.size() >= 2) {
                        java.time.LocalDate start = parseDate(args.get(0));
                        java.time.LocalDate end = parseDate(args.get(1));
                        if (start != null && end != null) {
                            return java.math.BigDecimal.valueOf(java.time.Duration.between(
                                    start.atStartOfDay(), end.atStartOfDay()).toDays());
                        }
                    }
                    return BigDecimal.ZERO;
                case "dateadddays":
                    if (args.size() >= 2) {
                        java.time.LocalDate base = parseDate(args.get(0));
                        int days = toNumber(args.get(1)).intValue();
                        if (base != null) {
                            return base.plusDays(days).toString();
                        }
                    }
                    return null;
                case "dateaddmonths":
                    if (args.size() >= 2) {
                        java.time.LocalDate base = parseDate(args.get(0));
                        int months = toNumber(args.get(1)).intValue();
                        if (base != null) {
                            return base.plusMonths(months).toString();
                        }
                    }
                    return null;
                case "dateaddyears":
                    if (args.size() >= 2) {
                        java.time.LocalDate base = parseDate(args.get(0));
                        int years = toNumber(args.get(1)).intValue();
                        if (base != null) {
                            return base.plusYears(years).toString();
                        }
                    }
                    return null;
                case "dayofweek":
                    if (!args.isEmpty()) {
                        java.time.LocalDate date = parseDate(args.get(0));
                        if (date != null) {
                            return java.math.BigDecimal.valueOf(date.getDayOfWeek().getValue());
                        }
                    }
                    return BigDecimal.ZERO;
                case "timediffhours":
                    if (args.size() >= 2) {
                        java.time.LocalTime start = parseTime(args.get(0));
                        java.time.LocalTime end = parseTime(args.get(1));
                        if (start != null && end != null) {
                            long minutes = java.time.Duration.between(start, end).toMinutes();
                            if (minutes < 0) {
                                minutes += 24 * 60;
                            }
                            return java.math.BigDecimal.valueOf(minutes)
                                    .divide(java.math.BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                        }
                    }
                    return BigDecimal.ZERO;
                default:
                    if (strictMissingVariables) {
                        throw new IllegalArgumentException("Unknown function: " + name);
                    }
                    return BigDecimal.ZERO;
            }
        }

        private Object firstArg(List<Object> args) {
            if (args.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return args.get(0);
        }

        private boolean isNullLike(Object value) {
            if (value == null) {
                return true;
            }
            if (value instanceof String) {
                return ((String) value).trim().isEmpty();
            }
            return false;
        }

        private java.time.LocalDate parseDate(Object value) {
            if (value instanceof java.time.LocalDate) {
                return (java.time.LocalDate) value;
            }
            if (value instanceof java.time.OffsetDateTime) {
                return ((java.time.OffsetDateTime) value).toLocalDate();
            }
            if (value instanceof String) {
                try {
                    return java.time.LocalDate.parse((String) value);
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }

        private java.time.LocalTime parseTime(Object value) {
            if (value instanceof java.time.LocalTime) {
                return (java.time.LocalTime) value;
            }
            if (value instanceof String) {
                try {
                    return java.time.LocalTime.parse((String) value);
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
    }

    private enum TokenType {
        NUMBER,
        STRING,
        IDENTIFIER,
        PLUS,
        MINUS,
        MUL,
        DIV,
        LPAREN,
        RPAREN,
        COMMA,
        QUESTION,
        COLON,
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        AND,
        OR,
        NOT
    }

    private static class Token {
        private final TokenType type;
        private final String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class Tokenizer {
        private final String expression;
        private final List<Token> tokens = new ArrayList<>();
        private int index = 0;

        private Tokenizer(String expression) {
            this.expression = expression;
        }

        private List<Token> tokenize() {
            while (!isAtEnd()) {
                char c = peek();
                if (Character.isWhitespace(c)) {
                    index++;
                    continue;
                }
                if (c == '\'' || c == '"') {
                    tokenizeString(c);
                    continue;
                }
                if (Character.isDigit(c) || c == '.') {
                    tokenizeNumber();
                    continue;
                }
                if (isIdentifierStart(c)) {
                    tokenizeIdentifier();
                    continue;
                }
                switch (c) {
                    case '+':
                        add(TokenType.PLUS);
                        break;
                    case '-':
                        add(TokenType.MINUS);
                        break;
                    case '*':
                        add(TokenType.MUL);
                        break;
                    case '/':
                        add(TokenType.DIV);
                        break;
                    case '(':
                        add(TokenType.LPAREN);
                        break;
                    case ')':
                        add(TokenType.RPAREN);
                        break;
                    case ',':
                        add(TokenType.COMMA);
                        break;
                    case '?':
                        add(TokenType.QUESTION);
                        break;
                    case ':':
                        add(TokenType.COLON);
                        break;
                    case '!':
                        if (match('=')) {
                            tokens.add(new Token(TokenType.NE, "!="));
                        } else {
                            add(TokenType.NOT);
                        }
                        break;
                    case '=':
                        if (match('=')) {
                            tokens.add(new Token(TokenType.EQ, "=="));
                        } else {
                            index++;
                        }
                        break;
                    case '>':
                        if (match('=')) {
                            tokens.add(new Token(TokenType.GE, ">="));
                        } else {
                            tokens.add(new Token(TokenType.GT, ">"));
                            index++;
                        }
                        break;
                    case '<':
                        if (match('=')) {
                            tokens.add(new Token(TokenType.LE, "<="));
                        } else {
                            tokens.add(new Token(TokenType.LT, "<"));
                            index++;
                        }
                        break;
                    case '&':
                        if (match('&')) {
                            tokens.add(new Token(TokenType.AND, "&&"));
                        } else {
                            index++;
                        }
                        break;
                    case '|':
                        if (match('|')) {
                            tokens.add(new Token(TokenType.OR, "||"));
                        } else {
                            index++;
                        }
                        break;
                    default:
                        index++;
                        break;
                }
            }
            return tokens;
        }

        private void tokenizeString(char quote) {
            index++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (!isAtEnd() && peek() != quote) {
                sb.append(peek());
                index++;
            }
            if (!isAtEnd()) {
                index++; // closing quote
            }
            tokens.add(new Token(TokenType.STRING, sb.toString()));
        }

        private void tokenizeNumber() {
            StringBuilder sb = new StringBuilder();
            boolean hasDot = false;
            while (!isAtEnd()) {
                char c = peek();
                if (Character.isDigit(c)) {
                    sb.append(c);
                    index++;
                    continue;
                }
                if (c == '.' && !hasDot) {
                    hasDot = true;
                    sb.append(c);
                    index++;
                    continue;
                }
                break;
            }
            tokens.add(new Token(TokenType.NUMBER, sb.toString()));
        }

        private void tokenizeIdentifier() {
            StringBuilder sb = new StringBuilder();
            while (!isAtEnd()) {
                char c = peek();
                if (isIdentifierPart(c)) {
                    sb.append(c);
                    index++;
                } else {
                    break;
                }
            }
            tokens.add(new Token(TokenType.IDENTIFIER, sb.toString()));
        }

        private boolean isIdentifierStart(char c) {
            return Character.isLetter(c) || c == '_' || c == '$';
        }

        private boolean isIdentifierPart(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '$';
        }

        private boolean isAtEnd() {
            return index >= expression.length();
        }

        private char peek() {
            return expression.charAt(index);
        }

        private void add(TokenType type) {
            tokens.add(new Token(type, String.valueOf(expression.charAt(index))));
            index++;
        }

        private boolean match(char expected) {
            if (index + 1 >= expression.length()) {
                return false;
            }
            if (expression.charAt(index + 1) != expected) {
                return false;
            }
            index += 2;
            return true;
        }
    }
}
