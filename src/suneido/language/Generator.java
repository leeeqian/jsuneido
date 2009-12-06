package suneido.language;

import suneido.language.ParseExpression.Value;

public abstract class Generator<T> {

	public void lvalue(Value<T> value) {
	}

	public void lvalueForAssign(Value<T> value, Token op) {
	}

	public T assignment(T term, Value<T> value, Token op, T expression) {
		return null;
	}

	public abstract T binaryExpression(Token op, T expr1, T expr2);

	public abstract T and(T expr1, T expr2);

	public Object and(Object prevlabel) {
		return null;
	}

	public void andEnd(Object label) {
	}

	public abstract T or(T expr1, T expr2);

	public Object or(Object label) {
		return null;
	}

	public void orEnd(Object label) {
	}

	public Object conditionalTrue(Object label, T first) {
		return null;
	}

	public abstract T conditional(T primaryExpression, T first, T second,
			Object label);

	public void dowhileContinue(Object label) {
	}

	public T dowhileStatement(T statement, T expression, Object label) {
		return null;
	}

	public T foreverStatement(T statement, Object label) {
		return null;
	}

	public T expressionStatement(T expression) {
		return null;
	}

	public T function(T params, T compound) {
		return null;
	}

	public void startFunction(T name) {
	}

	public Object startBlock() {
		return true;
	}

	public abstract T identifier(String text);

	public Object ifExpr(T expr) {
		return null;
	}

	public void ifThen(Object label, T t) {
	}

	public Object ifElse(Object label) {
		return null;
	}

	public T ifStatement(T expression, T t, T e, Object label) {
		return null;
	}

	public abstract T in(T expression, T constant);

	public T returnStatement(T expression, Object context) {
		return null;
	}

	public void afterStatement(T statements) {
	}

	public T statementList(T n, T next) {
		return null;
	}

	public abstract T unaryExpression(Token op, T expression);

	public Object loop() {
		return true;
	}

	public Object dowhileLoop() {
		return true;
	}

	public void whileExpr(T expr, Object loop) {
	}

	public T whileStatement(T expr, T statement, Object loop) {
		return null;
	}

	public abstract T number(String value);

	public abstract T string(String value);

	public abstract T date(String value);

	public abstract T symbol(String identifier);

	public abstract T bool(boolean value);

	public T breakStatement(Object loop) {
		return null;
	}

	public T continueStatement(Object loop) {
		return null;
	}

	public T throwStatement(T expression) {
		return null;
	}

	public T catcher(String variable, String pattern, T statement) {
		return null;
	}

	public Object startTry() {
		return null;
	}

	public void startCatch(String var, String pattern, Object trycatch) {
	}

	public T tryStatement(T tryStatement, T catcher, Object trycatch) {
		return null;
	}

	public Object startSwitch() {
		return null;
	}

	public void startCase(Object labels) {
	}

	public void startCaseValue() {
	}

	public void startCaseBody(Object labels) {
	}

	public T caseValues(T values, T expression, Object labels,
			boolean more) {
		return null;
	}

	public T switchCases(T cases, T values, T statements, Object labels) {
		return null;
	}

	public T switchStatement(T expression, T cases, Object labels) {
		return null;
	}

	public Object forInExpression(String var, T expr) {
		return true;
	}

	public T forInStatement(String var, T expr, T statement,
			Object loop) {
		return null;
	}

	public T forClassicStatement(T expr1, T expr2, T expr3,
			T statement, Object loop) {
		return null;
	}

	public Object forStart() {
		return null;
	}

	public void forIncrement(Object label) {
	}

	public void forCondition(T cond, Object loop) {
	}

	public T expressionList(T list, T expression) {
		return null;
	}

	public T preIncDec(T term, Token incdec, Value<T> value) {
		return null;
	}

	public T postIncDec(T term, Token incdec, Value<T> value) {
		return null;
	}

	public T member(T term, Value<T> value) {
		return null;
	}

	public T subscript(T term, T expression) {
		return null;
	}

	public T selfRef() {
		return null;
	}

	public T superRef() {
		return null;
	}

	public void preFunctionCall(Value<T> value) {
	}

	public abstract T functionCall(T function, Value<T> value, T arguments);

	public void newCall() {
	}

	public T newExpression(T term, T arguments) {
		return null;
	}

	public abstract T argumentList(T list, Object keyword, T expression);

	public void argumentName(Object keyword) {
	}

	public void atArgument(String n) {
	}

	public T atArgument(String n, T expr) {
		return null;
	}

	public void blockParams() {
	}

	public T block(T params, T statements) {
		return null;
	}

	public T parameters(T list, String name, T defaultValue) {
		return null;
	}

	public abstract T memberList(MType which, T list, T member);

	public void startClass() {
	}

	public T classConstant(String base, T members) {
		return null;
	}

	public abstract T memberDefinition(T name, T value);

	public enum MType { OBJECT, RECORD, CLASS };

	public abstract T object(MType which, T members);

	public abstract T constant(T value);

	public void addSuperInit() {
	}

	public abstract T rvalue(T expr);

	public void finish() {
	}

	public void startObject() {
	}

	public T argumentListConstant(T args, Object keyword, T value) {
		return argumentList(args, keyword, constant(value));
	}

}
