package dev.paoding.longan.data.jpa;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicSqlParser {

    public String parse(String sql, Map<String, Object> params) {
        try {
            PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
            Expression where = select.getWhere();
            if (where != null) {
                select.setWhere(visit(where, params));
            }
            return select.toString();
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private Expression visit(Expression expression, Map<String, Object> params) {
        return switch (expression) {
            case AndExpression andExpression -> visit(andExpression, params);
            case OrExpression orExpression -> visit(orExpression, params);
            case LikeExpression likeExpression -> visit(likeExpression, params);
            case EqualsTo equalsTo -> visit(equalsTo, params);
            case GreaterThan greaterThan -> visit(greaterThan, params);
            case MinorThan minorThan -> visit(minorThan, params);
            case MinorThanEquals minorThanEquals -> visit(minorThanEquals, params);
            case GreaterThanEquals greaterThanEquals -> visit(greaterThanEquals, params);
            case NotEqualsTo notEqualsTo -> visit(notEqualsTo, params);
            case InExpression inExpression -> visit(inExpression, params);
            case Between between -> visit(between, params);
            case Parenthesis parenthesis -> visit(parenthesis, params);
            case null, default -> expression;
        };
    }

    private Expression visit(Parenthesis parenthesis, Map<String, Object> params) {
        Expression expression = visit(parenthesis.getExpression(), params);
        if (expression == null) {
            return null;
        }
        if (OrExpression.class.isAssignableFrom(expression.getClass()) || AndExpression.class.isAssignableFrom(expression.getClass())) {
            parenthesis.setExpression(expression);
            return parenthesis;
        }
        return expression;
    }

    private Expression visit(AndExpression andExpression, Map<String, Object> params) {
        Expression leftExpress = visit(andExpression.getLeftExpression(), params);
        Expression rightExpress = visit(andExpression.getRightExpression(), params);
        if (leftExpress != null && rightExpress != null) {
            andExpression.setLeftExpression(leftExpress);
            andExpression.setRightExpression(rightExpress);
            return andExpression;
        }
        if (leftExpress == null && rightExpress == null) {
            return null;
        }
        if (leftExpress != null) {
            return leftExpress;
        }
        return rightExpress;
    }

    private Expression visit(OrExpression orExpression, Map<String, Object> params) {
        Expression leftExpress = visit(orExpression.getLeftExpression(), params);
        Expression rightExpress = visit(orExpression.getRightExpression(), params);
        if (leftExpress != null && rightExpress != null) {
            orExpression.setLeftExpression(leftExpress);
            orExpression.setRightExpression(rightExpress);
            return orExpression;
        }
        if (leftExpress == null && rightExpress == null) {
            return null;
        }
        if (leftExpress != null) {
            return leftExpress;
        }
        return rightExpress;
    }

    private Expression visit(Between between, Map<String, Object> params) {
        if (between.getBetweenExpressionStart() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (between.getBetweenExpressionEnd() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return between;
    }

    private Expression visit(InExpression inExpression, Map<String, Object> params) {
        if (inExpression.getRightExpression() instanceof ParenthesedExpressionList<?> parenthesedExpressionList) {
            List<Expression> expressionList = new ArrayList<>();
            for (Object object : parenthesedExpressionList) {
                if (object instanceof JdbcNamedParameter jdbcNamedParameter) {
                    if (params.containsKey(jdbcNamedParameter.getName())) {
                        expressionList.add(jdbcNamedParameter);
                    }
                } else {
                    expressionList.add((Expression) object);
                }
            }
            if (expressionList.isEmpty()) {
                return null;
            } else {
                inExpression.setRightExpression(new ParenthesedExpressionList<>(expressionList));
            }
        } else if (inExpression.getRightExpression() instanceof ParenthesedSelect parenthesedSelect) {
            PlainSelect plainSelect = parenthesedSelect.getPlainSelect();
            plainSelect.setWhere(visit(plainSelect.getWhere(), params));
        }
        return inExpression;
    }

    private Expression visit(NotEqualsTo notEqualsTo, Map<String, Object> params) {
        if (notEqualsTo.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (notEqualsTo.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return notEqualsTo;
    }

    private Expression visit(EqualsTo equalsTo, Map<String, Object> params) {
        if (equalsTo.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (equalsTo.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return equalsTo;
    }

    private Expression visit(MinorThan minorThan, Map<String, Object> params) {
        if (minorThan.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (minorThan.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return minorThan;
    }

    private Expression visit(MinorThanEquals minorThanEquals, Map<String, Object> params) {
        if (minorThanEquals.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (minorThanEquals.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return minorThanEquals;
    }

    private Expression visit(GreaterThanEquals greaterThanEquals, Map<String, Object> params) {
        if (greaterThanEquals.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (greaterThanEquals.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return greaterThanEquals;
    }

    private Expression visit(GreaterThan greaterThan, Map<String, Object> params) {
        if (greaterThan.getLeftExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        if (greaterThan.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return greaterThan;
    }

    private Expression visit(LikeExpression likeExpression, Map<String, Object> params) {
        if (likeExpression.getRightExpression() instanceof Function function) {
            for (Expression parameter : function.getParameters()) {
                if (parameter instanceof JdbcNamedParameter jdbcNamedParameter) {
                    if (!params.containsKey(jdbcNamedParameter.getName())) {
                        return null;
                    }
                }
            }
        } else if (likeExpression.getRightExpression() instanceof JdbcNamedParameter jdbcNamedParameter) {
            if (!params.containsKey(jdbcNamedParameter.getName())) {
                return null;
            }
        }
        return likeExpression;
    }
}
