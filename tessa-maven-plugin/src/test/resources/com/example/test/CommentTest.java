package com.example.test;

class AsciiDocCommentTest {

  /*
  = Some Asciidoc Content

  This is a sample block comment containing Asciidoc documentation.
  This is used to illustrate that indentation is removed but linebreaks stay intact.

  - Lists
  - can
  - be
  - used

  [source,json]
  ----
  {
    "message": "Hello World"
  }
  ----

  |===
  |Header 1 |Header 2

  |Foo
  |Bar

  |Bla
  |Blubb
  |===
   */

  void someMethod() {
    /*
    = Asciidoc Content

    This also uses Asciidoc.
    It works the same way as at class level.
     */
  }

}

class InvalidRegionTest {

  // region First Region

  void someMethod() {}

  // endregion

  // region Invalid Region Begin

  // region Second Region

  void someOtherMethod() {}

  // endregion
}
