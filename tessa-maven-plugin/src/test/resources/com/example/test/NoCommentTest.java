package com.example.test;

class NoCommentTest {}

/*
This is a block comment.
 */
class BlockCommentTest {}

// This is a line comment.
class LineCommentTest {}

class MethodTest {

  void methodWithoutComment() {}

  /*
  This is a block comment at method level.
   */
  void methodWithBlockComment() {}

  // This is a line comment at method level.
  void methodWithLineComment() {}
}
