/* Copyright 2013 (c) Suneido Software Corp. All rights reserved.
 * Licensed under GPLv2.
 */

package suneido.compiler;

import static suneido.compiler.Token.*;

/**
 * Parser for Suneido <code>dll</code> entity (part of the DLL interface).
 *
 * @author Victor Schappert
 * @since 20130705
 * @see ParseStruct
 * @see ParseCallback
 *
 * @param <T>
 *            Parse result type (<em>ie</em> {@link AstNode}).
 * @param <G>
 *            Result type generator class (<em>ie</em> {@link AstGenerator}).
 */
public final class ParseDll<T, G extends Generator<T>> extends ParseDllEntity<T, G> {

	//
	// CONSTRUCTORS
	//

	public ParseDll(Lexer lexer, G generator) {
		super(lexer, generator);
	}

	ParseDll(Parse<T, G> parse) {
		super(parse);
	}

	//
	// RECURSIVE DESCENT PARSING OF 'dll' ENTITY
	//

	public T parse() {
		return matchReturn(EOF, dll());
	}

	public T dll() {
		int lineNumber = lexer.getLineNumber();
		matchSkipNewlines(DLL);
		// Return Type
		final String returnType = lexer.getValue();
		match(IDENTIFIER);
			// NOTE: Currently only value-type storage of the basic types, plus
			//       'void', constitute possible return values from dlls, so
			//       don't need to worry about '*' for pointers or '[#]' for
			//       arrays.
		// Library Name
		final String libraryName = lexer.getValue();
		match(IDENTIFIER);
		match(COLON);
		// Function Name as provided by the user
		final StringBuilder userFunctionName = new StringBuilder(
				lexer.getValue());
		match(IDENTIFIER);
		if (AT == token) {
			match();
			userFunctionName.append('@');
			userFunctionName.append(lexer.getValue());
			matchNonNegativeInteger();
		}
		// Parameters
		match(L_PAREN);
		T params = typeList(DLL);
		return generator.dll(libraryName, userFunctionName.toString(),
				returnType, params, lineNumber);
	}
}
