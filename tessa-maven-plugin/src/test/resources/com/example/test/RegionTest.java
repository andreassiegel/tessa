package com.example.test;

class SimpleRegionTest {

  // region First Region

  void firstRegionMethod() {}

  // endregion
}

class MultiRegionTest {

  // region First Region

  void firstRegionMethod() {}

  // endregion

  // region Second Region

  void secondRegionMethod() {}

  // endregion
}


class NestedRegionTest {

  // region First Region

  // region Nested Region 1

  void firstRegionMethod1() {}

  // endregion

  // region Nested Region 2

  void firstRegionMethod2() {}

  // endregion

  // endregion

  // region Second Region

  void secondRegionMethod() {}

  // endregion
}

class NestedMethodRegionTest {

  // region Class Region

  void method() {
    // region Method Region

    var foo = "bar";

    // endregion
  }

  // endregion
}
