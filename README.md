Jacket
===

Jacket, implementation of Lisp on the JVM.
This is a pet project. It's goals:
* Learning JVM internals
* Practicing theory of compilation and interpretation
* Building yet another LISP implementation
* Doing something fun, big and ambitious

Also, have an idea to try to implement currying in s-expressions.

What's ready:
* Lexer with regular grammar recognized by a finite state machine. Batteries included (FSM).
* Parser.
* Semantic analysis.
* Code generation: readln; polymorphic n-arity funcitons: println, print, arithmetics (+, -, *, /), logical operations (and, or, xor, not)

Under development:
* Coming very soon: comparison operators

Not yet
* Code generation: conditionals (if, cond), lists and operations (cons, car, cdr), variables and scopes (let, define), lambda functions

Probably
* Macros
* S-expressions currying
* tail recursions support
* Java interop
* Optimizations